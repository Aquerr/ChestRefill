package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class TimeCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public TimeCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        Integer time = context.one(Parameter.integerNumber().key("time").build()).orElse(null);
        ServerPlayer serverPlayer = requirePlayerSource(context);
        
        ChestRefill.PLAYER_CHEST_SELECTION_MODE.merge(serverPlayer.uniqueId(), SelectionMode.TIME, (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            if (time == null)
            {
                ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.remove(serverPlayer.uniqueId());
            }
            else
            {
                ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.put(serverPlayer.uniqueId(), time);
            }
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.time.turned-on"));
        }
        else
        {
            ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.remove(serverPlayer.uniqueId());
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.time.turned-off"));
        }

        return CommandResult.success();
    }
}
