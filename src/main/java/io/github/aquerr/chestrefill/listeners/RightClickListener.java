package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.RefillingChest;
import io.github.aquerr.chestrefill.managers.ChestManager;
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
            if(ChestRefill.PlayersChestMode.containsKey(player.getUniqueId()))
            {
                Chest chest = (Chest) event.getTargetBlock().getLocation().get().getTileEntity().get();
                RefillingChest refillingChest = RefillingChest.fromChest(chest, player.getWorld().getUniqueId());

                switch (ChestRefill.PlayersChestMode.get(player.getUniqueId()))
                {
                    case CREATE:

                        if (!ChestManager.getChests().stream().anyMatch(x->x.getChestLocation().equals(refillingChest.getChestLocation())))
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
                        break;

                    case REMOVE:

                        if (ChestManager.getChests().stream().anyMatch(x->x.getChestLocation().equals(refillingChest.getChestLocation())))
                        {
                            boolean didSucceed = ChestManager.removeChest(refillingChest.getChestLocation());

                            if (didSucceed)
                            {
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully removed a refilling chest!"));
                            }
                            else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This chest is not a refillable chest"));
                        }
                        break;

                    case UPDATE:

                        if (ChestManager.getChests().stream().anyMatch(x->x.getChestLocation().equals(refillingChest.getChestLocation())))
                        {
                            boolean didSucceed = ChestManager.updateChest(refillingChest);

                            if (didSucceed)
                            {
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully updated a refilling chest!"));
                            }
                            else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This chest is not a refillable chest"));
                        }
                        break;
                }
            }
        }
    }
}
