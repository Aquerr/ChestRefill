package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

import static io.github.aquerr.chestrefill.PluginInfo.PLUGIN_PREFIX;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

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

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(shouldPlaceItemsInRandomSlots), (selectionMode, selectionMode2) -> null);

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

    private SelectionParams prepareParams(boolean shouldPlaceItemsInRandomSlots)
    {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("SHOULD_PLACE_ITEMS_IN_RANDOM_SLOTS", shouldPlaceItemsInRandomSlots);
        return new SelectionParams(SelectionMode.SET_PLACE_ITEMS_IN_RANDOM_SLOTS, this::setPlaceItemsInRandomSlots, extraData);
    }

    private void setPlaceItemsInRandomSlots(ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        final RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        final boolean shouldPlaceItemsInRandomSlots = (boolean)params.getExtraData().get("SHOULD_PLACE_ITEMS_IN_RANDOM_SLOTS");
        refillableContainerAtLocation.setShouldPlaceItemsInRandomSlots(shouldPlaceItemsInRandomSlots);
        final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainerAtLocation);
        if(didSucceed)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully updated a refilling container!")));
        }
        else
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, text("Something went wrong...")));
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
