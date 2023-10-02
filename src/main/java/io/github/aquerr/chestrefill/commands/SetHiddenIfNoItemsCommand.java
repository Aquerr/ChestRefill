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

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(hiddenIfNoItems), (selectionMode, selectionMode2) -> null);

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

    private SelectionParams prepareParams(boolean hiddenIfNoItems)
    {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("HIDDEN_IF_NO_ITEMS", hiddenIfNoItems);
        return new SelectionParams(SelectionMode.SET_HIDDEN_IF_NO_ITEMS, this::setHiddenIfNoItems, extraData);
    }

    private void setHiddenIfNoItems(ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        final boolean hiddenIfNoItems = (boolean)params.getExtraData().get("HIDDEN_IF_NO_ITEMS");
        final RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        refillableContainerAtLocation.setHiddenIfNoItems(hiddenIfNoItems);

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
