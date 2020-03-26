package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.entities.SelectionMode;
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

            if (ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.getUniqueId()))
            {
                if (SelectionMode.TIME != ChestRefill.PLAYER_CHEST_SELECTION_MODE.get(player.getUniqueId()))
                {
                    ChestRefill.PLAYER_CHEST_SELECTION_MODE.replace(player.getUniqueId(), SelectionMode.TIME);
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned on time mode"));
                }
                else
                {
                    ChestRefill.PLAYER_CHEST_SELECTION_MODE.remove(player.getUniqueId());
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned off time mode"));
                }
            }
            else
            {
                ChestRefill.PLAYER_CHEST_SELECTION_MODE.put(player.getUniqueId(), SelectionMode.TIME);
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned on time mode"));
            }

            if (ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.getUniqueId())
                    && ChestRefill.PLAYER_CHEST_SELECTION_MODE.get(player.getUniqueId()) == SelectionMode.TIME)
            {
                if (optionalTime.isPresent())
                {
                    if (ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.containsKey(player.getUniqueId()))
                    {
                        ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.replace(player.getUniqueId(), optionalTime.get());
                    }
                    else
                    {
                        ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.put(player.getUniqueId(), optionalTime.get());
                    }
                }
            }
            else if (ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.containsKey(player.getUniqueId()))
            {
                ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.remove(player.getUniqueId());
            }
        }

        return CommandResult.success();
    }
}
