package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;

import java.util.Collections;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
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
        ServerPlayer serverPlayer = requirePlayerSource(context);

        ChestRefill.SELECTION_MODE.merge(serverPlayer.uniqueId(), prepareParams(), (selectionMode, selectionMode2) -> null);
        boolean isModeActive = ChestRefill.SELECTION_MODE.containsKey(serverPlayer.uniqueId());
        if (isModeActive)
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.info.turned-on"));
        }
        else
        {
            serverPlayer.sendMessage(messageSource.resolveMessageWithPrefix("command.info.turned-off"));
        }

        return CommandResult.success();
    }

    private SelectionParams prepareParams()
    {
        return new SelectionParams(SelectionMode.INFO, this::showContainerInfo, Collections.emptyMap());
    }

    private void showContainerInfo(ModeExecutionParams params)
    {
        ServerPlayer player = params.getPlayer();
        RefillableContainer refillableContainer = params.getRefillableContainerAtLocation();

        TextComponent info = Component.empty()
                .append(linear(messageSource.resolveComponentWithMessage("command.list.container-name", refillableContainer.getName()), newline()))
                .append(linear(messageSource.resolveComponentWithMessage("command.list.items-in-inventory"), newline()));

        for (final RefillableItem refillableItem : refillableContainer.getItems())
        {
            info = info.append(linear(YELLOW, Component.translatable(refillableItem.getItem().type().key(RegistryTypes.ITEM_TYPE).asString()), WHITE, text(" x" + refillableItem.getItem().quantity()), newline()));
        }

        info = info.append(linear(newline(), messageSource.resolveComponentWithMessage("command.list.item-provider", refillableContainer.getItemProvider().getType(), refillableContainer.getItemProvider().getLocation()), newline()))
                .append(linear(messageSource.resolveComponentWithMessage("command.list.place-items-in-random-slots", refillableContainer.shouldPlaceItemsInRandomSlots()), newline()))
                .append(linear(messageSource.resolveComponentWithMessage("command.list.one-item-at-time", refillableContainer.shouldRefillOneItemAtTime()), newline()))
                .append(linear(messageSource.resolveComponentWithMessage("command.list.replace-existing-items", refillableContainer.shouldReplaceExistingItems()), newline()))
                .append(linear(messageSource.resolveComponentWithMessage("command.list.hidden-if-no-items", refillableContainer.shouldBeHiddenIfNoItems()), newline()))
                .append(linear(messageSource.resolveComponentWithMessage("command.list.hiding-block", refillableContainer.getHidingBlock()), newline()))
                .append(linear(messageSource.resolveComponentWithMessage("command.list.permission", refillableContainer.getRequiredPermission())))
                .append(linear(newline(), messageSource.resolveComponentWithMessage("command.list.container-cooldown", refillableContainer.getRestoreTime())));

        PaginationService paginationService = Sponge.serviceProvider().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder()
                .title(messageSource.resolveComponentWithMessage("command.list.header"))
                .padding(messageSource.resolveComponentWithMessage("command.list.padding-character"))
                .contents(info)
                .linesPerPage(10);
        paginationBuilder.sendTo(player);
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
