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

import static io.github.aquerr.chestrefill.ChestRefill.SOMETHING_WENT_WRONG;
import static io.github.aquerr.chestrefill.PluginInfo.PLUGIN_PREFIX;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

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
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully assigned a loot table to the refilling container!")));
        }
        else
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
