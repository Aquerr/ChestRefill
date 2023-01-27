package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

public class RefillAllCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public RefillAllCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        boolean didSucceed = true;
        for (ContainerLocation containerLocation : super.getPlugin().getContainerManager().getContainerLocations())
        {
            boolean refilledContainer = super.getPlugin().getContainerManager().refillContainer(containerLocation);
            if(!refilledContainer)
                didSucceed = false;
        }

        if(didSucceed)
        {
            context.cause().audience().sendMessage(messageSource.resolveMessageWithPrefix("command.refillall.success"));
            return CommandResult.success();
        }
        else
        {
            throw messageSource.resolveExceptionWithMessage("command.refillall.failure");
        }
    }
}
