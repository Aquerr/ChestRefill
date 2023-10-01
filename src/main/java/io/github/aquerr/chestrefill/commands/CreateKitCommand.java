package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandExecutor;
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

        if(super.getPlugin().getContainerManager().getKits().keySet().stream().anyMatch(x->x.equals(kitName)))
        {
            throw messageSource.resolveExceptionWithMessage("command.createkit.error.kit-with-given-name-already-exists");
        }

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(kitName), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.createkit.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.createkit.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareParams(final String kitName)
    {
        final Map<String, Object> extraData = new HashMap<>();
        extraData.put("KIT_NAME", kitName);
        return new SelectionParams(SelectionMode.CREATE_KIT, this::createKit, extraData);
    }

    private void createKit(final ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        final String kitName = (String) params.getExtraData().get("KIT_NAME");
        final Kit kit = new Kit(kitName, params.getRefillableContainer().getItems());
        final boolean didSucceed = super.getPlugin().getContainerManager().createKit(kit);
        if (didSucceed)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully created a kit!")));
        }
        else
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
    }
}
