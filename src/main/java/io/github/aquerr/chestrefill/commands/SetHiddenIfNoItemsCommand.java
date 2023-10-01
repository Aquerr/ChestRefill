package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class SetHiddenIfNoItemsCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public SetHiddenIfNoItemsCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final boolean hiddenIfNoItems = context.requireOne(Parameter.bool().key("value").build());

        final ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), SelectionMode.SET_HIDDEN_IF_NO_ITEMS, (selectionMode, selectionMode2) -> null);
        ChestRefill.CONTAINER_HIDDEN_IF_NO_ITEMS.merge(serverPlayer.uniqueId(), hiddenIfNoItems, (s, s2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.sethiddenifnoitems.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.sethiddenifnoitems.turned-off"));
        }

        return CommandResult.success();
    }
}
