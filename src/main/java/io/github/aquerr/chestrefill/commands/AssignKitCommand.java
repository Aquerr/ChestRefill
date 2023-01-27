package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.commands.arguments.ChestRefillCommandParameters;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class AssignKitCommand extends AbstractCommand implements CommandExecutor
{
    private final MessageSource messageSource;

    public AssignKitCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Kit kit = context.requireOne(ChestRefillCommandParameters.kit());

        final ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.PLAYER_CHEST_SELECTION_MODE.merge(serverPlayer.uniqueId(), SelectionMode.ASSIGN_KIT, (selectionMode, selectionMode2) -> null);
        ChestRefill.PLAYER_KIT_ASSIGN.merge(serverPlayer.uniqueId(), kit.getName(), (s, s2) -> null);
        boolean isModeActive = ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.assignkit.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.assignkit.turned-off"));
        }

        return CommandResult.success();
    }
}
