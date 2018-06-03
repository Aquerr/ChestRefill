package io.github.aquerr.chestrefill.entities;

import org.spongepowered.api.item.inventory.ItemStack;

public class RefillableItem
{
    //Chance: 1 = 100%
    private int chance = 1;
    //Item
    private ItemStack item;

    public RefillableItem(ItemStack itemStack)
    {
        this.item = itemStack;
    }

    public RefillableItem(ItemStack itemStack, int chance)
    {
        this.item = itemStack;
        this.chance = chance;
    }

    public ItemStack getItem()
    {
        return item;
    }

    public int getChance()
    {
        return chance;
    }
}
