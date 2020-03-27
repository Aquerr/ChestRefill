package io.github.aquerr.chestrefill.entities;

import org.spongepowered.api.item.inventory.ItemStackSnapshot;

public class RefillableItem
{
    //Chance: 1 = 100%
    private float chance = 1f;

    //Item
    private ItemStackSnapshot item;

    private int slot;

    public RefillableItem()
    {

    }

    public RefillableItem(ItemStackSnapshot itemStack)
    {
        this.item = itemStack;
    }

    public RefillableItem(ItemStackSnapshot itemStack, int slot, float chance)
    {
        this.item = itemStack;
        this.slot = slot;
        this.chance = chance;
    }

    public ItemStackSnapshot getItem()
    {
        return this.item;
    }

    public int getSlot()
    {
        return slot;
    }

    public float getChance()
    {
        return chance;
    }

    @Override
    public String toString()
    {
        return "RefillableItem{" +
                "chance=" + chance +
                ", item=" + item +
                ", slot=" + slot +
                '}';
    }
}
