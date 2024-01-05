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

public class SetOpenMessageCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public SetOpenMessageCommand(final ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        String message = context.one(Parameter.string().key("message").build()).orElse("");
        ServerPlayer serverPlayer = requirePlayerSource(context);
        
        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(message), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.setopenmessage.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.setopenmessage.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareParams(String openMessage)
    {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("OPEN_MESSAGE", openMessage);
        return new SelectionParams(SelectionMode.SET_OPEN_MESSAGE, this::updateContainerOpenMessage, extraData);
    }

    private void updateContainerOpenMessage(ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        final RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        final String openMessage = (String) params.getExtraData().get("OPEN_MESSAGE");
        refillableContainerAtLocation.setOpenMessage(openMessage.trim());
        final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainerAtLocation);
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
