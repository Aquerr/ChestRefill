package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.util.ModSupport;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.BlockCarrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    public void onRefillableContainerEdit(final InteractBlockEvent.Secondary event, @Root final Player player)
    {
        if(!ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.getUniqueId()))
            return;

        if(!event.getTargetBlock().getLocation().isPresent())
            return;

        if(!event.getTargetBlock().getLocation().get().getTileEntity().isPresent())
            return;

        final TileEntity tileEntity = event.getTargetBlock().getLocation().get().getTileEntity().get();

        RefillableContainer refillableContainer;
        if (tileEntity instanceof TileEntityCarrier)
        {
            refillableContainer = RefillableContainer.fromTileEntity(tileEntity, player.getWorld().getUniqueId());
        }
        else if (ModSupport.isStorageUnitFromActuallyAdditions(tileEntity))
        {
            final Inventory inventory = ModSupport.getInventoryFromActuallyAdditions(tileEntity);
            refillableContainer = RefillableContainer.fromInventory(inventory, tileEntity.getBlock().getType(), tileEntity.getLocation().getBlockPosition(), player.getWorld().getUniqueId());
        }
        else return;

        final ContainerLocation containerLocation = new ContainerLocation(tileEntity.getLocatableBlock().getPosition(), player.getWorld().getUniqueId());
        final Optional<RefillableContainer> optionalRefillableContainerAtLocation = super.getPlugin().getContainerManager().getRefillableContainerAtLocation(containerLocation);

        switch (ChestRefill.PLAYER_CHEST_SELECTION_MODE.get(player.getUniqueId()))
        {
            case CREATE:
                if(optionalRefillableContainerAtLocation.isPresent())
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "This container is already marked as a refilling container!"));
                }
                else
                {
                    if(ChestRefill.PLAYER_CHEST_NAME.containsKey(player.getUniqueId()))
                        refillableContainer.setName(ChestRefill.PLAYER_CHEST_NAME.get(player.getUniqueId()));

                    final boolean didSucceed = super.getPlugin().getContainerManager().addRefillableContainer(refillableContainer);
                    if (didSucceed)
                    {
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Successfully created a refilling container!"));
                    }
                    else player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Something went wrong..."));
                }
                break;

            case REMOVE:
            {
                if(!optionalRefillableContainerAtLocation.isPresent())
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "This is not a refillable container!"));
                }
                else
                {
                    final boolean didSucceed = super.getPlugin().getContainerManager().removeRefillableContainer(refillableContainer.getContainerLocation());
                    if(didSucceed)
                    {
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Successfully removed a refilling container!"));
                    }
                    else
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Something went wrong..."));
                }
                break;
            }

            case UPDATE:
            {
                if(!optionalRefillableContainerAtLocation.isPresent())
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "This is not a refillable container!"));

                }
                else
                {
                    final RefillableContainer refillableContainerAtLocation = optionalRefillableContainerAtLocation.get();
                    refillableContainer.setKit(refillableContainerAtLocation.getKitName());
                    refillableContainer.setRestoreTime(refillableContainerAtLocation.getRestoreTime());
                    refillableContainer.setName(refillableContainerAtLocation.getName());
                    refillableContainer.setRequiredPermission(refillableContainerAtLocation.getRequiredPermission());
                    refillableContainer.setHidingBlock(refillableContainerAtLocation.getHidingBlock());
                    final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainer);
                    if(didSucceed)
                    {
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Successfully updated a refilling container!"));
                    }
                    else
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Something went wrong..."));
                }
                break;
            }

            case TIME:
            {
                if(!optionalRefillableContainerAtLocation.isPresent())
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "This is not a refillable container!"));
                }
                else
                {
                    if(ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.containsKey(player.getUniqueId()))
                    {
                        final int time = ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.get(player.getUniqueId());
                        final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillingTime(refillableContainer.getContainerLocation(), time);

                        if(didSucceed)
                        {
                            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Successfully updated container's refill time!"));
                        }
                        else
                            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Something went wrong..."));
                    }
                    else
                    {
                        final ContainerLocation containerLocation1 = refillableContainer.getContainerLocation();
                        RefillableContainer chestToView = super.getPlugin().getContainerManager().getRefillableContainers().stream().filter(x -> x.getContainerLocation().equals(containerLocation1)).findFirst().get();
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "This container refills every ", TextColors.GREEN, chestToView.getRestoreTime(), TextColors.YELLOW, " seconds"));
                    }
                }
                ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.remove(player.getUniqueId());
                break;
            }

            case SET_NAME:
            {
                if(!optionalRefillableContainerAtLocation.isPresent())
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "This is not a refillable container!"));
                }
                else
                {
                    final boolean didSucceed = super.getPlugin().getContainerManager().renameRefillableContainer(refillableContainer.getContainerLocation(), ChestRefill.PLAYER_CHEST_NAME.get(player.getUniqueId()));
                    if(didSucceed)
                    {
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Successfully updated a refilling container!"));
                    }
                    else
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Something went wrong..."));
                }
                break;
            }

            case COPY:
            {
                if(!optionalRefillableContainerAtLocation.isPresent())
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "This is not a refillable container!"));
                }
                else
                {
                    ChestRefill.PLAYER_COPY_REFILLABLE_CONTAINER.put(player.getUniqueId(), refillableContainer);
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Now select a new container which should behave in the same way!"));
                    break;
                }
                break;
            }

            case AFTER_COPY:
            {
                final RefillableContainer copiedContainer = ChestRefill.PLAYER_COPY_REFILLABLE_CONTAINER.get(player.getUniqueId());
                if(!copiedContainer.getContainerBlockType().equals(refillableContainer.getContainerBlockType()))
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Containers must be of the same type!"));
                    break;
                }

                copiedContainer.setContainerLocation(refillableContainer.getContainerLocation());
                boolean didSucceed;
                if(optionalRefillableContainerAtLocation.isPresent())
                {
                    didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(copiedContainer);
                }
                else
                {
                    didSucceed = super.getPlugin().getContainerManager().addRefillableContainer(copiedContainer);
                }

                if (didSucceed)
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Successfully copied a refilling container!"));
                }
                else player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Something went wrong..."));
                ChestRefill.PLAYER_COPY_REFILLABLE_CONTAINER.remove(player.getUniqueId());
                break;
            }

            case CREATE_KIT:
            {
                Kit kit = new Kit(ChestRefill.PLAYER_KIT_NAME.get(player.getUniqueId()), refillableContainer.getItems());
                boolean didSucceed = super.getPlugin().getContainerManager().createKit(kit);
                if (didSucceed)
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Successfully created a kit!"));
                }
                else player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Something went wrong..."));

                ChestRefill.PLAYER_KIT_NAME.remove(player.getUniqueId());
                break;
            }

            case ASSIGN_KIT:
            {
                if(!optionalRefillableContainerAtLocation.isPresent())
                {
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "This is not a refillable container!"));
                }
                else
                {
                    final boolean didSucceed = super.getPlugin().getContainerManager().assignKit(refillableContainer.getContainerLocation(), ChestRefill.PLAYER_KIT_ASSIGN.get(player.getUniqueId()));
                    if(didSucceed)
                    {
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "Successfully assigned a kit to the refilling container!"));
                    }
                    else
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "Something went wrong..."));
                }
                ChestRefill.PLAYER_KIT_ASSIGN.remove(player.getUniqueId());
                break;
            }
        }

        if(ChestRefill.PLAYER_CHEST_SELECTION_MODE.get(player.getUniqueId()) == SelectionMode.COPY)
            ChestRefill.PLAYER_CHEST_SELECTION_MODE.put(player.getUniqueId(), SelectionMode.AFTER_COPY);
        ChestRefill.PLAYER_CHEST_SELECTION_MODE.remove(player.getUniqueId());
    }

    @Listener
    public void onRefillableContainerOpen(final InteractBlockEvent.Secondary event, @Root final Player player)
    {
        if(ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.getUniqueId()))
            return;

        if(!event.getTargetBlock().getLocation().isPresent())
            return;

        if(!event.getTargetBlock().getLocation().get().getTileEntity().isPresent())
            return;

        final TileEntity tileEntity = event.getTargetBlock().getLocation().get().getTileEntity().get();
        if (!(tileEntity instanceof TileEntityCarrier))
            return;

        final ContainerLocation containerLocation = new ContainerLocation(tileEntity.getLocatableBlock().getPosition(), player.getWorld().getUniqueId());
        final Optional<RefillableContainer> optionalContainerAtLocation = super.getPlugin().getContainerManager().getRefillableContainerAtLocation(containerLocation);

        if(optionalContainerAtLocation.isPresent())
        {
            final RefillableContainer refillableContainer = optionalContainerAtLocation.get();
            if(!refillableContainer.getRequiredPermission().equals("") && !player.hasPermission(refillableContainer.getRequiredPermission()))
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "You don't have permissions to open this chest!"));
                event.setCancelled(true);
                return;
            }
        }
    }
}
