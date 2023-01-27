package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WandCommand extends AbstractCommand
{
    private final MessageSource messageSource;

    public WandCommand(final ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = getPlugin().getMessageSource();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Inventory inventory = player.inventory();

        final List<Component> wandDescriptionLines = new ArrayList<>();
        final TextComponent firstLine = messageSource.resolveComponentWithMessage("command.wand.wand-lore.line1");
        final TextComponent secondLine = messageSource.resolveComponentWithMessage("command.wand.wand-lore.line2");
        wandDescriptionLines.add(firstLine);
        wandDescriptionLines.add(secondLine);

        final ItemStack chestRefillWand = ItemStack.builder()
                .itemType(ItemTypes.IRON_AXE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, messageSource.resolveComponentWithMessage("command.wand.wand-name"))
                .add(Keys.LORE, wandDescriptionLines)
                .build();

        inventory.offer(chestRefillWand);
        return CommandResult.success();
    }
}
