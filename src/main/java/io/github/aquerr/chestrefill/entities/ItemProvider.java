package io.github.aquerr.chestrefill.entities;

import java.util.Objects;

public class ItemProvider
{
    private final ItemProviderType type;
    private final String location;

    public static ItemProvider kit(String name)
    {
        return ItemProvider.kit(name);
    }

    public static ItemProvider self()
    {
        return new ItemProvider(ItemProviderType.SELF, null);
    }

    public static ItemProvider lootTable(String location)
    {
        return new ItemProvider(ItemProviderType.LOOT_TABLE, location);
    }

    public ItemProvider(ItemProviderType type, String location)
    {
        this.type = type;
        this.location = location;
    }

    public ItemProviderType getType()
    {
        return type;
    }

    public String getLocation()
    {
        return location;
    }

    @Override
    public String toString()
    {
        return "ItemProvider{" +
                "type=" + type +
                ", location='" + location + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemProvider that = (ItemProvider) o;
        return type == that.type && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, location);
    }
}
