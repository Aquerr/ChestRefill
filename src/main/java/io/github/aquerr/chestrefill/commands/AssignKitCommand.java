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

import java.util.List;
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
        if(!(source instanceof Player))
        {
            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Only in-game players can use this command!"));
            return CommandResult.empty();
        }

        Optional<String> optionalName = context.getOne(Text.of("kit name"));
        if(!optionalName.isPresent())
        {
            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "You must specify a kit name!"));
            return CommandResult.empty();
        }

        List<Kit> kits = super.getPlugin().getContainerManager().getKits();
        if(kits.stream().noneMatch(x->x.getName().equals(optionalName.get())))
        {
            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Kit with such name does not exists!"));
            return CommandResult.empty();
        }

        Player player = (Player)source;
        if (ChestRefill.PlayersSelectionMode.containsKey(player.getUniqueId()))
        {
            if (SelectionMode.ASSIGN_KIT != ChestRefill.PlayersSelectionMode.get(player.getUniqueId()))
            {
                optionalName.ifPresent(s -> ChestRefill.PlayerKitAssign.put(player.getUniqueId(), s));
                ChestRefill.PlayersSelectionMode.replace(player.getUniqueId(), SelectionMode.ASSIGN_KIT);
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned on assign mode"));
            }
            else
            {
                ChestRefill.PlayerKitAssign.remove(player.getUniqueId());
                ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());
                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned off assign mode"));
            }
        }
        else
        {
            optionalName.ifPresent(s -> ChestRefill.PlayerKitAssign.put(player.getUniqueId(), s));
            ChestRefill.PlayersSelectionMode.put(player.getUniqueId(), SelectionMode.ASSIGN_KIT);
            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Turned on assign mode"));
        }

        return CommandResult.success();
    }
}
