package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import java.util.Collection;

public class RemoveAllCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public RemoveAllCommand(final ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Collection<ContainerLocation> refillableContainerList = super.getPlugin().getContainerManager().getContainerLocations();
        for (final ContainerLocation containerLocation : refillableContainerList)
        {
            super.getPlugin().getContainerManager().removeRefillableContainer(containerLocation);
        }

        context.sendMessage(Identity.nil(), messageSource.resolveMessageWithPrefix("command.removeall.success"));
        return CommandResult.success();
    }
}
