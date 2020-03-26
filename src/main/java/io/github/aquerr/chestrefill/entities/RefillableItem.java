package io.github.aquerr.chestrefill.entities;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.item.inventory.ItemStack;

@ConfigSerializable
public class RefillableItem
{
    //Chance: 1 = 100%
    @Setting(comment = "Chance for this item to regenerate")
    private float chance = 1f;

    //Item
    @Setting
    private ItemStack item;

    @Setting
    private int slot;

    public RefillableItem()
    {

    }

    public RefillableItem(ItemStack itemStack)
    {
        this.item = itemStack;
    }

    public RefillableItem(ItemStack itemStack, int slot, float chance)
    {
        this.item = itemStack;
        this.slot = slot;
        this.chance = chance;
    }

    public ItemStack getItem()
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
