package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.RefillableTileEntity;
import io.github.aquerr.chestrefill.managers.ChestManager;
import org.spongepowered.api.block.tileentity.TileEntity;
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
        if(event.getTargetBlock().getLocation().get().getTileEntity().isPresent())
        {
            if(ChestRefill.PlayersSelectionMode.containsKey(player.getUniqueId()))
            {
                TileEntity tileEntity = event.getTargetBlock().getLocation().get().getTileEntity().get();

                if (ChestManager.allowedTileEntityTypes.contains(tileEntity.getType()))
                {
                    RefillableTileEntity refillableTileEntity = RefillableTileEntity.fromTileEntity(tileEntity, player.getWorld().getUniqueId());

                    switch (ChestRefill.PlayersSelectionMode.get(player.getUniqueId()))
                    {
                        case CREATE:

                            if (!ChestManager.getRefillableTileEntities().stream().anyMatch(x->x.getTileEntityLocation().equals(refillableTileEntity.getTileEntityLocation())))
                            {
                                boolean didSucceed = ChestManager.addRefillableTileEntity(refillableTileEntity);

                                if (didSucceed)
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully created a refilling entity!"));
                                }
                                else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                            }
                            else
                            {
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This entity is already marked as a refilling entity"));
                            }

                            //Turn off chest mode. It will be more safe to turn it off and let the player turn it on again.
                            ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());

                            break;

                        case REMOVE:

                            if (ChestManager.getRefillableTileEntities().stream().anyMatch(x->x.getTileEntityLocation().equals(refillableTileEntity.getTileEntityLocation())))
                            {
                                boolean didSucceed = ChestManager.removeChest(refillableTileEntity.getTileEntityLocation());

                                if (didSucceed)
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully removed a refilling entity!"));
                                }
                                else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                            }
                            else
                            {
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This chest is not a refillable entity"));
                            }

                            //Turn off chest mode. It will be more safe to turn it off and let the player turn it on again.
                            ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());

                            break;

                        case UPDATE:

                            if (ChestManager.getRefillableTileEntities().stream().anyMatch(x->x.getTileEntityLocation().equals(refillableTileEntity.getTileEntityLocation())))
                            {
                                boolean didSucceed = ChestManager.updateRefillableEntity(refillableTileEntity);

                                if (didSucceed)
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully updated a refilling entity!"));
                                }
                                else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                            }
                            else
                            {
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This chest is not a refillable entity"));
                            }

                            //Turn off chest mode. It will be more safe to turn it off and let the player turn it on again.
                            ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());

                            break;

                        case TIME:

                            if (ChestManager.getRefillableTileEntities().stream().anyMatch(x->x.getTileEntityLocation().equals(refillableTileEntity.getTileEntityLocation())))
                            {
                                if (ChestRefill.EntityTimeChangePlayer.containsKey(player.getUniqueId()))
                                {
                                    int time = ChestRefill.EntityTimeChangePlayer.get(player.getUniqueId());

                                    boolean didSucceed = ChestManager.updateRefillingTime(refillableTileEntity.getTileEntityLocation(), time);

                                    if (didSucceed)
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully updated entity's refill time!"));
                                    }
                                    else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                                }
                                else
                                {
                                    RefillableTileEntity chestToView = ChestManager.getRefillableTileEntities().stream().filter(x->x.getTileEntityLocation().equals(refillableTileEntity.getTileEntityLocation())).findFirst().get();
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "This entity refills every ", TextColors.GREEN, chestToView.getRestoreTime(), TextColors.YELLOW, " seconds"));
                                }
                            }
                            else
                            {
                                player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This chest is not a refillable entity"));
                            }

                            //Turn off chest mode. It will be more safe to turn it off and let the player turn it on again.
                            ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());
                            if (ChestRefill.EntityTimeChangePlayer.containsKey(player.getUniqueId())) ChestRefill.EntityTimeChangePlayer.remove(player.getUniqueId());

                            break;
                    }
                }
            }
        }
    }
}
