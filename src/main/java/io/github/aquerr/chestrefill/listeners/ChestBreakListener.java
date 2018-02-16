package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ChestLocation;
import io.github.aquerr.chestrefill.entities.RefillingChest;
import io.github.aquerr.chestrefill.managers.ChestManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableCareerData;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableInventoryItemData;
import org.spongepowered.api.data.value.mutable.CompositeValueStore;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.TileEntityInventory;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
