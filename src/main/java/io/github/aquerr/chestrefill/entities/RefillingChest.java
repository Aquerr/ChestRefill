package io.github.aquerr.chestrefill.entities;

import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;

import java.util.List;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class RefillingChest
{
    private Location _chestLocation;
    private List<ItemStack> _items;

    public RefillingChest(Location location, List<ItemStack> itemsList)
    {
        _chestLocation = location;
        _items = itemsList;
    }

    public Location getChestLocation()
    {
        return _chestLocation;
    }

    public List<ItemStack> getItems()
    {
        return _items;
    }
}
