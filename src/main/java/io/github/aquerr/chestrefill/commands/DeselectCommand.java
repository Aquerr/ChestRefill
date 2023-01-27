package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionPoints;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class DeselectCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public DeselectCommand(final ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = getPlugin().getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerPlayer serverPlayer = requirePlayerSource(context);
        final SelectionPoints selectionPoints = ChestRefill.PLAYER_SELECTION_POINTS.get(serverPlayer.uniqueId());
        if (selectionPoints != null)
        {
            selectionPoints.setFirstPoint(null);
            selectionPoints.setSecondPoint(null);
        }
        ChestRefill.PLAYER_SELECTION_POINTS.remove(serverPlayer.uniqueId());
        serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.deselect.selection-points-cleared-out"));
        return CommandResult.success();
    }
}
