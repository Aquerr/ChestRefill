package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class CopyCommand extends AbstractCommand implements CommandExecutor
{
    private final MessageSource messageSource;

    public CopyCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.PLAYER_CHEST_SELECTION_MODE.merge(serverPlayer.uniqueId(), SelectionMode.COPY, (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.copy.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.copy.turned-off"));
        }

        return CommandResult.success();
    }
}
