package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.transaction.BlockTransaction;
import org.spongepowered.api.block.transaction.Operations;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.server.ServerWorld;

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
    public void onRefillableEntityBreak(final ChangeBlockEvent.All event)
    {
        for (BlockTransaction transaction : event.transactions())
        {
            if (transaction.operation() != Operations.BREAK.get())
                continue;

            if (transaction.original().location().isPresent())
            {
                ContainerLocation containerLocation = new ContainerLocation(transaction.original().position(), transaction.original().location()
                        .map(Location::world)
                        .map(ServerWorld::uniqueId)
                        .orElse(null));

                for (RefillableContainer refillableContainer : super.getPlugin().getContainerManager().getRefillableContainers())
                {
                    if (refillableContainer.getContainerLocation().equals(containerLocation))
                    {
                        //TODO: If player destroyed the container, inform him/her about it.
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
        Optional<BlockSnapshot> blockSnapshot = event.cause().first(BlockSnapshot.class);
        if (blockSnapshot.isPresent() && blockSnapshot.get().location().isPresent())
        {
            ContainerLocation containerLocation = new ContainerLocation(blockSnapshot.get().location().get().blockPosition(),
                    blockSnapshot.get().location().map(Location::world)
                            .map(ServerWorld::uniqueId)
                            .orElse(null));

            for (RefillableContainer refillableContainer : destroyedContainers)
            {
                if (refillableContainer.getContainerLocation().equals(containerLocation) && refillableContainer.getHidingBlock().equals(blockSnapshot.get().state().type()))
                {
                    event.setCancelled(true);
                    destroyedContainers.clear();
                }
            }
        }
    }
}
