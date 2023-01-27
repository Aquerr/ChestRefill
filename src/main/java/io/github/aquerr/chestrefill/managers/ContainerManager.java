package io.github.aquerr.chestrefill.managers;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import io.github.aquerr.chestrefill.scheduling.ScanForEmptyContainersTask;
import io.github.aquerr.chestrefill.storage.StorageHelper;
import io.github.aquerr.chestrefill.util.ModSupport;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.block.entity.carrier.chest.Chest;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static io.github.aquerr.chestrefill.util.WorldUtils.getWorldByUUID;
import static java.util.Optional.ofNullable;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

/**
 * Created by Aquerr on 2018-02-13.
 */
public class ContainerManager
{
    private final ChestRefill plugin;
    private final StorageHelper storageHelper;

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
        //We do not need to restart the refill scheduler task. New chest content will be loaded from the storage by existing scheduler.
        return storageHelper.addOrUpdateContainer(refillableContainer);
    }

    public Optional<RefillableContainer> getRefillableContainer(String containerName)
    {
        return getRefillableContainers().stream()
                .filter(refillableContainer -> refillableContainer.getName().equals(containerName))
                .findFirst();
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

        return successfullyStopped && successfullyRemoved;
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
            final ServerWorld world =  getWorldByUUID(chestToRefill.getContainerLocation().getWorldUUID()).orElse(null);
            if (world == null)
            {
                this.plugin.getLogger().error(String.format("World with UUID = '%s' does not exist!", chestToRefill.getContainerLocation().getWorldUUID().toString()));
            }
            else
            {
                final ServerLocation location = ServerLocation.of(world, chestToRefill.getContainerLocation().getBlockPosition());

                //If chest is hidden then we need to show it
                if (!location.blockEntity().isPresent() && chestToRefill.shouldBeHiddenIfNoItems())
                {
                    location.setBlockType(chestToRefill.getContainerBlockType());
                }

                final Optional<? extends BlockEntity> optionalBlockEntity = location.blockEntity();
                if(optionalBlockEntity.isPresent())
                {
                    final BlockEntity blockEntity = location.blockEntity().get();
                    Inventory blockEntityInventory;
                    if (ModSupport.isStorageUnitFromActuallyAdditions(blockEntity))
                        blockEntityInventory = ModSupport.getInventoryFromActuallyAdditions(blockEntity);
                    else
                    {
                        final CarrierBlockEntity carrierBlockEntity = (CarrierBlockEntity) blockEntity;
                        blockEntityInventory = carrierBlockEntity.inventory();
                        if (carrierBlockEntity instanceof Chest)
                            blockEntityInventory = ((Chest) carrierBlockEntity).doubleChestInventory().orElse(blockEntityInventory);
                    }

                    if (chestToRefill.shouldReplaceExistingItems())
                    {
                        blockEntityInventory.clear();
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

                            refillItems(blockEntityInventory, Collections.singletonList(lowestChanceItem), true, chestToRefill.shouldPlaceItemsInRandomSlots());
                        }
                    }
                    else
                    {
                        refillItems(blockEntityInventory, itemsAchievedFromRandomizer, false, chestToRefill.shouldPlaceItemsInRandomSlots());
                    }

                    tryHideContainer(chestToRefill, blockEntityInventory, location);
                    return true;
                }
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }

        this.plugin.getServer().sendMessage(linear(RED, text("Couldn't refill : " + chestToRefill.getName())));
        this.plugin.getServer().sendMessage(linear(RED, text("Container block type : " + chestToRefill.getContainerBlockType())));
        this.plugin.getServer().sendMessage(linear(RED, text("Container block position : " + chestToRefill.getContainerLocation().getBlockPosition() + "|" + chestToRefill.getContainerLocation().getWorldUUID().toString())));
        this.plugin.getServer().sendMessage(linear(RED, text("Container items : " + chestToRefill.getItems())));
        this.plugin.getServer().sendMessage(linear(RED, text("Suggestion: Remove this container from the containers.json file and restart the server.")));

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
        if (refillableContainer != null)
            refillableContainer.setKit(kitName);
        return this.storageHelper.assignKit(containerLocation, kitName);
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

    private void refillItems(final Inventory inventory, final List<RefillableItem> refillableItems, final boolean stopAtFirstItem, final boolean placeItemsInRandomSlots)
    {
        if (placeItemsInRandomSlots)
        {
            final int numberOfSlots = inventory.capacity();
            int itemIndex = 0;
            for (; itemIndex < refillableItems.size(); itemIndex++)
            {
                final RefillableItem refillableItem = refillableItems.get(itemIndex);
                final int randomSlot = ThreadLocalRandom.current().nextInt(numberOfSlots);
                Slot slot = inventory.slot(randomSlot).orElse(null);
                if (slot == null)
                {
                    itemIndex--;
                    continue;
                }

                if (slot.totalQuantity() != 0)
                {
                    itemIndex--;
                    continue;
                }
                slot.offer(refillableItem.getItem().createStack());
                if (stopAtFirstItem)
                    break;
            }
        }
        else
        {
            for (final RefillableItem item : refillableItems)
            {
                int i = 0;
                for(final Inventory slot : inventory.slots())
                {
                    if(item.getSlot() == i)
                    {
                        slot.offer(item.getItem().createStack());
                        if (stopAtFirstItem)
                            break;
                    }
                    i++;
                }
            }
        }
    }

    private void tryHideContainer(final RefillableContainer refillableContainer, final Inventory inventory, final ServerLocation containerLocation)
    {
        if (refillableContainer.shouldBeHiddenIfNoItems() && inventory.totalQuantity() == 0)
        {
            containerLocation.setBlockType(refillableContainer.getHidingBlock());
        }
    }
}
