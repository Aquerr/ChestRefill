package io.github.aquerr.chestrefill.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;

/**
 * Created by Aquerr on 2018-02-10.
 */

public class HelpCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        source.sendMessage(Text.of("Printing out help command!"));

        return CommandResult.success();
    }
}
