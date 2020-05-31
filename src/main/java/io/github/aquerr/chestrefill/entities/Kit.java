package io.github.aquerr.chestrefill.entities;

import java.util.List;

public class Kit
{
    private String name;

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