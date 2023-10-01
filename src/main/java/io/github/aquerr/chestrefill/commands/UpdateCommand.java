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
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collections;

import static io.github.aquerr.chestrefill.ChestRefill.SOMETHING_WENT_WRONG;
import static io.github.aquerr.chestrefill.PluginInfo.PLUGIN_PREFIX;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class UpdateCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public UpdateCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareSelectionParams(), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.update.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.update.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareSelectionParams()
    {
        return new SelectionParams(SelectionMode.UPDATE, this::updateContainer, Collections.emptyMap());
    }

    private void updateContainer(ModeExecutionParams executionParams)
    {
        RefillableContainer refillableContainer = executionParams.getRefillableContainer();
        RefillableContainer refillableContainerAtLocation = executionParams.getRefillableContainerAtLocation();
        ServerPlayer player = executionParams.getPlayer();

        refillableContainer.setItemProvider(refillableContainerAtLocation.getItemProvider());
        refillableContainer.setRestoreTime(refillableContainerAtLocation.getRestoreTime());
        refillableContainer.setName(refillableContainerAtLocation.getName());
        refillableContainer.setRequiredPermission(refillableContainerAtLocation.getRequiredPermission());
        refillableContainer.setHidingBlock(refillableContainerAtLocation.getHidingBlock());
        refillableContainer.setOpenMessage(refillableContainerAtLocation.getOpenMessage());

        final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainer);
        if(didSucceed)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully updated a refilling container!")));
        }
        else
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
    }
}
