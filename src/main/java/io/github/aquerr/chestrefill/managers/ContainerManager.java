package io.github.aquerr.chestrefill.managers;

import com.flowpowered.math.vector.Vector3d;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import io.github.aquerr.chestrefill.scheduling.ScanForEmptyContainersTask;
import io.github.aquerr.chestrefill.storage.StorageHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleOptions;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Color;
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

//            final Optional<World> optionalWorld = Sponge.getServer().getWorld(containerLocation.getWorldUUID());
//            if(optionalWorld.isPresent())
//            {
//                this.plugin.getContainerScheduler().scheduleWithInterval(name + "_particle", 50, TimeUnit.MILLISECONDS, startParticleEffect(containerLocation, optionalWorld.get()));
//            }

            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

//    private Runnable startParticleEffect(final ContainerLocation containerLocation, final World world)
//    {
//        return new Runnable()
//        {
//            private World chestWorld = world;
//            private int maxHeight = 2;
//            private Vector3d chestLocation = new Vector3d(containerLocation.getBlockPosition().getX(), containerLocation.getBlockPosition().getY(), containerLocation.getBlockPosition().getZ());
//            private Vector3d lastParticleLocation = new Vector3d(chestLocation);
//
//            private final double d = Math.PI - 2 * Math.acos(0.1 / (2 * 6));
//            private final double l = (2 * Math.PI) / d;
//            private int i = 0;
//
//            @Override
//            public void run()
//            {
//                if(i > l)
//                    i = 0;
//
//                double x = chestLocation.getX() + 0.5;
//                double z = chestLocation.getZ() + 0.5;
//
//                double xOffset = 2 * Math.cos(i * d);
//                double zOffset = 2 * Math.sin(i * d);
//
//                double xOffset2 = 2 * Math.cos((i - 50) * d);
//                double zOffset2 = 2 * Math.sin((i - 50) * d);
//
//                final ParticleEffect.Builder particleEffectBuilder = ParticleEffect.builder();
//                particleEffectBuilder.type(ParticleTypes.REDSTONE_DUST)
//                        .option(ParticleOptions.COLOR, Color.CYAN)
//                        .quantity(10);
//                final ParticleEffect.Builder particleEffectBuilder2 = ParticleEffect.builder();
//                particleEffectBuilder2.type(ParticleTypes.REDSTONE_DUST)
//                        .option(ParticleOptions.COLOR, Color.RED)
//                        .quantity(10);
//                chestWorld.spawnParticles(particleEffectBuilder.build(), new Vector3d(x + xOffset, chestLocation.getY(), z + zOffset));
//                chestWorld.spawnParticles(particleEffectBuilder2.build(), new Vector3d(x + xOffset2, chestLocation.getY(), z + zOffset2));
//                i++;
//
////                double x = lastParticleLocation.getX();
////                double y = lastParticleLocation.getY();
////                double z = lastParticleLocation.getZ();
////
////                double newY = 0.5 * Math.sin(20*x) + chestLocation.getY() + 0.5;
////                double newX = x + 0.1;
////
////                if(newX > chestLocation.getX() + 1)
////                    newX = chestLocation.getX();
////
////                y = newY;
////                x = newX;
////
////                final Vector3d particleLocation = new Vector3d(x, y, z);
////                final ParticleEffect.Builder particleEffectBuilder = ParticleEffect.builder();
////                particleEffectBuilder.type(ParticleTypes.REDSTONE_DUST)
////                        .quantity(1)
////                        .offset(new Vector3d())
////                        .velocity(new Vector3d(0, 0, 0));
////                chestWorld.spawnParticles(particleEffectBuilder.build(), particleLocation);
////                lastParticleLocation = particleLocation;
//            }
//        };
//    }

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
                        final List<RefillableItem> refillableItems = chestToRefill.getKitName().equals("") ? chestToRefill.getItems() : getKit(chestToRefill.getKitName()).getItems();
                        for (RefillableItem refillableItem : refillableItems)
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
            startRefillingContainer(refillableContainer.getContainerLocation(), refillableContainer.getRestoreTime());
//            String name = "Chest Refill " + refillableContainer.getContainerLocation().getBlockPosition().toString() + "|" + refillableContainer.getContainerLocation().getWorldUUID().toString();
//            this.plugin.getContainerScheduler().scheduleWithInterval(name, refillableContainer.getRestoreTime(), TimeUnit.SECONDS, runRefillContainer(refillableContainer.getContainerLocation()));
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

    public Map<String, Kit> getKits()
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
        refillableContainer.setKit(kitName);
//        final Map<String, Kit> kits = getKits();
//        Kit assignedKit = null;
//
//        for(Kit kit : kits.values())
//        {
//            if(kit.getName().equals(kitName))
//            {
//                assignedKit = kit;
//            }
//        }
//
//        if(assignedKit != null)
//        {
            //This code modifies cache. This is bad. We should not modify cache outside ContainerCache class.
//            refillableContainer.setItems(assignedKit.getItems());
//            refillableContainer.setKit(kitName);
            return this.storageHelper.assignKit(containerLocation, kitName);
//        }

//        return false;
    }

    public Optional<RefillableContainer> getRefillableContainerAtLocation(ContainerLocation containerLocation)
    {
        final RefillableContainer refillableContainer = this.storageHelper.getRefillableContainer(containerLocation);
        if(refillableContainer == null)
        {
            return Optional.empty();
        }
        else
        {
            return Optional.of(refillableContainer);
        }
    }

    public void startLookingForEmptyContainers()
    {
        this.plugin.getContainerScheduler().scheduleWithInterval("Chest Refill - Scanning for empty containers", 5L, TimeUnit.SECONDS, new ScanForEmptyContainersTask(this));
    }

    public Kit getKit(final String name)
    {
        return getKits().get(name);
    }
}
