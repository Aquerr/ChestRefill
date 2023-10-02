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

public class SetContainerNameCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public SetContainerNameCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        String containerName = context.requireOne(Parameter.string().key("name").build());
        ServerPlayer serverPlayer = requirePlayerSource(context);

        if(super.getPlugin().getContainerManager().getRefillableContainers().stream().anyMatch(x->x.getName().equals(containerName)))
        {
            throw messageSource.resolveExceptionWithMessage("command.setcontainername.error.container-with-given-name-already-exists");
        }
        
        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(containerName), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.setcontainername.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.setcontainername.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareParams(final String containerName)
    {
        final Map<String, Object> extraData = new HashMap<>();
        extraData.put("CONTAINER_NAME", containerName);
        return new SelectionParams(SelectionMode.SET_CONTAINER_NAME, this::renameContainer, extraData);
    }

    private void renameContainer(ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        final String containerName = (String) params.getExtraData().get("CONTAINER_NAME");
        final boolean didSucceed = super.getPlugin().getContainerManager().renameRefillableContainer(params.getRefillableContainerAtLocation().getContainerLocation(), containerName);
        if(didSucceed)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully updated a refilling container!")));
        }
        else
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
