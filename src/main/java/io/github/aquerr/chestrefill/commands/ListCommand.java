package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.RefillingChest;
import io.github.aquerr.chestrefill.managers.ChestManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ListCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException
    {
        if (source instanceof Player)
        {

            Player player = (Player) source;
            player.sendMessage(Text.of(TextColors.GREEN,"This is a list of all chests: "));
            for(RefillingChest refillingChest:ChestManager.getChests())
            {
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED,refillingChest.getChestLocation().getBlockPosition().toString() ));
            }

        }
        return null;
    }
}
