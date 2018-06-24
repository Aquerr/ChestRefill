package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.PluginInfo;
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
        List<RefillableContainer> refillableContainerList = ContainerManager.getRefillableContainers();

        //Getting all refillable chests is pointless if we are getting them again in the "refillContainer" method.
        //TODO: Rework refillContainer method, create "getRefillableContainersLocations" or check if it necessary
        //TODO: to get every chest again in "refillContainer".
        for (RefillableContainer refillableContainer : refillableContainerList)
        {
            ContainerManager.refillContainer(refillableContainer.getContainerLocation());
        }

        source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Refilled all containers!"));

        return CommandResult.success();
    }
}
