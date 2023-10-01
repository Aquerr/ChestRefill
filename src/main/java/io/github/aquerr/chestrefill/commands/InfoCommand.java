package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;

import static io.github.aquerr.chestrefill.PluginInfo.PLUGIN_PREFIX;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public class InfoCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public InfoCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
//        final ContainerLocation containerLocation1 = refillableContainer.getContainerLocation();
//        RefillableContainer chestToView = super.getPlugin().getContainerManager().getRefillableContainers().stream().filter(x -> x.getContainerLocation().equals(containerLocation1)).findFirst().get();
//        player.sendMessage(
//                linear(PLUGIN_PREFIX,
//                        YELLOW, text("This container refills every "),
//                        GREEN, text(chestToView.getRestoreTime()),
//                        YELLOW, text(" seconds")));
        return CommandResult.success();
    }
}
