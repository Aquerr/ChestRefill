package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionPoints;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class PlayerDisconnectListener extends AbstractListener
{
    public PlayerDisconnectListener(final ChestRefill plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.POST)
    public void onPlayerDisconnect(final ClientConnectionEvent.Disconnect event)
    {
        final Player player = event.getTargetEntity();
        final SelectionPoints selectionPoints = ChestRefill.PLAYER_SELECTION_POINTS.get(player.getUniqueId());
        if (selectionPoints != null)
        {
            selectionPoints.setFirstPoint(null);
            selectionPoints.setSecondPoint(null);
        }
        ChestRefill.PLAYER_SELECTION_POINTS.remove(player.getUniqueId());
    }
}
