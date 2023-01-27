package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

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

    protected ServerPlayer requirePlayerSource(CommandContext context) throws CommandException
    {
        if(!isServerPlayer(context.cause().audience()))
            throw this.plugin.getMessageSource().resolveExceptionWithMessage("error.command.in-game-player-required");
        return(ServerPlayer) context.cause().audience();
    }

    protected boolean isServerPlayer(Audience audience)
    {
        return audience instanceof ServerPlayer;
    }
}
