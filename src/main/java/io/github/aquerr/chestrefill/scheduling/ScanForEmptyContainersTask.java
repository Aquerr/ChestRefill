package io.github.aquerr.chestrefill.scheduling;

import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import io.github.aquerr.chestrefill.util.ModSupport;
import io.github.aquerr.chestrefill.util.WorldUtils;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

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
            final Optional<ServerWorld> world = WorldUtils.getWorldByUUID(refillableContainer.getContainerLocation().getWorldUUID());
            if(!world.isPresent())
                continue;

            final ServerLocation location = ServerLocation.of(world.get(), refillableContainer.getContainerLocation().getBlockPosition());

            if (!location.blockEntity().isPresent() && refillableContainer.shouldBeHiddenIfNoItems())
                continue;

            if(location.blockEntity().isPresent())
            {
                final BlockEntity blockEntity = location.blockEntity().get();
                Inventory tileEntityInventory;
                if (ModSupport.isStorageUnitFromActuallyAdditions(blockEntity))
                    tileEntityInventory = ModSupport.getInventoryFromActuallyAdditions(blockEntity);
                else
                    tileEntityInventory = ((CarrierBlockEntity)blockEntity).inventory();
                if(tileEntityInventory.totalQuantity() == 0 && refillableContainer.shouldBeHiddenIfNoItems())
                {
                    location.setBlockType(refillableContainer.getHidingBlock());
                }
            }
        }
    }
}
