package io.github.aquerr.chestrefill.entities;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class Kit
{
    @Setting
    private String name;

    @Setting
    private List<RefillableItem> items;

    public Kit()
    {

    }

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

    @Override
    public String toString()
    {
        return "Kit{" +
                "name='" + name + '\'' +
                ", items=" + items +
                '}';
    }
}