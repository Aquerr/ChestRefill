package io.github.aquerr.chestrefill.commands;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;

public class WandCommand extends AbstractCommand
{
    public WandCommand(final ChestRefill plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext args) throws CommandException
    {
        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Only in-game players can use this command!"));

        final Player player = (Player) source;
        final Inventory inventory = player.getInventory();

        final List<Text> wandDescriptionLines = new ArrayList<>();
        final Text firstLine = Text.of("Select first point with your", TextColors.GOLD, " left click.");
        final Text secondLine = Text.of("Select second point with your", TextColors.GOLD, " right click.");
        wandDescriptionLines.add(firstLine);
        wandDescriptionLines.add(secondLine);

        final ItemStack chestRefillWand = ItemStack.builder()
                .itemType(ItemTypes.IRON_AXE)
                .quantity(1)
                .add(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "ChestRefill Wand"))
                .add(Keys.ITEM_LORE, wandDescriptionLines)
                .build();

        inventory.offer(chestRefillWand);
        return CommandResult.success();
    }
}
