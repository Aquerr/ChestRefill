package io.github.aquerr.chestrefill.entities;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class Kit
{
    @Setting
    private final String name;

    @Setting
    private final List<RefillableItem> items;

    public Kit(String name, List<RefillableItem> items)
    {
        this.name = name;
        this.items = items;
    }

    public String getName()
    {
        return name;
    }

    public List<RefillableItem> getItems()
    {
        return items;
    }
}