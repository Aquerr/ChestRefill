package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.github.aquerr.chestrefill.ChestRefill.SOMETHING_WENT_WRONG;
import static io.github.aquerr.chestrefill.PluginInfo.PLUGIN_PREFIX;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class CopyCommand extends AbstractCommand implements CommandExecutor
{
    private final MessageSource messageSource;
    private final ContainerManager containerManager;

    public CopyCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
        this.containerManager = plugin.getContainerManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.copy.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.copy.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareParams()
    {
        return new SelectionParams(SelectionMode.COPY, this::copyContainer, Collections.emptyMap());
    }

    private void copyContainer(ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();

        final Map<String, Object> extraData = new HashMap<>(params.getExtraData());
        extraData.put("CONTAINER_COPY", params.getRefillableContainerAtLocation().copy());

        final SelectionParams selectionParams = new SelectionParams(SelectionMode.AFTER_COPY, this::doCopy, extraData);
        ChestRefill.SELECTION_MODE.put(player.uniqueId(), selectionParams);

        player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Now select a new container which should behave in the same way!")));
    }

    private void doCopy(ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        final RefillableContainer clickedContainer = params.getBuiltContainer();
        final RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        final RefillableContainer copiedContainer = (RefillableContainer) params.getExtraData().get("CONTAINER_COPY");

        if(!copiedContainer.getContainerBlockType().equals(clickedContainer.getContainerBlockType()))
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, text("Containers must be of the same type!")));
        }

        copiedContainer.setContainerLocation(clickedContainer.getContainerLocation());

        clickedContainer.setContainerLocation(copiedContainer.getContainerLocation());
        boolean didSucceed;
        if(refillableContainerAtLocation != null)
        {
            super.getPlugin().getContainerManager().removeRefillableContainer(clickedContainer.getContainerLocation());
        }
        didSucceed = super.getPlugin().getContainerManager().addRefillableContainer(copiedContainer);

        if (didSucceed)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully copied a refilling container!")));
        }
        else
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
