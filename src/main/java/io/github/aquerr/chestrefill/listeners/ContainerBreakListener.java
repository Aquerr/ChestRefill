package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;

/**
 * Created by Aquerr on 2018-02-16.
 */

public class ContainerBreakListener
{
    @Listener
    public void onRefillableEntityBreak(ChangeBlockEvent.Break event)
    {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions())
        {
            if(transaction.getOriginal().getLocation().isPresent())
            {
                ContainerLocation containerLocation = new ContainerLocation(transaction.getOriginal().getPosition(), transaction.getOriginal().getWorldUniqueId());

                if(ContainerManager.getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(containerLocation)))
                {
                    //TODO: If player destroyed the container, inform him/she about it.
                    ContainerManager.removeRefillableContainer(containerLocation);
                }
            }
        }
    }
}
