package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.entities.ChestLocation;
import io.github.aquerr.chestrefill.managers.ChestManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;

/**
 * Created by Aquerr on 2018-02-16.
 */

public class ChestBreakListener
{
    @Listener
    public void onChestBreak(ChangeBlockEvent.Break event)
    {
        for (Transaction<BlockSnapshot> transaction :event.getTransactions())
        {
            if (transaction.getOriginal().getState().getType() == BlockTypes.CHEST)
            {
                ChestLocation chestLocation = new ChestLocation(transaction.getOriginal().getPosition(), transaction.getOriginal().getWorldUniqueId());

                if (ChestManager.getChests().stream().anyMatch(x->x.getChestLocation().equals(chestLocation)))
                {
                    ChestManager.removeChest(chestLocation);
                }
            }
        }
    }
}
