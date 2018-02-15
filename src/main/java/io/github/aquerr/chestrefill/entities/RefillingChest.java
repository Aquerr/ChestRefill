package io.github.aquerr.chestrefill.entities;

import io.github.aquerr.chestrefill.ChestRefill;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class RefillingChest
{
    private ChestLocation chestLocation;
    private List<ItemStack> items;
    private int restoreTimeInSeconds;

    public RefillingChest(ChestLocation chestLocation, List<ItemStack> itemsList)
    {
        this.chestLocation = chestLocation;
        this.items = itemsList;
        this.restoreTimeInSeconds = 120; //Default: 120 sec
    }

    public RefillingChest(ChestLocation chestLocation, List<ItemStack> itemsList, int time)
    {
        this.chestLocation = chestLocation;
        this.items = itemsList;
        this.restoreTimeInSeconds = time;
    }

    public ChestLocation getChestLocation()
    {
        return chestLocation;
    }

    public List<ItemStack> getItems()
    {
        return items;
    }

    public int getRestoreTime() { return restoreTimeInSeconds; }

    public static RefillingChest fromChest(Chest chest, UUID worldUUID)
    {
        //Iterate over items in chest inventory
        List<ItemStack> items = new ArrayList<>();

        chest.getInventory().slots().forEach(x->
        {
            if (x.peek().isPresent())
            {
                items.add(x.peek().get());
            }
        });

        RefillingChest refillingChest = new RefillingChest(new ChestLocation(chest.getLocation().getBlockPosition(), worldUUID), items);

        return refillingChest;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof RefillingChest))
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }


        //TODO: Refactor this code if it will be possible...
        //Compare chest location
        if (this.chestLocation.equals(((RefillingChest)obj).getChestLocation()))
        {
            Inventory tempInventory = Inventory.builder().build(ChestRefill.getChestRefill());

            this.items.forEach(x-> tempInventory.offer(x));

            //Compare items
            for (ItemStack comparedItem : ((RefillingChest) obj).getItems())
            {
                if (!tempInventory.contains(comparedItem))
                {
                    return false;
                }
            }

            //Compare restore time
            if (this.restoreTimeInSeconds == ((RefillingChest)obj).getRestoreTime())
            {
                return true;
            }
        }

        return false;

//        return this.chestLocation.equals(((RefillingChest)obj).getChestLocation()) && this.items.containsAll(((RefillingChest)obj).getItems())
//                && this.restoreTimeInSeconds == ((RefillingChest)obj).getRestoreTime();
    }

    @Override
    public int hashCode()
    {
        return chestLocation.toString().length();
    }
}
