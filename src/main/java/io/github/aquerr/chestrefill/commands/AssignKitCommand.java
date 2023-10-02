package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.commands.arguments.ChestRefillCommandParameters;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

import static io.github.aquerr.chestrefill.ChestRefill.SOMETHING_WENT_WRONG;
import static io.github.aquerr.chestrefill.PluginInfo.PLUGIN_PREFIX;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class AssignKitCommand extends AbstractCommand implements CommandExecutor
{
    private final MessageSource messageSource;

    public AssignKitCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final Kit kit = context.requireOne(ChestRefillCommandParameters.kit());

        final ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(kit), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.assignkit.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.assignkit.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareParams(final Kit kit)
    {
        final Map<String, Object> extraData = new HashMap<>();
        extraData.put("KIT", kit);
        return new SelectionParams(SelectionMode.ASSIGN_KIT, this::assignKit, extraData);
    }

    private void assignKit(final ModeExecutionParams params)
    {
        final ServerPlayer player = params.getPlayer();
        final RefillableContainer refillableContainer = params.getRefillableContainerAtLocation();
        final Kit kit = (Kit) params.getExtraData().get("KIT");
        final boolean didSucceed = super.getPlugin().getContainerManager().assignKit(refillableContainer.getContainerLocation(), kit.getName());
        if(didSucceed)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully assigned a kit to the refilling container!")));
        }
        else
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
