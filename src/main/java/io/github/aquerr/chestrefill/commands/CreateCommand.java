package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CreateCommand extends AbstractCommand
{
    private final MessageSource messageSource;
    private final ContainerManager containerManager;

    public CreateCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
        this.containerManager = plugin.getContainerManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Optional<String> optionalName = context.one(Parameter.string().key("name").build());
        final ServerPlayer serverPlayer = requirePlayerSource(context);

        if (optionalName.isPresent() && (super.getPlugin().getContainerManager().getRefillableContainers().stream().anyMatch(x->x.getName().equals(optionalName.get()))))
        {
            throw messageSource.resolveExceptionWithMessage("command.setcontainername.error.container-with-given-name-already-exists");
        }

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(optionalName.orElse("")), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.create.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.create.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareParams(String containerName)
    {
        Map<String, Object> params = new HashMap<>();
        params.put("CONTAINER_NAME", containerName);
        return new SelectionParams(SelectionMode.CREATE, this::createContainer, params);
    }

    private void createContainer(ModeExecutionParams executionParams)
    {
        RefillableContainer refillableContainer = executionParams.getBuiltContainer();
        refillableContainer.setName(String.valueOf(executionParams.getExtraData().get("CONTAINER_NAME")));
        final boolean didSucceed = containerManager.addRefillableContainer(executionParams.getBuiltContainer());
        if (didSucceed)
        {
            executionParams.getPlayer().sendMessage(messageSource.resolveMessageWithPrefix("command.create.successful-create"));
        }
        else
        {
            executionParams.getPlayer().sendMessage(messageSource.resolveMessageWithPrefix("error.command.something-went-wrong"));
        }
        ChestRefill.SELECTION_MODE.remove(executionParams.getPlayer().uniqueId());
    }
}
