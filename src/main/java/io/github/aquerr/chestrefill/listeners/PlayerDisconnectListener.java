package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.SelectionPoints;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class PlayerDisconnectListener extends AbstractListener
{
    public PlayerDisconnectListener(final ChestRefill plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.POST)
    public void onPlayerDisconnect(final ServerSideConnectionEvent.Disconnect event)
    {
        final ServerPlayer player = event.player();
        final SelectionPoints selectionPoints = ChestRefill.PLAYER_SELECTION_POINTS.get(player.uniqueId());
        if (selectionPoints != null)
        {
            selectionPoints.setFirstPoint(null);
            selectionPoints.setSecondPoint(null);
        }
        ChestRefill.PLAYER_SELECTION_POINTS.remove(player.uniqueId());
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
