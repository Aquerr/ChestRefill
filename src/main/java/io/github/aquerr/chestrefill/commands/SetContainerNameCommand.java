package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

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
        
        ChestRefill.PLAYER_CHEST_SELECTION_MODE.merge(serverPlayer.uniqueId(), SelectionMode.SET_CONTAINER_NAME, (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            if(super.getPlugin().getContainerManager().getRefillableContainers().stream().anyMatch(x->x.equals(containerName)))
            {
                throw messageSource.resolveExceptionWithMessage("command.setcontainername.error.container-with-given-name-already-exists");
            }

            ChestRefill.PLAYER_CHEST_NAME.put(serverPlayer.uniqueId(), containerName);
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.setcontainername.turned-on"));
        }
        else
        {
            ChestRefill.PLAYER_CHEST_NAME.remove(serverPlayer.uniqueId());
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.setcontainername.turned-off"));
        }

        return CommandResult.success();
    }
}
