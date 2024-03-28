package io.github.aquerr.chestrefill.commands;

import com.google.common.collect.Lists;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import io.github.aquerr.chestrefill.util.WorldUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.List;
import java.util.function.Consumer;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public class ListCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public ListCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = getPlugin().getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        List<Component> helpList = Lists.newArrayList();

        for(RefillableContainer refillableContainer : super.getPlugin().getContainerManager().getRefillableContainers())
        {
            TextComponent itemsToShow = Component.empty()
                    .append(linear(messageSource.resolveComponentWithMessage("command.list.container-name", refillableContainer.getName()), newline()))
                    .append(linear(messageSource.resolveComponentWithMessage("command.list.items-in-inventory"), newline()));

            for (final RefillableItem refillableItem : refillableContainer.getItems())
            {
                itemsToShow = itemsToShow.append(linear(YELLOW, Component.translatable(refillableItem.getItem().type().key(RegistryTypes.ITEM_TYPE).asString()), WHITE, text(" x" + refillableItem.getItem().quantity()), newline()));
            }

            itemsToShow = itemsToShow.append(linear(newline(), messageSource.resolveComponentWithMessage("command.list.item-provider", refillableContainer.getItemProvider().getType(), refillableContainer.getItemProvider().getLocation()), newline()))
                    .append(linear(messageSource.resolveComponentWithMessage("command.list.place-items-in-random-slots", refillableContainer.shouldPlaceItemsInRandomSlots()), newline()))
                    .append(linear(messageSource.resolveComponentWithMessage("command.list.one-item-at-time", refillableContainer.shouldRefillOneItemAtTime()), newline()))
                    .append(linear(messageSource.resolveComponentWithMessage("command.list.replace-existing-items", refillableContainer.shouldReplaceExistingItems()), newline()))
                    .append(linear(messageSource.resolveComponentWithMessage("command.list.hidden-if-no-items", refillableContainer.shouldBeHiddenIfNoItems()), newline()))
                    .append(linear(messageSource.resolveComponentWithMessage("command.list.hiding-block", refillableContainer.getHidingBlock()), newline()))
                    .append(linear(messageSource.resolveComponentWithMessage("command.list.indestructible", refillableContainer.isIndestructible()), newline()))
                    .append(linear(messageSource.resolveComponentWithMessage("command.list.permission", refillableContainer.getRequiredPermission())))
                    .append(linear(newline(), messageSource.resolveComponentWithMessage("command.list.container-cooldown", refillableContainer.getRestoreTime())))
                    .append(linear(newline(), messageSource.resolveComponentWithMessage("command.list.click-to-teleport")));

            TextComponent chestText = empty();
            if(refillableContainer.getName().equals(""))
                chestText = chestText.append(messageSource.resolveComponentWithMessage("command.list.unnamed-container-at-location", refillableContainer.getContainerLocation().getBlockPosition().toString()));
            else
                chestText = chestText.append(messageSource.resolveComponentWithMessage("command.list.container-at-location", refillableContainer.getName(), refillableContainer.getContainerLocation().getBlockPosition().toString()));

            chestText = chestText.hoverEvent(HoverEvent.showText(itemsToShow))
                    .clickEvent(SpongeComponents.executeCallback(new ChestTeleport(refillableContainer.getContainerLocation())));

            helpList.add(chestText);
        }

        PaginationService paginationService = Sponge.serviceProvider().provide(PaginationService.class).get();
        PaginationList.Builder paginationBuilder = paginationService.builder()
                .title(messageSource.resolveComponentWithMessage("command.list.header"))
                .padding(messageSource.resolveComponentWithMessage("command.list.padding-character"))
                .contents(helpList)
                .linesPerPage(10);
        paginationBuilder.sendTo(context.cause().audience());


        return CommandResult.success();
    }

    private static class ChestTeleport implements Consumer<CommandCause>
    {
        private final ContainerLocation containerLocation;

        ChestTeleport(ContainerLocation containerLocation)
        {
            this.containerLocation = containerLocation;
        }

        @Override
        public void accept(CommandCause source)
        {
            final ServerPlayer player = (ServerPlayer) source.audience();
            player.setLocation(ServerLocation.of(WorldUtils.getWorldByUUID(containerLocation.getWorldUUID()).orElse(null), containerLocation.getBlockPosition()));
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(ChestRefill.getInstance().getMessageSource().resolveComponentWithMessage("command.list.you-were-teleported-to-selected-container")));
        }
    }
}
