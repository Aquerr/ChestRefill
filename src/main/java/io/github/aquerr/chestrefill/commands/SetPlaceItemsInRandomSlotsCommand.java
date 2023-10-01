package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.UUID;

public class SetPlaceItemsInRandomSlotsCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public SetPlaceItemsInRandomSlotsCommand(final ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final boolean shouldPlaceItemsInRandomSlots = context.requireOne(Parameter.bool().key("value").build());

        final ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.SELECTION_MODE.compute(serverPlayer.uniqueId(), this::toggleSelectionMode);
        if (ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId()))
        {
            ChestRefill.CONTAINER_PLACE_ITEMS_IN_RANDOM_SLOTS.compute(serverPlayer.uniqueId(), ((uuid, aBoolean) -> shouldPlaceItemsInRandomSlots));
        }

        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.setplaceitemsinrandomslots.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.setplaceitemsinrandomslots.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionMode toggleSelectionMode(UUID uuid, SelectionMode selectionMode)
    {
        return selectionMode == null ? SelectionMode.SET_PLACE_ITEMS_IN_RANDOM_SLOTS : null;
    }
}
