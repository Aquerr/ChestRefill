package io.github.aquerr.chestrefill.scheduling;

import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import io.github.aquerr.chestrefill.util.ModSupport;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.item.inventory.Inventory;
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
                final TileEntity tileEntity = location.getTileEntity().get();
                Inventory tileEntityInventory;
                if (ModSupport.isStorageUnitFromActuallyAdditions(tileEntity))
                    tileEntityInventory = ModSupport.getInventoryFromActuallyAdditions(tileEntity);
                else
                    tileEntityInventory = ((TileEntityCarrier)tileEntity).getInventory();
                if(tileEntityInventory.totalItems() == 0 && refillableContainer.shouldBeHiddenIfNoItems())
                {
                    location.setBlockType(refillableContainer.getHidingBlock());
                }
            }
        }
    }
}
