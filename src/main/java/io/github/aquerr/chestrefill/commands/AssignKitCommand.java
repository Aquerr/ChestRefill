package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.SelectionMode;
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

public class AssignKitCommand extends AbstractCommand implements CommandExecutor
{
    public AssignKitCommand(ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final Optional<String> kitName = context.getOne(Text.of("kit name"));

        if(!(source instanceof Player))
        {
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Only in-game players can use this command!"));
            return CommandResult.empty();
        }

        final Map<String, Kit> kits = super.getPlugin().getContainerManager().getKits();
        if(kits.keySet().stream().noneMatch(x->x.equals(kitName.get())))
        {
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Kit with such name does not exists!"));
            return CommandResult.empty();
        }

        final Player player = (Player)source;
        if (ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.getUniqueId()))
        {
            if (SelectionMode.ASSIGN_KIT != ChestRefill.PLAYER_CHEST_SELECTION_MODE.get(player.getUniqueId()))
            {
                ChestRefill.PLAYER_KIT_ASSIGN.put(player.getUniqueId(), kitName.get());
                ChestRefill.PLAYER_CHEST_SELECTION_MODE.replace(player.getUniqueId(), SelectionMode.ASSIGN_KIT);
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned on assign mode"));
            }
            else
            {
                ChestRefill.PLAYER_KIT_ASSIGN.remove(player.getUniqueId());
                ChestRefill.PLAYER_CHEST_SELECTION_MODE.remove(player.getUniqueId());
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned off assign mode"));
            }
        }
        else
        {
            ChestRefill.PLAYER_KIT_ASSIGN.put(player.getUniqueId(), kitName.get());
            ChestRefill.PLAYER_CHEST_SELECTION_MODE.put(player.getUniqueId(), SelectionMode.ASSIGN_KIT);
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Turned on assign mode"));
        }

        return CommandResult.success();
    }
}
