package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.entities.TileEntityLocation;
import io.github.aquerr.chestrefill.managers.ChestManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

/**
 * Created by Aquerr on 2018-02-16.
 */

public class TileEntityBreakListener
{
    @Listener
    public void onChestBreak(ChangeBlockEvent.Break event)
    {
        for (Transaction<BlockSnapshot> transaction :event.getTransactions())
        {
            if (transaction.getOriginal().getLocation().isPresent() && transaction.getOriginal().getLocation().get().getTileEntity().isPresent())
            {
                if (ChestManager.allowedTileEntityTypes.contains(transaction.getOriginal().getLocation().get().getTileEntity().get().getType()))
                {
                    TileEntityLocation tileEntityLocation = new TileEntityLocation(transaction.getOriginal().getPosition(), transaction.getOriginal().getWorldUniqueId());

                    if (ChestManager.getRefillableTileEntities().stream().anyMatch(x->x.getTileEntityLocation().equals(tileEntityLocation)))
                    {
                        ChestManager.removeChest(tileEntityLocation);
                    }
                }
            }
        }
    }
}
