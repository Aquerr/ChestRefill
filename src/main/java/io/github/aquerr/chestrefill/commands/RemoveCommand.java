package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class RemoveCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public RemoveCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        ServerPlayer serverPlayer = requirePlayerSource(context);
        ChestRefill.PLAYER_CHEST_SELECTION_MODE.merge(serverPlayer.uniqueId(), SelectionMode.REMOVE, (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.remove.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.remove.turned-off"));
        }

        return CommandResult.success();
    }
}
