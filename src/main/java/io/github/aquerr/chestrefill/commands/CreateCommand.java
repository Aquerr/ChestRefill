package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public class CreateCommand extends AbstractCommand implements CommandExecutor
{
    private final MessageSource messageSource;

    public CreateCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Optional<String> optionalName = context.one(Parameter.string().key("name").build());
        final ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), SelectionMode.CREATE, (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            optionalName.ifPresent(s -> ChestRefill.PLAYER_CHEST_NAME.put(serverPlayer.uniqueId(), s));
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.create.turned-on"));
        }
        else
        {
            ChestRefill.PLAYER_CHEST_NAME.remove(serverPlayer.uniqueId());
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.create.turned-off"));
        }

        return CommandResult.success();
    }
}
