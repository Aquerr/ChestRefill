package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestMode;
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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Aquerr on 2018-02-17.
 */
public class TimeCommand implements CommandExecutor
{

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Integer> optionalTime = context.<Integer>getOne(Text.of("time"));

        if (source instanceof Player)
        {
            Player player = (Player)source;

            if (ChestRefill.PlayersChestMode.containsKey(player.getUniqueId()))
            {
                if (ChestMode.TIME != ChestRefill.PlayersChestMode.get(player.getUniqueId()))
                {
                    ChestRefill.PlayersChestMode.replace(player.getUniqueId(), ChestMode.TIME);
                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned on time mode"));
                }
                else
                {
                    ChestRefill.PlayersChestMode.remove(player.getUniqueId());
                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned off time mode"));
                }
            }
            else
            {
                ChestRefill.PlayersChestMode.put(player.getUniqueId(), ChestMode.TIME);
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned on time mode"));
            }

            if (ChestRefill.PlayersChestMode.containsKey(player.getUniqueId())
                    && ChestRefill.PlayersChestMode.get(player.getUniqueId()) == ChestMode.TIME)
            {
                if (optionalTime.isPresent())
                {
                    if (ChestRefill.ChestTimeChangePlayer.containsKey(player.getUniqueId()))
                    {
                        ChestRefill.ChestTimeChangePlayer.replace(player.getUniqueId(), optionalTime.get());
                    }
                    else
                    {
                        ChestRefill.ChestTimeChangePlayer.put(player.getUniqueId(), optionalTime.get());
                    }
                }
            }
            else
            {
                if (ChestRefill.ChestTimeChangePlayer.containsKey(player.getUniqueId()))
                {
                    ChestRefill.ChestTimeChangePlayer.remove(player.getUniqueId());
                    Map<UUID, ChestMode> test = ChestRefill.ChestTimeChangePlayer; 
                }
            }
        }

        return CommandResult.success();
    }
}
