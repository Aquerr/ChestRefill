package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.util.ModSupport;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.world.Location;

import java.util.EnumMap;
import java.util.Optional;
import java.util.function.Consumer;

import static io.github.aquerr.chestrefill.PluginInfo.PLUGIN_PREFIX;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class RightClickListener extends AbstractListener
{
    private static final TextComponent THIS_IS_NOT_A_REFILLABLE_CONTAINER = text("This is not a refillable container!");

    private final EnumMap<SelectionMode, Consumer<ModeExecutionParams>> MODE_EXECUTORS = new EnumMap<>(SelectionMode.class);

    public RightClickListener(ChestRefill plugin)
    {
        super(plugin);

        MODE_EXECUTORS.put(SelectionMode.CREATE, this::createRefillableContainer);
        MODE_EXECUTORS.put(SelectionMode.REMOVE, this::removeRefillableContainer);
        MODE_EXECUTORS.put(SelectionMode.UPDATE, this::updateRefillableContainer);
        MODE_EXECUTORS.put(SelectionMode.TIME, this::updateTime);
        MODE_EXECUTORS.put(SelectionMode.SET_CONTAINER_NAME, this::renameContainer);
        MODE_EXECUTORS.put(SelectionMode.SET_OPEN_MESSAGE, this::updateContainerOpenMessage);
        MODE_EXECUTORS.put(SelectionMode.COPY, this::copyContainer);
        MODE_EXECUTORS.put(SelectionMode.AFTER_COPY, this::afterCopyContainer);
        MODE_EXECUTORS.put(SelectionMode.CREATE_KIT, this::createKit);
        MODE_EXECUTORS.put(SelectionMode.ASSIGN_KIT, this::assignKit);
        MODE_EXECUTORS.put(SelectionMode.ASSIGN_LOOT_TABLE, this::assignLootTable);
        MODE_EXECUTORS.put(SelectionMode.SET_PLACE_ITEMS_IN_RANDOM_SLOTS, this::setPlaceItemsInRandomSlots);
        MODE_EXECUTORS.put(SelectionMode.SET_HIDDEN_IF_NO_ITEMS, this::setHiddenIfNoItems);
    }

    @Listener
    public void onRefillableContainerEdit(final InteractBlockEvent.Secondary event, @Root final ServerPlayer player)
    {
        if(!ChestRefill.SELECTION_MODE.containsKey(player.uniqueId()))
            return;

        if(!event.block().location().flatMap(Location::blockEntity).isPresent())
            return;

        final BlockEntity blockEntity = event.block().location().get().blockEntity().get();

        RefillableContainer refillableContainer;
        if (blockEntity instanceof CarrierBlockEntity)
        {
            refillableContainer = RefillableContainer.fromBlockEntity((CarrierBlockEntity) blockEntity, player.world().uniqueId());
        }
        else if (ModSupport.isStorageUnitFromActuallyAdditions(blockEntity))
        {
            final Inventory inventory = ModSupport.getInventoryFromActuallyAdditions(blockEntity);
            if (inventory == null)
            {
                getPlugin().getLogger().error("Could not convert Actually Additions storage to inventory!");
                return;
            }
            refillableContainer = RefillableContainer.fromInventory(inventory, blockEntity.block().type(), blockEntity.serverLocation().blockPosition(), player.world().uniqueId());
        }
        else
        {
            return;
        }

        final ContainerLocation containerLocation = new ContainerLocation(blockEntity.locatableBlock().blockPosition(), player.world().uniqueId());
        final RefillableContainer refillableContainerAtLocation = super.getPlugin().getContainerManager().getRefillableContainerAtLocation(containerLocation)
                .orElse(null);

        SelectionParams selectionParams = ChestRefill.SELECTION_MODE.get(player.uniqueId());
        ModeExecutionParams params = new ModeExecutionParams(player, refillableContainer, refillableContainerAtLocation, selectionParams.getExtraData());

        MODE_EXECUTORS.get(selectionParams.getSelectionMode()).accept(params);
    }

    @Listener
    public void onRefillableContainerOpen(final InteractBlockEvent.Secondary event, @Root final ServerPlayer player)
    {
        if(ChestRefill.SELECTION_MODE.containsKey(player.uniqueId()))
            return;

        if(!event.block().location().flatMap(Location::blockEntity).isPresent())
            return;

        final BlockEntity tileEntity = event.block().location()
                .flatMap(Location::blockEntity)
                .orElse(null);
        if (!(tileEntity instanceof CarrierBlockEntity))
            return;

        final ContainerLocation containerLocation = new ContainerLocation(tileEntity.locatableBlock().blockPosition(), player.world().uniqueId());
        final Optional<RefillableContainer> optionalContainerAtLocation = super.getPlugin().getContainerManager().getRefillableContainerAtLocation(containerLocation);

        if (!optionalContainerAtLocation.isPresent())
            return;

        final RefillableContainer refillableContainer = optionalContainerAtLocation.get();
        if(!refillableContainer.hasPermissionToOpen(player))
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, text("You don't have permissions to open this chest!")));
            event.setCancelled(true);
            return;
        }

        if (!refillableContainer.hasBeenOpened())
        {
            if (!"".equals(refillableContainer.getFirstOpenMessage()))
                player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(refillableContainer.getFirstOpenMessage()));

            refillableContainer.setHasBeenOpened(true);
            super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainer);
            return;
        }

        if (!"".equals(refillableContainer.getOpenMessage()) || LegacyComponentSerializer.legacyAmpersand().deserialize(refillableContainer.getOpenMessage()).equals(""))
        {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(refillableContainer.getOpenMessage()));
        }
    }

    private void createRefillableContainer(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getPlayer();

        if(refillableContainerAtLocation != null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, text("This container is already marked as a refilling container!")));
        }
        else
        {
            ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }

    private void removeRefillableContainer(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
            return;
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }

    private void updateRefillableContainer(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }

    private void updateTime(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }

    private void renameContainer(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }

    private void updateContainerOpenMessage(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }

    private void copyContainer(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        }
    }

    private void afterCopyContainer(ModeExecutionParams params)
    {
        ServerPlayer player = params.getPlayer();
        ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }

    private void createKit(ModeExecutionParams params)
    {
        ServerPlayer player = params.getPlayer();
        ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }

    private void assignKit(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }

    private void assignLootTable(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }

    private void setPlaceItemsInRandomSlots(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, text("This is not a refillable container!")));
        }
        else
        {
            ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }

    private void setHiddenIfNoItems(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, text("This is not a refillable container!")));
        }
        else
        {
            ChestRefill.SELECTION_MODE.get(player.uniqueId()).getExecutor().accept(params);
        }
        ChestRefill.SELECTION_MODE.remove(player.uniqueId());
    }
}
