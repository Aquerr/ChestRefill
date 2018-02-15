package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ChestLocation;
import io.github.aquerr.chestrefill.entities.RefillingChest;
import io.github.aquerr.chestrefill.managers.ChestManager;
import io.github.aquerr.chestrefill.storage.JSONChestStorage;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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

                //TODO: This is bad. Change location to coordinates and world UUID.

                RefillingChest refillingChest = RefillingChest.fromChest(chest, player.getWorld().getUniqueId());

                if (!ChestManager.getChests().contains(refillingChest))
                {
                    boolean didSucceed = ChestManager.addChest(refillingChest);

                    if (didSucceed)
                    {
                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully created a refilling chest!"));
                    }
                    else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This chest is already marked as a refilling chest"));
                }

                //Turn creation mode off
                ChestRefill.ChestCreationPlayers.remove(player.getUniqueId());
            }
        }
    }
}
