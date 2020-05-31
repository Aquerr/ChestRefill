package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by Aquerr on 2018-06-24.
 */
public class RefillCommand extends AbstractCommand implements CommandExecutor
{
    public RefillCommand(ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException
    {
        Optional<String> optionalChestName = args.getOne(Text.of("chest name"));

        if (optionalChestName.isPresent())
        {
            String chestName = optionalChestName.get();

            Collection<RefillableContainer> containerList = super.getPlugin().getContainerManager().getRefillableContainers();

            for (RefillableContainer refillableContainer : containerList)
            {
                if (refillableContainer.getName() != null && refillableContainer.getName().equals(chestName))
                {
                    boolean didSucceed = super.getPlugin().getContainerManager().refillContainer(refillableContainer.getContainerLocation());
                    if(didSucceed)
                        source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Successfully refilled the container!"));
                    else
                        source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Could not refill the container!"));
                    break;
                }
            }
        }

        return CommandResult.success();
    }
}
