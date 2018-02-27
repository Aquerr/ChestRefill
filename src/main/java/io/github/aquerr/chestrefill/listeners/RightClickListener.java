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

                        //Turn off chest mode. It will be more safe to turn it off and let the player turn it on again.
                        ChestRefill.PlayersChestMode.remove(player.getUniqueId());

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

                        //Turn off chest mode. It will be more safe to turn it off and let the player turn it on again.
                        ChestRefill.PlayersChestMode.remove(player.getUniqueId());

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

                        //Turn off chest mode. It will be more safe to turn it off and let the player turn it on again.
                        ChestRefill.PlayersChestMode.remove(player.getUniqueId());

                        break;

                    case TIME:

                        if (ChestManager.getChests().stream().anyMatch(x->x.getChestLocation().equals(refillingChest.getChestLocation())))
                        {
                            if (ChestRefill.ChestTimeChangePlayer.containsKey(player.getUniqueId()))
                            {
                                int time = ChestRefill.ChestTimeChangePlayer.get(player.getUniqueId());

                                boolean didSucceed = ChestManager.updateChestTime(refillingChest.getChestLocation(), time);

                                if (didSucceed)
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully updated chest's refill time!"));
                                }
                                else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                            }
                            else
                            {
                                RefillingChest chestToView = ChestManager.getChests().stream().filter(x->x.getChestLocation().equals(refillingChest.getChestLocation())).findFirst().get();
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "This chest refills every ", TextColors.GREEN, chestToView.getRestoreTime(), TextColors.YELLOW, " seconds"));
                            }
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This chest is not a refillable chest"));
                        }

                        //Turn off chest mode. It will be more safe to turn it off and let the player turn it on again.
                        ChestRefill.PlayersChestMode.remove(player.getUniqueId());
                        if (ChestRefill.ChestTimeChangePlayer.containsKey(player.getUniqueId())) ChestRefill.ChestTimeChangePlayer.remove(player.getUniqueId());

                        break;
                }
            }
        }
    }
}
