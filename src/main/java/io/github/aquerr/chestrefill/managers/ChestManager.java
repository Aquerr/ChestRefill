package io.github.aquerr.chestrefill.managers;

import io.github.aquerr.chestrefill.entities.RefillingChest;
import io.github.aquerr.chestrefill.storage.JSONChestStorage;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aquerr on 2018-02-13.
 */
public class ChestManager
{
    public static boolean addChest(RefillingChest refillingChest)
    {
        return JSONChestStorage.addChest(refillingChest);
    }

    public static List<RefillingChest> getChests()
    {
        return JSONChestStorage.getChests();
    }

    public static RefillingChest toRefillingChest(Chest chest)
    {
        //Iritate over items in chest inventory
        List<ItemStack> items = new ArrayList<>();

        chest.getInventory().slots().forEach(x->
        {
            if (x.peek().isPresent())
            {
                items.add(x.peek().get());
            }
        });

        RefillingChest refillingChest = new RefillingChest(chest.getLocation(), items);

        return refillingChest;
    }
}
