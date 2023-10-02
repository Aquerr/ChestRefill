package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
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

public class RemoveCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public RemoveCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        ServerPlayer serverPlayer = requirePlayerSource(context);
        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.remove.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.remove.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareParams()
    {
        return new SelectionParams(SelectionMode.REMOVE, this::removeContainer, Collections.emptyMap());
    }

    private void removeContainer(ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        final boolean didSucceed = super.getPlugin().getContainerManager().removeRefillableContainer(params.getRefillableContainerAtLocation().getContainerLocation());
        if(didSucceed)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully removed a refilling container!")));
        }
        else
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, SOMETHING_WENT_WRONG));
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
