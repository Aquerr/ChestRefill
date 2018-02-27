package io.github.aquerr.chestrefill.managers;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ChestLocation;
import io.github.aquerr.chestrefill.entities.RefillingChest;
import io.github.aquerr.chestrefill.storage.JSONChestStorage;
import io.github.aquerr.chestrefill.storage.Storage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aquerr on 2018-02-13.
 */
public class ChestManager
{
    private static Storage chestStorage;

    public static void setupChestManager(Path configDir)
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

        chestStorage = new JSONChestStorage(configDir);
    }

    public static boolean addChest(RefillingChest refillingChest)
    {
        if (chestStorage.addOrUpdateChest(refillingChest))
        {
            return startRefillingChest(refillingChest.getChestLocation(), refillingChest.getRestoreTime());
        }

        return false;
    }

    public static boolean updateChest(RefillingChest refillingChest)
    {
        //We do not need to restart scheduler. New chest content will be loaded from the storage by existing scheduler.
        return chestStorage.addOrUpdateChest(refillingChest);
    }

    public static List<RefillingChest> getChests()
    {
        return chestStorage.getChests();
    }

    public static boolean removeChest(ChestLocation chestLocation)
    {
        if (chestStorage.removeChest(chestLocation))
        {
            return stopRefillingChest(chestLocation);
        }

        return false;
    }

    private static boolean stopRefillingChest(ChestLocation chestLocation)
    {
        try
        {
            Optional<Task> optionalTask = Sponge.getScheduler().getScheduledTasks().stream().filter(x->x.getName().equals("Chest Refill " + chestLocation.getBlockPosition().toString()
                    + "|" + chestLocation.getWorldUUID().toString())).findFirst();

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
    private static RefillingChest getChest(ChestLocation chestLocation)
    {
        return chestStorage.getChest(chestLocation);
    }

    private static boolean startRefillingChest(ChestLocation chestLocation, int time)
    {
        try
        {
            Task.Builder refillTask = Sponge.getScheduler().createTaskBuilder();

            refillTask.execute(refillChest(chestLocation)).interval(time, TimeUnit.SECONDS)
                    .name("Chest Refill " + chestLocation.getBlockPosition().toString() + "|" + chestLocation.getWorldUUID().toString())
                    .submit(ChestRefill.getChestRefill());

            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

    private static Runnable refillChest(ChestLocation refillingChest)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                RefillingChest chestToRefill = getChest(refillingChest);

                Optional<World> world =  Sponge.getServer().getWorld(chestToRefill.getChestLocation().getWorldUUID());

                if (world.isPresent())
                {
                    Location location = new Location(world.get(), chestToRefill.getChestLocation().getBlockPosition());

                    if (location.getTileEntity().isPresent())
                    {
                        Chest chest = (Chest) location.getTileEntity().get();

                        chest.getInventory().clear();
                        for (ItemStack itemStack : chestToRefill.getItems())
                        {
                            chest.getInventory().offer(itemStack);
                        }
                    }
                }
            }
        };
    }

    public static void restoreRefilling()
    {
        for (RefillingChest refillingChest : getChests())
        {
            Task.Builder refilling = Sponge.getScheduler().createTaskBuilder();

            refilling.execute(refillChest(refillingChest.getChestLocation())).interval(refillingChest.getRestoreTime(), TimeUnit.SECONDS)
                    .name("Chest Refill " + refillingChest.getChestLocation().getBlockPosition().toString() + "|" + refillingChest.getChestLocation().getWorldUUID().toString())
                    .submit(ChestRefill.getChestRefill());
        }
    }

    public static boolean updateChestTime(ChestLocation chestLocation, int time)
    {
        if (stopRefillingChest(chestLocation)
            && chestStorage.updateChestTime(chestLocation, time)
            && startRefillingChest(chestLocation, time)) return true;

        return false;
    }
}
