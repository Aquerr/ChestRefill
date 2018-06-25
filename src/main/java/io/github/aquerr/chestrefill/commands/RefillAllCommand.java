package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;

public class RefillAllCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException
    {
        for (ContainerLocation containerLocation : ContainerManager.getContainerLocations())
        {
            Runnable refillContainer = ContainerManager.refillContainer(containerLocation);
            refillContainer.run();
        }

        source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Refilled all containers!"));

        return CommandResult.success();
    }
}
