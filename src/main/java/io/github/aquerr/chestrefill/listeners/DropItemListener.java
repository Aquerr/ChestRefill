package io.github.aquerr.chestrefill.listeners;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.DropItemEvent;

import java.util.Optional;

public class DropItemListener
{
    @Listener
    public void onItemDropAfterDestroyingChest(DropItemEvent.Destruct event)
    {
        Optional<BlockSnapshot> blockSnapshot = event.getCause().first(BlockSnapshot.class);

        if (blockSnapshot.isPresent() && blockSnapshot.get().getLocation().isPresent())
        {
            //TODO: This will not work cause we are removing container in break listener.

//            ContainerLocation containerLocation = new ContainerLocation(blockSnapshot.get().getLocation().get().getBlockPosition(), blockSnapshot.get().getWorldUniqueId());
//
//            if(ContainerManager.getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(containerLocation) && x.shouldBeHiddenIfNoItems()))
//            {
//                event.setCancelled(true);
//            }
        }
    }
}
