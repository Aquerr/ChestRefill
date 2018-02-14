package io.github.aquerr.chestrefill.entities;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;

import java.util.List;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class RefillingChest
{
    private ChestLocation chestLocation;
    private List<ItemStack> items;

    public RefillingChest(ChestLocation chestLocation, List<ItemStack> itemsList)
    {
        this.chestLocation = chestLocation;
        this.items = itemsList;
    }

    public ChestLocation getChestLocation()
    {
        return chestLocation;
    }

    public List<ItemStack> getItems()
    {
        return items;
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

        return this.chestLocation.equals(((RefillingChest)obj).chestLocation) && this.items.equals(((RefillingChest)obj).items);
    }

    @Override
    public int hashCode()
    {
        return chestLocation.hashCode();
    }
}
