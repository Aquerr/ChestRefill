package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
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
        if(ChestRefill.PlayersSelectionMode.containsKey(player.getUniqueId()))
        {
            if (event.getTargetBlock().getLocation().isPresent())
            {
                if(event.getTargetBlock().getLocation().get().getTileEntity().isPresent())
                {
                    TileEntity tileEntity = event.getTargetBlock().getLocation().get().getTileEntity().get();

                    if (tileEntity instanceof TileEntityCarrier)
                    {
                        RefillableContainer refillableContainer = RefillableContainer.fromTileEntity(tileEntity, player.getWorld().getUniqueId());

                        switch (ChestRefill.PlayersSelectionMode.get(player.getUniqueId()))
                        {
                            case CREATE:
                                if (!ContainerManager.getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                {
                                    boolean didSucceed = ContainerManager.addRefillableContainer(refillableContainer);

                                    if (didSucceed)
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully created a refilling container!"));
                                    }
                                    else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                                }
                                else
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This container is already marked as a refilling container!"));
                                }

                                //Turn off selection mode. It will be more safe to turn it off and let the player turn it on again.
                                ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());

                                break;

                            case REMOVE:

                                if (ContainerManager.getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                {
                                    boolean didSucceed = ContainerManager.removeRefillableContainer(refillableContainer.getContainerLocation());

                                    if (didSucceed)
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully removed a refilling container!"));
                                    }
                                    else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                                }
                                else
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This is not a refillable container!"));
                                }

                                //Turn off selection mode. It will be more safe to turn it off and let the player turn it on again.
                                ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());

                                break;

                            case UPDATE:

                                if (ContainerManager.getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                {
                                    boolean didSucceed = ContainerManager.updateRefillableContainer(refillableContainer);

                                    if (didSucceed)
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully updated a refilling container!"));
                                    }
                                    else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                                }
                                else
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This is not a refillable container!"));
                                }

                                //Turn off selection mode. It will be more safe to turn it off and let the player turn it on again.
                                ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());

                                break;

                            case TIME:
                                if (ContainerManager.getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                {
                                    if (ChestRefill.ContainerTimeChangePlayer.containsKey(player.getUniqueId()))
                                    {
                                        int time = ChestRefill.ContainerTimeChangePlayer.get(player.getUniqueId());

                                        boolean didSucceed = ContainerManager.updateRefillingTime(refillableContainer.getContainerLocation(), time);

                                        if (didSucceed)
                                        {
                                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully updated container's refill time!"));
                                        }
                                        else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                                    }
                                    else
                                    {
                                        RefillableContainer chestToView = ContainerManager.getRefillableContainers().stream().filter(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())).findFirst().get();
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "This container refills every ", TextColors.GREEN, chestToView.getRestoreTime(), TextColors.YELLOW, " seconds"));
                                    }
                                }
                                else
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This is not a refillable container!"));
                                }

                                //Turn off selection mode. It will be more safe to turn it off and let the player turn it on again.
                                ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());
                                if (ChestRefill.ContainerTimeChangePlayer.containsKey(player.getUniqueId())) ChestRefill.ContainerTimeChangePlayer.remove(player.getUniqueId());

                                break;
                        }
                    }
                }
            }
        }
    }
}
