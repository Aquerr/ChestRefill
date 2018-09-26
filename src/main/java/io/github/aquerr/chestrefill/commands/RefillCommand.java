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

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Aquerr on 2018-06-24.
 */
public class RefillCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException
    {
        Optional<String> optionalChestName = args.getOne(Text.of("chest name"));

        if (optionalChestName.isPresent())
        {
            String chestName = optionalChestName.get();

            Collection<RefillableContainer> containerList = ContainerManager.getRefillableContainers();

            for (RefillableContainer refillableContainer : containerList)
            {
                if (refillableContainer.getName() != null && refillableContainer.getName().equals(chestName))
                {
                    boolean didSucceed = ContainerManager.refillContainer(refillableContainer.getContainerLocation());
                    if(didSucceed)
                        source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Successfully refilled the container!"));
                    else
                        source.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Could not refill the container!"));
                    break;
                }
            }
        }

        return CommandResult.success();
    }
}
