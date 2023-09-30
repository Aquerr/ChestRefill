package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.commands.arguments.ChestRefillCommandParameters;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class AssignLootTableCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public AssignLootTableCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final String lootTableName = context.requireOne(ChestRefillCommandParameters.lootTable());

        final ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.PLAYER_CHEST_SELECTION_MODE.merge(serverPlayer.uniqueId(), SelectionMode.ASSIGN_LOOT_TABLE, (selectionMode, selectionMode2) -> null);
        ChestRefill.PLAYER_LOOT_TABLE_ASSIGN.merge(serverPlayer.uniqueId(), lootTableName, (s, s2) -> null);
        boolean isModeActive = ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.assignloottable.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.assignloottable.turned-off"));
        }

        return CommandResult.success();
    }
}
