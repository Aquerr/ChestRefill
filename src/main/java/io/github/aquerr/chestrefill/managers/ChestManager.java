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

    public static boolean removeChest(RefillingChest refillingChest)
    {
        return chestStorage.removeChest(refillingChest);
    }

    private static boolean startRefillingChest(RefillingChest refillingChest)
    {
        try
        {
            Task.Builder refillTask = Sponge.getScheduler().createTaskBuilder();

            refillTask.execute(refillChest(refillingChest)).interval(refillingChest.getRestoreTime(), TimeUnit.SECONDS)
                    .name(refillingChest.getChestLocation().getBlockPosition().toString() + "|" + refillingChest.getChestLocation().getWorldUUID().toString())
                    .submit(ChestRefill.getChestRefill());

            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

    private static Runnable refillChest(RefillingChest refillingChest)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                Optional<World> world =  Sponge.getServer().getWorld(refillingChest.getChestLocation().getWorldUUID());

                if (world.isPresent())
                {
                    Location location = new Location(world.get(), refillingChest.getChestLocation().getBlockPosition());

                    if (location.getTileEntity().isPresent())
                    {
                        Chest chest = (Chest) location.getTileEntity().get();

                        chest.getInventory().clear();
                        refillingChest.getItems().forEach(x-> chest.getInventory().offer(x));
                        String test = "";
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

            refilling.execute(refillChest(refillingChest)).interval(refillingChest.getRestoreTime(), TimeUnit.SECONDS)
                    .name(refillingChest.getChestLocation().getBlockPosition().toString() + "|" + refillingChest.getChestLocation().getWorldUUID().toString())
                    .submit(ChestRefill.getChestRefill());
        }
    }
}
