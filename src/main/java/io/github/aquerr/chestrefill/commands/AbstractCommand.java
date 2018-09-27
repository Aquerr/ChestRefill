package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;

public abstract class AbstractCommand
{
    private final ChestRefill plugin;

    protected AbstractCommand(ChestRefill plugin)
    {
        this.plugin = plugin;
    }

    public ChestRefill getPlugin()
    {
        return plugin;
    }
}
