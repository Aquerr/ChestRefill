package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.Collection;

public class RefillCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public RefillCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        String containerName = context.requireOne(Parameter.string().key("name").build());
        Collection<RefillableContainer> containers = super.getPlugin().getContainerManager().getRefillableContainers();

        for (RefillableContainer refillableContainer : containers)
        {
            if (refillableContainer.getName() != null && refillableContainer.getName().equals(containerName))
            {
                boolean didSucceed = super.getPlugin().getContainerManager().refillContainer(refillableContainer.getContainerLocation());
                if(didSucceed)
                {
                    context.cause().audience().sendMessage(messageSource.resolveMessageWithPrefix("command.refill.success"));
                    break;
                }
                else
                {
                    throw messageSource.resolveExceptionWithMessage("command.refill.failure");
                }
            }
        }
        return CommandResult.success();
    }
}
