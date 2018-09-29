package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
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

public class RightClickListener extends AbstractListener
{
    public RightClickListener(ChestRefill plugin)
    {
        super(plugin);
    }

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
                                if (!super.getPlugin().getContainerManager().getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                {
                                    if(ChestRefill.PlayerChestName.containsKey(player.getUniqueId()))
                                        refillableContainer.setName(ChestRefill.PlayerChestName.get(player.getUniqueId()));

                                    boolean didSucceed = super.getPlugin().getContainerManager().addRefillableContainer(refillableContainer);
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

                                //Turns off selection mode. It will be more safe to turn it off and let the player turn it on again.
                                ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());
                                ChestRefill.PlayerChestName.remove(player.getUniqueId());
                                break;

                            case REMOVE:

                                if (super.getPlugin().getContainerManager().getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                {
                                    boolean didSucceed = super.getPlugin().getContainerManager().removeRefillableContainer(refillableContainer.getContainerLocation());

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

                                if (super.getPlugin().getContainerManager().getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                {
                                    boolean didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainer);

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
                                if (super.getPlugin().getContainerManager().getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                {
                                    if (ChestRefill.ContainerTimeChangePlayer.containsKey(player.getUniqueId()))
                                    {
                                        int time = ChestRefill.ContainerTimeChangePlayer.get(player.getUniqueId());

                                        boolean didSucceed = super.getPlugin().getContainerManager().updateRefillingTime(refillableContainer.getContainerLocation(), time);

                                        if (didSucceed)
                                        {
                                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully updated container's refill time!"));
                                        }
                                        else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                                    }
                                    else
                                    {
                                        RefillableContainer chestToView = super.getPlugin().getContainerManager().getRefillableContainers().stream().filter(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())).findFirst().get();
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "This container refills every ", TextColors.GREEN, chestToView.getRestoreTime(), TextColors.YELLOW, " seconds"));
                                    }
                                }
                                else
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This is not a refillable container!"));
                                }

                                //Turn off selection mode. It will be more safe to turn it off and let the player turn it on again.
                                ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());
                                ChestRefill.ContainerTimeChangePlayer.remove(player.getUniqueId());

                                break;

                            case SET_NAME:
                                if (super.getPlugin().getContainerManager().getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                {
                                    boolean didSucceed = super.getPlugin().getContainerManager().renameRefillableContainer(refillableContainer.getContainerLocation(), ChestRefill.PlayerChestName.get(player.getUniqueId()));

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

                            case COPY:
                                if(!ChestRefill.PlayerCopyRefillableContainer.containsKey(player.getUniqueId()))
                                {
                                    if (super.getPlugin().getContainerManager().getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                    {
                                        ChestRefill.PlayerCopyRefillableContainer.put(player.getUniqueId(), refillableContainer);
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Now select a new container which should behave in the same way!"));
                                        break;
                                    }
                                    else
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This is not a refillable container!"));
                                    }
                                }
                                else
                                {
                                    RefillableContainer copiedContainer = ChestRefill.PlayerCopyRefillableContainer.get(player.getUniqueId());

                                    if(!copiedContainer.getContainerBlockType().equals(refillableContainer.getContainerBlockType()))
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Containers must be of the same type!"));
                                        break;
                                    }

                                    copiedContainer.setContainerLocation(refillableContainer.getContainerLocation());
                                    boolean didSucceed;
                                    if (super.getPlugin().getContainerManager().getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                    {
                                        didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(copiedContainer);
                                    }
                                    else
                                    {
                                        didSucceed = super.getPlugin().getContainerManager().addRefillableContainer(copiedContainer);
                                    }

                                    if (didSucceed)
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully copied a refilling container!"));
                                    }
                                    else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));

                                    ChestRefill.PlayerCopyRefillableContainer.remove(player.getUniqueId());
                                }

                                //Turn off selection mode. It will be more safe to turn it off and let the player turn it on again.
                                ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());
                                break;

                            case CREATE_KIT:
                            {
                                Kit kit = new Kit(ChestRefill.PlayerKitName.get(player.getUniqueId()), refillableContainer.getItems());
                                boolean didSucceed = super.getPlugin().getContainerManager().createKit(kit);
                                if (didSucceed)
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully created a kit!"));
                                }
                                else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));

                                //Turns off selection mode. It will be more safe to turn it off and let the player turn it on again.
                                ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());
                                ChestRefill.PlayerKitName.remove(player.getUniqueId());
                                break;
                            }

                            case ASSIGN_KIT:
                                if (super.getPlugin().getContainerManager().getRefillableContainers().stream().anyMatch(x->x.getContainerLocation().equals(refillableContainer.getContainerLocation())))
                                {
                                    boolean didSucceed = super.getPlugin().getContainerManager().assignKit(refillableContainer.getContainerLocation(), ChestRefill.PlayerKitAssign.get(player.getUniqueId()));

                                    if (didSucceed)
                                    {
                                        player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.GREEN, "Successfully assigned a kit to the refilling container!"));
                                    }
                                    else player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "Something went wrong..."));
                                }
                                else
                                {
                                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.RED, "This is not a refillable container!"));
                                }

                                //Turn off selection mode. It will be more safe to turn it off and let the player turn it on again.
                                ChestRefill.PlayersSelectionMode.remove(player.getUniqueId());
                                ChestRefill.PlayerKitAssign.remove(player.getUniqueId());
                                break;
                        }
                    }
                }
            }
        }
    }
}
