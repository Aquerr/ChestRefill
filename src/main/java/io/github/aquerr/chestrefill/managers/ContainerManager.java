package io.github.aquerr.chestrefill.managers;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import io.github.aquerr.chestrefill.storage.JSONStorage;
import io.github.aquerr.chestrefill.storage.Storage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.scheduler.Task;
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
    private static Storage containerStorage;

    public static void setupContainerManager(Path configDir)
    {

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

        containerStorage = new JSONStorage(configDir);
    }

    public static boolean addRefillableContainer(RefillableContainer refillableContainer)
    {
        if (containerStorage.addOrUpdateContainer(refillableContainer))
        {
            return startRefillingContainer(refillableContainer.getContainerLocation(), refillableContainer.getRestoreTime());
        }

        return false;
    }

    public static boolean updateRefillableContainer(RefillableContainer refillableContainer)
    {
        //We do not need to restart scheduler. New chest content will be loaded from the storage by existing scheduler.
        return containerStorage.addOrUpdateContainer(refillableContainer);
    }

    public static List<RefillableContainer> getRefillableContainers()
    {
        return containerStorage.getRefillableContainers();
    }

    public static List<ContainerLocation> getContainerLocations()
    {
        return containerStorage.getContainerLocations();
    }

    public static boolean removeRefillableContainer(ContainerLocation containerLocation)
    {
        if (containerStorage.removeRefillableContainers(containerLocation))
        {
            return stopRefillingContainer(containerLocation);
        }

        return false;
    }

    private static boolean stopRefillingContainer(ContainerLocation containerLocation)
    {
        try
        {
            Optional<Task> optionalTask = Sponge.getScheduler().getScheduledTasks().stream().filter(x->x.getName().equals("Chest Refill " + containerLocation.getBlockPosition().toString()
                    + "|" + containerLocation.getWorldUUID().toString())).findFirst();

            if (optionalTask.isPresent())
            {
                optionalTask.get().cancel();
                return true;
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return false;
    }

    @Nullable
    private static RefillableContainer getRefillableContainer(ContainerLocation containerLocation)
    {
        return containerStorage.getRefillableContainer(containerLocation);
    }

    private static boolean startRefillingContainer(ContainerLocation containerLocation, int time)
    {
        try
        {
            Task.Builder refillTask = Sponge.getScheduler().createTaskBuilder();

            refillTask.execute(refillContainer(containerLocation)).delay(time, TimeUnit.SECONDS)
                    .name("Chest Refill " + containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID().toString())
                    .submit(ChestRefill.getChestRefill());

            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

    public static Runnable refillContainer(ContainerLocation containerLocation)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                RefillableContainer chestToRefill = getRefillableContainer(containerLocation);

                Optional<World> world =  Sponge.getServer().getWorld(chestToRefill.getContainerLocation().getWorldUUID());

                if (world.isPresent())
                {
                    Location location = new Location(world.get(), chestToRefill.getContainerLocation().getBlockPosition());

                    //If chest is hidden the we need to show it
                    if (!location.getTileEntity().isPresent() && chestToRefill.shouldBeHiddenIfNoItems())
                    {
                        location.setBlockType(chestToRefill.getContainerBlockType());
                    }

                    TileEntityCarrier chest = (TileEntityCarrier) location.getTileEntity().get();

                    if (chestToRefill.shouldReplaceExistingItems())
                    {
                        chest.getInventory().clear();
                    }

                    List<RefillableItem> achievedItemsFromRandomizer = new ArrayList<>();
                    for (RefillableItem refillableItem : chestToRefill.getItems())
                    {
                        double number = Math.random();

                        if (number <= refillableItem.getChance())
                        {
                            achievedItemsFromRandomizer.add(refillableItem);
                        }
                    }

                    if (chestToRefill.isOneItemAtTime())
                    {
                        if (achievedItemsFromRandomizer.size() > 0)
                        {
                            RefillableItem lowestChanceItem = achievedItemsFromRandomizer.get(0);
                            for (RefillableItem item : achievedItemsFromRandomizer)
                            {
                                if (item.getChance() < lowestChanceItem.getChance())
                                {
                                    lowestChanceItem = item;
                                }
                            }

                            chest.getInventory().offer(lowestChanceItem.getItem());
                        }
                    }
                    else
                    {
                        for (RefillableItem item : achievedItemsFromRandomizer)
                        {
                            chest.getInventory().offer(item.getItem());
                        }
                    }

                    if (chestToRefill.shouldBeHiddenIfNoItems() && chest.getInventory().totalItems() == 0)
                    {
                        //location.removeBlock();
                        location.setBlockType(chestToRefill.getHidingBlock());
                    }
                }

                Task.Builder refillTask = Sponge.getScheduler().createTaskBuilder();

                refillTask.execute(refillContainer(chestToRefill.getContainerLocation())).delay(chestToRefill.getRestoreTime(), TimeUnit.SECONDS)
                        .name("Chest Refill " + chestToRefill.getContainerLocation().getBlockPosition().toString() + "|" + chestToRefill.getContainerLocation().getWorldUUID().toString())
                        .submit(ChestRefill.getChestRefill());
            }
        };
    }

    public static void restoreRefilling()
    {
        for (RefillableContainer refillableContainer : getRefillableContainers())
        {
            Task.Builder refilling = Sponge.getScheduler().createTaskBuilder();

            refilling.execute(refillContainer(refillableContainer.getContainerLocation())).delay(refillableContainer.getRestoreTime(), TimeUnit.SECONDS)
                    .name("Chest Refill " + refillableContainer.getContainerLocation().getBlockPosition().toString() + "|" + refillableContainer.getContainerLocation().getWorldUUID().toString())
                    .submit(ChestRefill.getChestRefill());
        }
    }

    public static boolean updateRefillingTime(ContainerLocation containerLocation, int time)
    {
        if (stopRefillingContainer(containerLocation)
            && containerStorage.updateContainerTime(containerLocation, time)
            && startRefillingContainer(containerLocation, time)) return true;

        return false;
    }


}
