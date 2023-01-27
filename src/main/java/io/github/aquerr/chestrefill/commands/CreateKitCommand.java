package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class CreateKitCommand extends AbstractCommand implements CommandExecutor
{
    private final MessageSource messageSource;

    public CreateKitCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        String kitName = context.requireOne(Parameter.string().key("name").build());
        ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.PLAYER_CHEST_SELECTION_MODE.merge(serverPlayer.uniqueId(), SelectionMode.CREATE_KIT, (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            if(super.getPlugin().getContainerManager().getKits().keySet().stream().anyMatch(x->x.equals(kitName)))
            {
                throw messageSource.resolveExceptionWithMessage("command.createkit.error.kit-with-given-name-already-exists");
            }

            ChestRefill.PLAYER_KIT_NAME.put(serverPlayer.uniqueId(), kitName);
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.createkit.turned-on"));
        }
        else
        {
            ChestRefill.PLAYER_KIT_NAME.remove(serverPlayer.uniqueId());
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.createkit.turned-off"));
        }

        return CommandResult.success();
    }
}
