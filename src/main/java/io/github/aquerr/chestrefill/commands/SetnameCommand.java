package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class SetnameCommand extends AbstractCommand implements CommandExecutor
{
    public SetnameCommand(ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalName = context.getOne(Text.of("name"));

        if(!(source instanceof Player))
        {
            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Only in-game players can use this command!"));
            return CommandResult.success();
        }

        if(!optionalName.isPresent())
        {
            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "You need to specify a name!"));
            return CommandResult.success();
        }

        Player player = (Player)source;
        String containerName = optionalName.get();

        if (ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.getUniqueId()))
        {
            if (SelectionMode.SET_NAME != ChestRefill.PLAYER_CHEST_SELECTION_MODE.get(player.getUniqueId()))
            {
                ChestRefill.PLAYER_CHEST_SELECTION_MODE.replace(player.getUniqueId(), SelectionMode.SET_NAME);
                ChestRefill.PLAYER_CHEST_NAME.put(player.getUniqueId(), containerName);
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned on setname mode"));
            }
            else
            {
                ChestRefill.PLAYER_CHEST_SELECTION_MODE.remove(player.getUniqueId());
                ChestRefill.PLAYER_CHEST_NAME.remove(player.getUniqueId());
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned off setname mode"));
            }
        }
        else
        {
            ChestRefill.PLAYER_CHEST_SELECTION_MODE.put(player.getUniqueId(), SelectionMode.SET_NAME);
            ChestRefill.PLAYER_CHEST_NAME.put(player.getUniqueId(), containerName);
            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned on setname mode"));
        }

        return CommandResult.success();
    }
}
