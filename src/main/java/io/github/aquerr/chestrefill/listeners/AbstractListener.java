package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;

public abstract class AbstractListener
{
    private final ChestRefill plugin;

    protected AbstractListener(ChestRefill plugin)
    {
        this.plugin = plugin;
    }

    public ChestRefill getPlugin()
    {
        return plugin;
    }
}
