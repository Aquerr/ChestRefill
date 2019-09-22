package io.github.aquerr.chestrefill.scheduling;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class ScanForEmptyContainersTask implements Runnable
{
    private final ContainerManager containerManager;

    public ScanForEmptyContainersTask(final ContainerManager containerManager)
    {
        this.containerManager = containerManager;
    }

    @Override
    public void run()
    {
        for(final RefillableContainer refillableContainer : this.containerManager.getRefillableContainers())
        {
            final Optional<World> world =  Sponge.getServer().getWorld(refillableContainer.getContainerLocation().getWorldUUID());
            if(!world.isPresent())
                continue;

            final Location<World> location = new Location<>(world.get(), refillableContainer.getContainerLocation().getBlockPosition());

            if (!location.getTileEntity().isPresent() && refillableContainer.shouldBeHiddenIfNoItems())
                continue;

            if(location.getTileEntity().isPresent())
            {
                final TileEntityCarrier container = (TileEntityCarrier) location.getTileEntity().get();
                if(container.getInventory().totalItems() == 0 && refillableContainer.shouldBeHiddenIfNoItems())
                {
                    location.setBlockType(refillableContainer.getHidingBlock(), Cause.builder().named(NamedCause.OWNER, ChestRefill.getInstance()).build());
                }
            }
        }
    }
}
