package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;

public class RemoveAllCommand extends AbstractCommand
{
    public RemoveAllCommand(final ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException
    {
        final Collection<ContainerLocation> refillableContainerList = super.getPlugin().getContainerManager().getContainerLocations();
        for (final ContainerLocation containerLocation : refillableContainerList)
        {
            super.getPlugin().getContainerManager().removeRefillableContainer(containerLocation);
        }

        source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "All containers have been removed!"));
        return CommandResult.success();
    }
}
