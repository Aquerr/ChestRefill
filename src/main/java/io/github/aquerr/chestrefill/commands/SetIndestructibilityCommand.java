package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
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

public class SetIndestructibilityCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public SetIndestructibilityCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final boolean shouldSetIndestructible = context.requireOne(Parameter.bool().key("value").build());
        final ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(shouldSetIndestructible), (selectionMode, selectionMode2) -> null);
        if (shouldSetIndestructible)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.indestructibility.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.indestructibility.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareParams(boolean shouldSetIndestructible)
    {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("SET_INDESTRUCTIBILITY", shouldSetIndestructible);
        return new SelectionParams(SelectionMode.SET_INDESTRUCTIBILITY, this::setIndestructibility, extraData);
    }

    private void setIndestructibility(ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        RefillableContainer refillableContainer = params.getRefillableContainerAtLocation();
        final boolean value = (boolean)params.getExtraData().get("SET_INDESTRUCTIBILITY");
        refillableContainer.setIndestructible(value);
        final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainer);
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
