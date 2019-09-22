package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Aquerr on 2018-02-16.
 */

public class ContainerBreakListener extends AbstractListener
{
    private final List<RefillableContainer> destroyedContainers;

    public ContainerBreakListener(ChestRefill plugin)
    {
        super(plugin);
        this.destroyedContainers = new ArrayList<>();
    }

    @Listener
    public void onRefillableEntityBreak(final ChangeBlockEvent.Break event)
    {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions())
        {
            if (transaction.getOriginal().getLocation().isPresent())
            {
                ContainerLocation containerLocation = new ContainerLocation(transaction.getOriginal().getPosition(), transaction.getOriginal().getWorldUniqueId());

                for (RefillableContainer refillableContainer : super.getPlugin().getContainerManager().getRefillableContainers())
                {
                    if (refillableContainer.getContainerLocation().equals(containerLocation))
                    {
                        //TODO: If player destroyed the container, inform him/she about it.
                        destroyedContainers.add(refillableContainer);
                        super.getPlugin().getContainerManager().removeRefillableContainer(containerLocation);
                        break;
                    }
                }
            }
        }
    }

    @Listener
    public void onItemDrop(DropItemEvent event)
    {
        Optional<BlockSnapshot> blockSnapshot = event.getCause().first(BlockSnapshot.class);
        if (blockSnapshot.isPresent() && blockSnapshot.get().getLocation().isPresent())
        {
            ContainerLocation containerLocation = new ContainerLocation(blockSnapshot.get().getLocation().get().getBlockPosition(), blockSnapshot.get().getWorldUniqueId());
            for (RefillableContainer refillableContainer : destroyedContainers)
            {
                if (refillableContainer.getContainerLocation().equals(containerLocation) && refillableContainer.getHidingBlock().equals(blockSnapshot.get().getState().getType()))
                {
                    event.setCancelled(true);
                    destroyedContainers.clear();
                }
            }
        }
    }
}
