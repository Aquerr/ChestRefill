package io.github.aquerr.chestrefill.managers;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.TileEntityLocation;
import io.github.aquerr.chestrefill.entities.RefillableTileEntity;
import io.github.aquerr.chestrefill.storage.JSONStorage;
import io.github.aquerr.chestrefill.storage.Storage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by Aquerr on 2018-02-13.
 */
public class ChestManager
{
    public static List<TileEntityType> allowedTileEntityTypes = new ArrayList<>();

    private static Storage refillableEntityStorage;

    public static void setupChestManager(Path configDir)
    {
        allowedTileEntityTypes.add(TileEntityTypes.CHEST);
        allowedTileEntityTypes.add(TileEntityTypes.DISPENSER);
        allowedTileEntityTypes.add(TileEntityTypes.DROPPER);
        allowedTileEntityTypes.add(TileEntityTypes.HOPPER);

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

        refillableEntityStorage = new JSONStorage(configDir);
    }

    public static boolean addRefillableTileEntity(RefillableTileEntity refillableTileEntity)
    {
        if (refillableEntityStorage.addOrUpdateRefillableEntity(refillableTileEntity))
        {
            return startRefillingEntity(refillableTileEntity.getTileEntityLocation(), refillableTileEntity.getRestoreTime());
        }

        return false;
    }

    public static boolean updateRefillableEntity(RefillableTileEntity refillableTileEntity)
    {
        //We do not need to restart scheduler. New chest content will be loaded from the storage by existing scheduler.
        return refillableEntityStorage.addOrUpdateRefillableEntity(refillableTileEntity);
    }

    public static List<RefillableTileEntity> getRefillableTileEntities()
    {
        return refillableEntityStorage.getRefillableEntities();
    }

    public static boolean removeChest(TileEntityLocation tileEntityLocation)
    {
        if (refillableEntityStorage.removeRefillableEntity(tileEntityLocation))
        {
            return stopRefillingEntity(tileEntityLocation);
        }

        return false;
    }

    private static boolean stopRefillingEntity(TileEntityLocation tileEntityLocation)
    {
        try
        {
            Optional<Task> optionalTask = Sponge.getScheduler().getScheduledTasks().stream().filter(x->x.getName().equals("Chest Refill " + tileEntityLocation.getBlockPosition().toString()
                    + "|" + tileEntityLocation.getWorldUUID().toString())).findFirst();

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
    private static RefillableTileEntity getRefillableTileEntity(TileEntityLocation tileEntityLocation)
    {
        return refillableEntityStorage.getRefillableEntity(tileEntityLocation);
    }

    private static boolean startRefillingEntity(TileEntityLocation tileEntityLocation, int time)
    {
        try
        {
            Task.Builder refillTask = Sponge.getScheduler().createTaskBuilder();

            refillTask.execute(refillTileEntity(tileEntityLocation)).delay(time, TimeUnit.SECONDS)
                    .name("Chest Refill " + tileEntityLocation.getBlockPosition().toString() + "|" + tileEntityLocation.getWorldUUID().toString())
                    .submit(ChestRefill.getChestRefill());

            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            return false;
        }
    }

    private static Runnable refillTileEntity(TileEntityLocation tileEntityLocation)
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                RefillableTileEntity chestToRefill = getRefillableTileEntity(tileEntityLocation);

                Optional<World> world =  Sponge.getServer().getWorld(chestToRefill.getTileEntityLocation().getWorldUUID());

                if (world.isPresent())
                {
                    Location location = new Location(world.get(), chestToRefill.getTileEntityLocation().getBlockPosition());

                    if (location.getTileEntity().isPresent())
                    {
                        TileEntityCarrier chest = (TileEntityCarrier) location.getTileEntity().get();

                        chest.getInventory().clear();
                        for (ItemStack itemStack : chestToRefill.getItems())
                        {
                            chest.getInventory().offer(itemStack);
                        }
                    }
                }

                Task.Builder refillTask = Sponge.getScheduler().createTaskBuilder();

                refillTask.execute(refillTileEntity(chestToRefill.getTileEntityLocation())).delay(chestToRefill.getRestoreTime(), TimeUnit.SECONDS)
                        .name("Chest Refill " + chestToRefill.getTileEntityLocation().getBlockPosition().toString() + "|" + chestToRefill.getTileEntityLocation().getWorldUUID().toString())
                        .submit(ChestRefill.getChestRefill());
            }
        };
    }

    public static void restoreRefilling()
    {
        for (RefillableTileEntity refillableTileEntity : getRefillableTileEntities())
        {
            Task.Builder refilling = Sponge.getScheduler().createTaskBuilder();

            refilling.execute(refillTileEntity(refillableTileEntity.getTileEntityLocation())).delay(refillableTileEntity.getRestoreTime(), TimeUnit.SECONDS)
                    .name("Chest Refill " + refillableTileEntity.getTileEntityLocation().getBlockPosition().toString() + "|" + refillableTileEntity.getTileEntityLocation().getWorldUUID().toString())
                    .submit(ChestRefill.getChestRefill());
        }
    }

    public static boolean updateRefillingTime(TileEntityLocation tileEntityLocation, int time)
    {
        if (stopRefillingEntity(tileEntityLocation)
            && refillableEntityStorage.updateEntityTime(tileEntityLocation, time)
            && startRefillingEntity(tileEntityLocation, time)) return true;

        return false;
    }
}
