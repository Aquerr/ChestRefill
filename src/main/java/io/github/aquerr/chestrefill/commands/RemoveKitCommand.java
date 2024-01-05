package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import net.kyori.adventure.identity.Identity;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.Map;

public class RemoveKitCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public RemoveKitCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = getPlugin().getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        String kitName = context.requireOne(Parameter.string().key("name").build());
        Map<String, Kit> kits = super.getPlugin().getContainerManager().getKits();

        if(kits.keySet().stream().noneMatch(x->x.equals(kitName)))
        {
            throw messageSource.resolveExceptionWithMessage("command.removekit.error.kit-does-not-exist");
        }

        boolean didSucceed = super.getPlugin().getContainerManager().removeKit(kitName);
        if (didSucceed)
        {
            context.sendMessage(Identity.nil(), messageSource.resolveMessageWithPrefix("command.removekit.success"));
        }
        else
        {
            throw messageSource.resolveExceptionWithMessage("error.command.something-went-wrong");
        }

        return CommandResult.success();
    }
}
