package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionPoints;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;

public class WandUsageListener extends AbstractListener
{
    private final MessageSource messageSource;

    public WandUsageListener(final ChestRefill plugin)
    {
        super(plugin);
        this.messageSource = plugin.getMessageSource();
    }

    @Listener
    public void onRightClick(final InteractBlockEvent.Secondary event, final @Root Player player)
    {
        EventContext eventContext = event.context();
        HandType handType = eventContext.get(EventContextKeys.USED_HAND).orElse(null);

        if(handType == HandTypes.MAIN_HAND.get())
            return;

        if(event.block() == BlockSnapshot.empty())
            return;

        if(player.itemInHand(HandTypes.MAIN_HAND).isEmpty())
            return;

        if (!isHoldingChestRefillWand(player))
            return;

        SelectionPoints selectionPoints = ChestRefill.PLAYER_SELECTION_POINTS.get(player.uniqueId());
        if (selectionPoints == null)
        {
            selectionPoints = new SelectionPoints(null, event.block().position());
        }
        else
        {
            selectionPoints.setSecondPoint(event.block().position());
        }

        ChestRefill.PLAYER_SELECTION_POINTS.put(player.uniqueId(), selectionPoints);
        player.sendMessage(messageSource.resolveComponentWithMessage("wand.selection.second", event.block().position().toString()));
        event.setCancelled(true);
    }

    @Listener
    public void onLeftClick(final InteractBlockEvent.Primary.Start event, final @Root Player player)
    {
        EventContext eventContext = event.context();
        HandType handType = eventContext.get(EventContextKeys.USED_HAND).orElse(null);

        if(handType == HandTypes.OFF_HAND.get())
            return;

        if(event.block() == BlockSnapshot.empty())
            return;

        if (!isHoldingChestRefillWand(player))
            return;

        SelectionPoints selectionPoints = ChestRefill.PLAYER_SELECTION_POINTS.get(player.uniqueId());
        if (selectionPoints == null)
        {
            selectionPoints = new SelectionPoints(event.block().position(), null);
        }
        else
        {
            selectionPoints.setFirstPoint(event.block().position());
        }

        ChestRefill.PLAYER_SELECTION_POINTS.put(player.uniqueId(), selectionPoints);
        player.sendMessage(messageSource.resolveComponentWithMessage("wand.selection.first", event.block().position().toString()));
        event.setCancelled(true);
    }

    private boolean isHoldingChestRefillWand(Player player)
    {
        final ItemStack itemInHand = player.itemInHand(HandTypes.MAIN_HAND);
        return isChestRefillWand(itemInHand);
    }

    private boolean isChestRefillWand(ItemStack itemStack)
    {
        return itemStack.get(Keys.CUSTOM_NAME).isPresent() || !itemStack.get(Keys.CUSTOM_NAME).get()
                .equals(messageSource.resolveComponentWithMessage("command.wand.wand-name"));
    }
}
