package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.UUID;

public class SetPlaceItemsInRandomSlotsCommand extends AbstractCommand
{
    public SetPlaceItemsInRandomSlotsCommand(final ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException
    {
        final boolean shouldPlaceItemsInRandomSlots = args.requireOne(Text.of("value"));

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Only in-game players can use this command!"));

        Player player = (Player) source;
        ChestRefill.PLAYER_CHEST_SELECTION_MODE.compute(player.getUniqueId(), this::toggleSelectionMode);
        if (ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.getUniqueId()))
        {
            ChestRefill.CONTAINER_PLACE_ITEMS_IN_RANDOM_SLOTS.compute(player.getUniqueId(), ((uuid, aBoolean) -> shouldPlaceItemsInRandomSlots));
        }

        boolean isModeActive = ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.getUniqueId());
        if (isModeActive)
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned on set place items in random slots mode"));
        }
        else
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned off set place items in random slots mode"));
        }

        return CommandResult.success();
    }

    private SelectionMode toggleSelectionMode(UUID uuid, SelectionMode selectionMode)
    {
        return selectionMode == null ? SelectionMode.SET_PLACE_ITEMS_IN_RANDOM_SLOTS : null;
    }
}
