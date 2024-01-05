package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.commands.arguments.ChestRefillCommandParameters;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

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

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(lootTableName), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
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

    private SelectionParams prepareParams(String lootTableName)
    {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("LOOT_TABLE_NAME", lootTableName);
        return new SelectionParams(SelectionMode.ASSIGN_LOOT_TABLE, this::assignLootTable, extraData);
    }

    private void assignLootTable(ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        final String lootTableName = (String) params.getExtraData().get("LOOT_TABLE_NAME");
        final boolean didSucceed = super.getPlugin().getContainerManager().assignLootTable(params.getRefillableContainerAtLocation().getContainerLocation(), lootTableName);
        if(didSucceed)
        {
            player.sendMessage(messageSource.resolveMessageWithPrefix("command.successful-refillable-container-update"));
        }
        else
        {
            player.sendMessage(messageSource.resolveMessageWithPrefix("error.command.something-went-wrong"));
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
