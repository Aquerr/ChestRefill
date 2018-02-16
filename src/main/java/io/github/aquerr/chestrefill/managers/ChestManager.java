package io.github.aquerr.chestrefill.managers;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ChestLocation;
import io.github.aquerr.chestrefill.entities.RefillingChest;
import io.github.aquerr.chestrefill.storage.JSONChestStorage;
import io.github.aquerr.chestrefill.storage.Storage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aquerr on 2018-02-13.
 */
public class ChestManager
{
    private static Storage chestStorage = new JSONChestStorage();

    public static boolean addChest(RefillingChest refillingChest)
    {
        if (chestStorage.addChest(refillingChest))
        {
            return startRefillingChest(refillingChest);
        }

        return false;
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
            Task refillTask = (Task) Sponge.getScheduler().getTasksByName("Chest Refill " + chestLocation.getBlockPosition().toString()
                    + "|" + chestLocation.getWorldUUID().toString()).toArray()[0];
            refillTask.cancel();

            return true;
        }
        catch (Exception exception)
        {

        }
        return false;
    }

    @Nullable
    private static RefillingChest getChest(ChestLocation chestLocation)
    {
        return chestStorage.getChest(chestLocation);
    }

    private static boolean startRefillingChest(RefillingChest refillingChest)
    {
        try
        {
            Task.Builder refillTask = Sponge.getScheduler().createTaskBuilder();

            refillTask.execute(refillChest(refillingChest.getChestLocation())).interval(refillingChest.getRestoreTime(), TimeUnit.SECONDS)
                    .name("Chest Refill " + refillingChest.getChestLocation().getBlockPosition().toString() + "|" + refillingChest.getChestLocation().getWorldUUID().toString())
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
}
