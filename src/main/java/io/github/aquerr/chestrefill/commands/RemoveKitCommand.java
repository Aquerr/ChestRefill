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

public class RemoveKitCommand extends AbstractCommand implements CommandExecutor
{
    public RemoveKitCommand(ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        Optional<String> optionalName = context.getOne(Text.of("kit name"));

        if(!optionalName.isPresent())
        {
            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "You must specify a kit name!"));
            return CommandResult.empty();
        }

        List<Kit> kits = super.getPlugin().getContainerManager().getKits();

        if(!kits.stream().anyMatch(x->x.getName().equals(optionalName.get())))
        {
            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Kit with given name does not exists!"));
            return CommandResult.empty();
        }

        boolean didSucceed = super.getPlugin().getContainerManager().removeKit(optionalName.get());
        if (didSucceed)
        {
            source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully removed the kit!"));
        }
        else source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));

        return CommandResult.success();
    }
}
