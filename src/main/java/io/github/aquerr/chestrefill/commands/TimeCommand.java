package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

import static io.github.aquerr.chestrefill.ChestRefill.SOMETHING_WENT_WRONG;
import static io.github.aquerr.chestrefill.PluginInfo.PLUGIN_PREFIX;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class TimeCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public TimeCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        Integer time = context.requireOne(Parameter.integerNumber().key("time").build());
        ServerPlayer serverPlayer = requirePlayerSource(context);
        
        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareSelectionParams(time), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.time.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.time.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareSelectionParams(Integer time)
    {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("TIME", time);
        return new SelectionParams(SelectionMode.TIME, this::updateTime, extraData);
    }

    private void updateTime(ModeExecutionParams executionParams)
    {
        final ServerPlayer player = executionParams.getPlayer();
        final Integer time = (Integer)executionParams.getExtraData().get("TIME");
        final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillingTime(executionParams.getRefillableContainer().getContainerLocation(), time);
        if(didSucceed)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully updated container's refill time!")));
        }
        else
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
    }
}
