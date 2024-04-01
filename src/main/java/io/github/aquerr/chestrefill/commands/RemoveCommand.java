package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
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

import java.util.Collections;
import java.util.Optional;

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
        final Optional<String> optionalChestNameToRemove = context.one(Parameter.string().key("name").build());
        ServerPlayer serverPlayer = requirePlayerSource(context);
        if (optionalChestNameToRemove.isPresent())
        {
            removeContainerByName(serverPlayer, optionalChestNameToRemove.get());
        }
        else
        {
            toggleRemoveMode(serverPlayer);
        }

        return CommandResult.success();
    }

    private void toggleRemoveMode(ServerPlayer serverPlayer)
    {
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
    }

    private void removeContainerByName(ServerPlayer player, String chestName)
    {
        Optional<ContainerLocation> foundContainerLocationToRemove = super.getPlugin().getContainerManager().getRefillableContainers().stream()
                .filter(container -> chestName.equals(container.getName()))
                .map(RefillableContainer::getContainerLocation)
                .findFirst();
        if (foundContainerLocationToRemove.isPresent())
        {
            boolean didSuccess = super.getPlugin().getContainerManager().removeRefillableContainer(foundContainerLocationToRemove.get());
            handleDidSuccess(player, didSuccess);
        }
        else
        {
            player.sendMessage(messageSource.resolveMessageWithPrefix("command.remove-by-name.not-found"));
        }
    }

    private SelectionParams prepareParams()
    {
        return new SelectionParams(SelectionMode.REMOVE, this::removeContainer, Collections.emptyMap());
    }

    private void removeContainer(ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        final boolean didSucceed = super.getPlugin().getContainerManager().removeRefillableContainer(params.getRefillableContainerAtLocation().getContainerLocation());
        handleDidSuccess(player, didSucceed);
    }

    private void handleDidSuccess(ServerPlayer player, boolean didSuccess)
    {
        if (didSuccess)
        {
            player.sendMessage(messageSource.resolveMessageWithPrefix("command.remove.successful-remove"));
        }
        else
        {
            player.sendMessage(messageSource.resolveMessageWithPrefix("error.command.something-went-wrong"));
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
