package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import org.spongepowered.api.command.spec.CommandExecutor;

public abstract class AbstractCommand implements CommandExecutor
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
