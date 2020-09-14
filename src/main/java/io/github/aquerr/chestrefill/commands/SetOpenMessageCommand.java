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

import java.util.Optional;

public class SetOpenMessageCommand extends AbstractCommand
{
    public SetOpenMessageCommand(final ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, CommandContext args) throws CommandException
    {
        String message = args.requireOne(Text.of("message"));

        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Only in-game players can use this command!"));

        Player player = (Player)source;

        if (ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.getUniqueId()))
        {
            if (SelectionMode.SET_OPEN_MESSAGE != ChestRefill.PLAYER_CHEST_SELECTION_MODE.get(player.getUniqueId()))
            {
                ChestRefill.PLAYER_CHEST_SELECTION_MODE.replace(player.getUniqueId(), SelectionMode.SET_OPEN_MESSAGE);
                ChestRefill.PLAYER_CHEST_NAME.put(player.getUniqueId(), message);
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned on set open message mode"));
            }
            else
            {
                ChestRefill.PLAYER_CHEST_SELECTION_MODE.remove(player.getUniqueId());
                ChestRefill.PLAYER_CHEST_NAME.remove(player.getUniqueId());
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned off set open message mode"));
            }
        }
        else
        {
            ChestRefill.PLAYER_CHEST_SELECTION_MODE.put(player.getUniqueId(), SelectionMode.SET_OPEN_MESSAGE);
            ChestRefill.PLAYER_CHEST_NAME.put(player.getUniqueId(), message);
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned on set open message mode"));
        }


        return CommandResult.success();
    }
}
