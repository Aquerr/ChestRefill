package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.SelectionMode;
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

import java.util.Optional;

/**
 * Created by Aquerr on 2018-02-17.
 */
public class TimeCommand extends AbstractCommand implements CommandExecutor
{

    public TimeCommand(ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<Integer> optionalTime = context.<Integer>getOne(Text.of("time"));

        if (source instanceof Player)
        {
            Player player = (Player)source;

            if (ChestRefill.PlayersSelectionMode.containsKey(player.getUniqueId()))
            {
                if (SelectionMode.TIME != ChestRefill.PlayersSelectionMode.get(player.getUniqueId()))
                {
                    ChestRefill.PlayersSelectionMode.replace(player.getUniqueId(), SelectionMode.TIME);
                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned on time mode"));
                }
                else
                {
                    ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());
                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned off time mode"));
                }
            }
            else
            {
                ChestRefill.PlayersSelectionMode.put(player.getUniqueId(), SelectionMode.TIME);
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned on time mode"));
            }

            if (ChestRefill.PlayersSelectionMode.containsKey(player.getUniqueId())
                    && ChestRefill.PlayersSelectionMode.get(player.getUniqueId()) == SelectionMode.TIME)
            {
                if (optionalTime.isPresent())
                {
                    if (ChestRefill.ContainerTimeChangePlayer.containsKey(player.getUniqueId()))
                    {
                        ChestRefill.ContainerTimeChangePlayer.replace(player.getUniqueId(), optionalTime.get());
                    }
                    else
                    {
                        ChestRefill.ContainerTimeChangePlayer.put(player.getUniqueId(), optionalTime.get());
                    }
                }
            }
            else if (ChestRefill.ContainerTimeChangePlayer.containsKey(player.getUniqueId()))
            {
                ChestRefill.ContainerTimeChangePlayer.remove(player.getUniqueId());
            }
        }

        return CommandResult.success();
    }
}
