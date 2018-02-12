package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.config.ChestConfig;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.text.Text;

/**
 * Created by Aquerr on 2018-02-10.
 */

public class RightClickListener
{
    @Listener
    public void onRightClick(InteractBlockEvent.Secondary event, @Root Player player)
    {
        if(event.getTargetBlock().getState().getType().equals(BlockTypes.CHEST))
        {
            if(ChestRefill.ChestCreationPlayers.contains(player.getUniqueId()))
            {
                Chest chest = (Chest) event.getTargetBlock().getLocation().get().getTileEntity().get();

                //TODO: Serialize chest if player is in the "creation mode"


                ChestConfig.addChest(chest);

                //Turn creation mode off
                ChestRefill.ChestCreationPlayers.remove(player.getUniqueId());
            }
        }
    }
}
