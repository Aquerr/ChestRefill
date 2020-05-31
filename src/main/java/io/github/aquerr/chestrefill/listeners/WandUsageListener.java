package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionPoints;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class WandUsageListener extends AbstractListener
{
    public WandUsageListener(final ChestRefill plugin)
    {
        super(plugin);
    }

    @Listener
    public void onRightClick(final InteractBlockEvent.Secondary event, final @Root Player player)
    {
        if(event.getHandType() == HandTypes.MAIN_HAND)
            return;

        if(event.getTargetBlock() == BlockSnapshot.NONE)
            return;

        if(!player.getItemInHand(HandTypes.MAIN_HAND).isPresent())
            return;

        final ItemStack itemInHand = player.getItemInHand(HandTypes.MAIN_HAND).get();

        if(!itemInHand.get(Keys.DISPLAY_NAME).isPresent() || !player.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).get().toPlain().equals("ChestRefill Wand"))
            return;

        SelectionPoints selectionPoints = ChestRefill.PLAYER_SELECTION_POINTS.get(player.getUniqueId());
        if (selectionPoints == null)
        {
            selectionPoints = new SelectionPoints(null, event.getTargetBlock().getPosition());
        }
        else
        {
            selectionPoints.setSecondPoint(event.getTargetBlock().getPosition());
        }

        ChestRefill.PLAYER_SELECTION_POINTS.put(player.getUniqueId(), selectionPoints);
        player.sendMessage(Text.of(TextColors.GOLD, "Second point", TextColors.BLUE, " has been selected at ", TextColors.GOLD, event.getTargetBlock().getPosition()));
        event.setCancelled(true);
    }

    @Listener
    public void onLeftClick(final InteractBlockEvent.Primary event, final @Root Player player)
    {
        if(event.getHandType() == HandTypes.OFF_HAND)
            return;

        if(event.getTargetBlock() == BlockSnapshot.NONE)
            return;

        if(!player.getItemInHand(HandTypes.MAIN_HAND).isPresent())
            return;

        final ItemStack itemInHand = player.getItemInHand(HandTypes.MAIN_HAND).get();

        if(!itemInHand.get(Keys.DISPLAY_NAME).isPresent() || !player.getItemInHand(HandTypes.MAIN_HAND).get().get(Keys.DISPLAY_NAME).get().toPlain().equals("ChestRefill Wand"))
            return;

        SelectionPoints selectionPoints = ChestRefill.PLAYER_SELECTION_POINTS.get(player.getUniqueId());
        if (selectionPoints == null)
        {
            selectionPoints = new SelectionPoints(event.getTargetBlock().getPosition(), null);
        }
        else
        {
            selectionPoints.setFirstPoint(event.getTargetBlock().getPosition());
        }

        ChestRefill.PLAYER_SELECTION_POINTS.put(player.getUniqueId(), selectionPoints);
        player.sendMessage(Text.of(TextColors.GOLD, "First point", TextColors.BLUE, " has been selected at ", TextColors.GOLD, event.getTargetBlock().getPosition()));
        event.setCancelled(true);
    }
}
