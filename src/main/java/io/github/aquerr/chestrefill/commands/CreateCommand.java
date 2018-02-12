package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class CreateCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Player player = (Player)source;

            if (!ChestRefill.ChestCreationPlayers.contains(player.getUniqueId()))
            {
                ChestRefill.ChestCreationPlayers.add(player.getUniqueId());
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Toggled creation mode"));
            }
            else
            {
                ChestRefill.ChestCreationPlayers.remove(player.getUniqueId());
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned off creation mode"));
            }
        }

        return CommandResult.success();
    }
}
