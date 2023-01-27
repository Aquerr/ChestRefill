package io.github.aquerr.chestrefill.commands;

import com.google.common.collect.Lists;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;

import java.util.List;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public class KitsCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public KitsCommand(ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        List<Component> helpList = Lists.newArrayList();

        for(Kit kit : super.getPlugin().getContainerManager().getKits().values())
        {
            TextComponent.Builder itemsToShow = Component.text();

            itemsToShow.append(linear(messageSource.resolveComponentWithMessage("command.kits.kit-name", kit.getName()), newline()));
            itemsToShow.append(linear(messageSource.resolveComponentWithMessage("command.kits.items-in-kit"), newline()));
            kit.getItems().forEach(x -> itemsToShow.append(linear(YELLOW, Component.translatable(x.getItem().type().key(RegistryTypes.ITEM_TYPE).asString()), text(" x" + x.getItem().quantity()), newline())));

            TextComponent kitText = empty()
                    .append(linear(YELLOW, text(" - "), YELLOW, text(kit.getName())))
                    .hoverEvent(HoverEvent.showText(itemsToShow));

            helpList.add(kitText);
        }

        PaginationService paginationService = Sponge.serviceProvider().paginationService();
        PaginationList.Builder paginationBuilder = paginationService.builder()
                .title(messageSource.resolveComponentWithMessage("command.kits.header"))
                .padding(messageSource.resolveComponentWithMessage("command.kits.padding-character"))
                .contents(helpList)
                .linesPerPage(10);
        paginationBuilder.sendTo(context.cause().audience());


        return CommandResult.success();
    }
}
