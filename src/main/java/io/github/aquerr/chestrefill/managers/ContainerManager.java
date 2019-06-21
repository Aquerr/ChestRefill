package io.github.aquerr.chestrefill.managers;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.caching.ContainerCache;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import io.github.aquerr.chestrefill.storage.StorageHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aquerr on 2018-02-13.
 */
public class ContainerManager
{
    private ChestRefill plugin;
    private StorageHelper storageHelper;

    public ContainerManager(ChestRefill plugin, Path configDir)
    {
        this.plugin = plugin;

        if (!Files.isDirectory(configDir))
        {
            try
            {
                Files.createDirectory(configDir);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        storageHelper = new StorageHelper(configDir);
    }

    public boolean addRefillableContainer(RefillableContainer refillableContainer)
    {
        if (storageHelper.addOrUpdateContainer(refillableContainer))
        {
            return startRefillingContainer(refillableContainer.getContainerLocation(), refillableContainer.getRestoreTime());
        }

        return false;
    }

    public boolean updateRefillableContainer(RefillableContainer refillableContainer)
    {
        //We do not need to restart scheduler. New chest content will be loaded from the storage by existing scheduler.
        return storageHelper.addOrUpdateContainer(refillableContainer);
    }

    public Collection<RefillableContainer> getRefillableContainers()
    {
        return storageHelper.getRefillableContainers();
    }

    public Set<ContainerLocation> getContainerLocations()
    {
        return storageHelper.getContainerLocations();
    }

    public boolean removeRefillableContainer(ContainerLocation containerLocation)
    {
        final boolean successfullyStopped = stopRefillingContainer(containerLocation);
        final boolean successfullyRemoved = storageHelper.removeContainer(containerLocation);

        if(successfullyStopped && successfullyRemoved)
            return true;

        return false;
    }

    private boolean stopRefillingContainer(ContainerLocation containerLocation)
    {
        try
        {
            this.plugin.getContainerScheduler().cancelTask("Chest Refill " + containerLocation.getBlockPosition().toString()
                    + "|" + containerLocation.getWorldUUID().toString());
            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return false;
    }

    @Nullable
    private RefillableContainer getRefillableContainer(ContainerLocation containerLocation)
    {
        return storageHelper.getRefillableContainer(containerLocation);
    }

    private boolean startRefillingContainer(ContainerLocation containerLocation, int time)
    {
        try
        {
            String name = "Chest Refill " + containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID().toString();
            this.plugin.getContainerScheduler().scheduleWithInterval(name, time, TimeUnit.SECONDS, runRefillContainer(containerLocation));
            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean refillContainer(final ContainerLocation containerLocation)
    {
        final RefillableContainer chestToRefill = getRefillableContainer(containerLocation);
        try
        {
            final Optional<World> world =  Sponge.getServer().getWorld(chestToRefill.getContainerLocation().getWorldUUID());
            if (world.isPresent())
            {
                synchronized(chestToRefill)
                {
                    final Location<World> location = new Location<>(world.get(), chestToRefill.getContainerLocation().getBlockPosition());

                    //If chest is hidden then we need to show it
                    if (!location.getTileEntity().isPresent() && chestToRefill.shouldBeHiddenIfNoItems())
                    {
                        location.setBlockType(chestToRefill.getContainerBlockType());
                    }

                    final Optional<TileEntity> optionalTileEntity = location.getTileEntity();
                    if(optionalTileEntity.isPresent())
                    {
                        final TileEntityCarrier chest = (TileEntityCarrier) location.getTileEntity().get();
                        if (chestToRefill.shouldReplaceExistingItems())
                        {
                            chest.getInventory().clear();
                        }

                        final List<RefillableItem> itemsAchievedFromRandomizer = new ArrayList<>();
                        for (RefillableItem refillableItem : chestToRefill.getItems())
                        {
                            double number = Math.random();
                            if (number <= refillableItem.getChance())
                            {
                                itemsAchievedFromRandomizer.add(refillableItem);
                            }
                        }

                        if (chestToRefill.isOneItemAtTime())
                        {
                            if (itemsAchievedFromRandomizer.size() > 0)
                            {
                                RefillableItem lowestChanceItem = itemsAchievedFromRandomizer.get(0);
                                for (RefillableItem item : itemsAchievedFromRandomizer)
                                {
                                    if (item.getChance() < lowestChanceItem.getChance())
                                    {
                                        lowestChanceItem = item;
                                    }
                                }

                                //Refill item
                                int i = 0;
                                for(final Inventory slot : chest.getInventory().slots())
                                {
                                    if(lowestChanceItem.getSlot() == i)
                                    {
                                        //Offer removes items from existing list and that's why we need to build a new itemstack
                                        slot.offer(ItemStack.builder().fromItemStack(lowestChanceItem.getItem()).build());
                                        break;
                                    }

                                    i++;
                                }
                            }
                        }
                        else
                        {
                            for (final RefillableItem item : itemsAchievedFromRandomizer)
                            {
                                int i = 0;
                                for(final Inventory slot : chest.getInventory().slots())
                                {
                                    if(item.getSlot() == i)
                                    {
                                        //Offer removes items from existing list and that's why we need to build a new itemstack
                                        slot.offer(ItemStack.builder().fromItemStack(item.getItem()).build());
                                    }
                                    i++;
                                }
                            }
                        }

                        if (chestToRefill.shouldBeHiddenIfNoItems() && chest.getInventory().totalItems() == 0)
                        {
                            location.setBlockType(chestToRefill.getHidingBlock());
                        }
                        return true;
                    }
                }
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        this.plugin.getConsole().sendMessage(Text.of(TextColors.RED, "Couldn't refill : " + chestToRefill.getName()));
        this.plugin.getConsole().sendMessage(Text.of(TextColors.RED, "Container block type : " + chestToRefill.getContainerBlockType()));
        this.plugin.getConsole().sendMessage(Text.of(TextColors.RED, "Container block position : " + chestToRefill.getContainerLocation().getBlockPosition() + "|" + chestToRefill.getContainerLocation().getWorldUUID().toString()));
        this.plugin.getConsole().sendMessage(Text.of(TextColors.RED, "Container items : " + chestToRefill.getItems()));
        this.plugin.getConsole().sendMessage(Text.of(TextColors.YELLOW, "Suggestion: Remove this container from the containers.json file and restart server."));

        return false;
    }

    public Runnable runRefillContainer(ContainerLocation containerLocation)
    {
        return () -> refillContainer(containerLocation);
    }

    public void restoreRefilling()
    {
        for (RefillableContainer refillableContainer : getRefillableContainers())
        {
            String name = "Chest Refill " + refillableContainer.getContainerLocation().getBlockPosition().toString() + "|" + refillableContainer.getContainerLocation().getWorldUUID().toString();
            this.plugin.getContainerScheduler().scheduleWithInterval(name, refillableContainer.getRestoreTime(), TimeUnit.SECONDS, runRefillContainer(refillableContainer.getContainerLocation()));
        }
    }

    public boolean updateRefillingTime(ContainerLocation containerLocation, int time)
    {
        if (stopRefillingContainer(containerLocation)
            && storageHelper.updateContainerTime(containerLocation, time)
            && startRefillingContainer(containerLocation, time)) return true;

        return false;
    }

    public boolean renameRefillableContainer(ContainerLocation containerLocation, String containerName)
    {
        return this.storageHelper.changeContainerName(containerLocation, containerName);
    }

    public List<Kit> getKits()
    {
        return this.storageHelper.getKits();
    }

    public boolean createKit(Kit kit)
    {
        return this.storageHelper.createKit(kit);
    }

    public boolean removeKit(String kitName)
    {
        return this.storageHelper.removeKit(kitName);
    }

    public boolean assignKit(ContainerLocation containerLocation, String kitName)
    {
        //We need to load items from kit and assign them to the container.
        final RefillableContainer refillableContainer = getRefillableContainer(containerLocation);
        final List<Kit> kits = getKits();
        Kit assignedKit = null;

        for(Kit kit : kits)
        {
            if(kit.getName().equals(kitName))
            {
                assignedKit = kit;
            }
        }

        if(assignedKit != null)
        {
            //This code modifies cache. This is bad. We should not modify cache outside ContainerCache class.
            refillableContainer.setItems(assignedKit.getItems());
            refillableContainer.setKit(assignedKit.getName());
            return this.storageHelper.assignKit(containerLocation, kitName);
        }

        return false;
    }
}
