package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.util.ModSupport;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

public class RightClickListener extends AbstractListener
{
    private static final TextComponent THIS_IS_NOT_A_REFILLABLE_CONTAINER = text("This is not a refillable container!");
    private static final TextComponent SOMETHING_WENT_WRONG = text("Something went wrong...");

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
    }

    @Listener
    public void onRefillableContainerEdit(final InteractBlockEvent.Secondary event, @Root final ServerPlayer player)
    {
        if(!ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.uniqueId()))
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

        ModeExecutionParams params = new ModeExecutionParams(player, refillableContainer, refillableContainerAtLocation);

        MODE_EXECUTORS.get(ChestRefill.PLAYER_CHEST_SELECTION_MODE.get(player.uniqueId())).accept(params);

        ChestRefill.PLAYER_CHEST_SELECTION_MODE.remove(player.uniqueId());
    }

    @Listener
    public void onRefillableContainerOpen(final InteractBlockEvent.Secondary event, @Root final ServerPlayer player)
    {
        if(ChestRefill.PLAYER_CHEST_SELECTION_MODE.containsKey(player.uniqueId()))
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
            if (!refillableContainer.getFirstOpenMessage().equals(empty()))
                player.sendMessage(refillableContainer.getFirstOpenMessage());

            refillableContainer.setHasBeenOpened(true);
            super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainer);
            return;
        }

        if (!refillableContainer.getOpenMessage().equals(empty()) || PlainTextComponentSerializer.plainText().serialize(refillableContainer.getOpenMessage()).equals(""))
        {
            player.sendMessage(linear(refillableContainer.getOpenMessage()));
        }
    }

    private void createRefillableContainer(ModeExecutionParams params)
    {
        RefillableContainer refillableContainer = params.getRefillableContainer();
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getServerPlayer();

        if(refillableContainerAtLocation != null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, text("This container is already marked as a refilling container!")));
        }
        else
        {
            if(ChestRefill.PLAYER_CHEST_NAME.containsKey(player.uniqueId()))
                refillableContainer.setName(ChestRefill.PLAYER_CHEST_NAME.get(player.uniqueId()));

            final boolean didSucceed = super.getPlugin().getContainerManager().addRefillableContainer(refillableContainer);
            if (didSucceed)
            {
                player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully created a refilling container!")));
            }
            else player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
    }

    private void removeRefillableContainer(ModeExecutionParams params)
    {
        RefillableContainer refillableContainer = params.getRefillableContainer();
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getServerPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
            return;
        }

        final boolean didSucceed = super.getPlugin().getContainerManager().removeRefillableContainer(refillableContainer.getContainerLocation());
        if(didSucceed)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully removed a refilling container!")));
        }
        else
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, SOMETHING_WENT_WRONG));
    }

    private void updateRefillableContainer(ModeExecutionParams params)
    {
        RefillableContainer refillableContainer = params.getRefillableContainer();
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getServerPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            refillableContainer.setItemProvider(refillableContainerAtLocation.getItemProvider());
            refillableContainer.setRestoreTime(refillableContainerAtLocation.getRestoreTime());
            refillableContainer.setName(refillableContainerAtLocation.getName());
            refillableContainer.setRequiredPermission(refillableContainerAtLocation.getRequiredPermission());
            refillableContainer.setHidingBlock(refillableContainerAtLocation.getHidingBlock());
            refillableContainer.setOpenMessage(refillableContainerAtLocation.getOpenMessage());
            final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainer);
            if(didSucceed)
            {
                player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully updated a refilling container!")));
            }
            else
                player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
    }

    private void updateTime(ModeExecutionParams params)
    {
        RefillableContainer refillableContainer = params.getRefillableContainer();
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getServerPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            if(ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.containsKey(player.uniqueId()))
            {
                final int time = ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.get(player.uniqueId());
                final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillingTime(refillableContainer.getContainerLocation(), time);

                if(didSucceed)
                {
                    player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully updated container's refill time!")));
                }
                else
                    player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
            }
            else
            {
                final ContainerLocation containerLocation1 = refillableContainer.getContainerLocation();
                RefillableContainer chestToView = super.getPlugin().getContainerManager().getRefillableContainers().stream().filter(x -> x.getContainerLocation().equals(containerLocation1)).findFirst().get();
                player.sendMessage(
                        linear(PLUGIN_PREFIX,
                                YELLOW, text("This container refills every "),
                                GREEN, text(chestToView.getRestoreTime()),
                                YELLOW, text(" seconds")));
            }
        }
        ChestRefill.CONTAINER_TIME_CHANGE_PLAYER.remove(player.uniqueId());
    }

    private void renameContainer(ModeExecutionParams params)
    {
        RefillableContainer refillableContainer = params.getRefillableContainer();
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getServerPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            final boolean didSucceed = super.getPlugin().getContainerManager().renameRefillableContainer(refillableContainer.getContainerLocation(), ChestRefill.PLAYER_CHEST_NAME.get(player.uniqueId()));
            if(didSucceed)
            {
                player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully updated a refilling container!")));
            }
            else
                player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
    }

    private void updateContainerOpenMessage(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getServerPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            refillableContainerAtLocation.setOpenMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(ChestRefill.PLAYER_CHEST_NAME.get(player.uniqueId())));
            final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainerAtLocation);
            if(didSucceed)
            {
                player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully updated a refilling container!")));
            }
            else
                player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
    }

    private void copyContainer(ModeExecutionParams params)
    {
        RefillableContainer refillableContainer = params.getRefillableContainer();
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getServerPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            ChestRefill.PLAYER_COPY_REFILLABLE_CONTAINER.put(player.uniqueId(), refillableContainer);
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Now select a new container which should behave in the same way!")));
        }

        ChestRefill.PLAYER_CHEST_SELECTION_MODE.put(player.uniqueId(), SelectionMode.AFTER_COPY);
    }

    private void afterCopyContainer(ModeExecutionParams params)
    {
        RefillableContainer refillableContainer = params.getRefillableContainer();
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getServerPlayer();

        final RefillableContainer copiedContainer = ChestRefill.PLAYER_COPY_REFILLABLE_CONTAINER.get(player.uniqueId());
        if(!copiedContainer.getContainerBlockType().equals(refillableContainer.getContainerBlockType()))
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, text("Containers must be of the same type!")));
        }

        copiedContainer.setContainerLocation(refillableContainer.getContainerLocation());
        boolean didSucceed;
        if(refillableContainerAtLocation != null)
        {
            didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(copiedContainer);
        }
        else
        {
            didSucceed = super.getPlugin().getContainerManager().addRefillableContainer(copiedContainer);
        }

        if (didSucceed)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully copied a refilling container!")));
        }
        else
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
        ChestRefill.PLAYER_COPY_REFILLABLE_CONTAINER.remove(player.uniqueId());
    }

    private void createKit(ModeExecutionParams params)
    {
        RefillableContainer refillableContainer = params.getRefillableContainer();
        ServerPlayer player = params.getServerPlayer();

        Kit kit = new Kit(ChestRefill.PLAYER_KIT_NAME.get(player.uniqueId()), refillableContainer.getItems());
        boolean didSucceed = super.getPlugin().getContainerManager().createKit(kit);
        if (didSucceed)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully created a kit!")));
        }
        else player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));

        ChestRefill.PLAYER_KIT_NAME.remove(player.uniqueId());
    }

    private void assignKit(ModeExecutionParams params)
    {
        RefillableContainer refillableContainer = params.getRefillableContainer();
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getServerPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            final boolean didSucceed = super.getPlugin().getContainerManager().assignKit(refillableContainer.getContainerLocation(), ChestRefill.PLAYER_KIT_ASSIGN.get(player.uniqueId()));
            if(didSucceed)
            {
                player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully assigned a kit to the refilling container!")));
            }
            else
                player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
        ChestRefill.PLAYER_KIT_ASSIGN.remove(player.uniqueId());
    }

    private void assignLootTable(ModeExecutionParams params)
    {
        RefillableContainer refillableContainer = params.getRefillableContainer();
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getServerPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
        }
        else
        {
            final boolean didSucceed = super.getPlugin().getContainerManager().assignLootTable(refillableContainer.getContainerLocation(), ChestRefill.PLAYER_LOOT_TABLE_ASSIGN.get(player.uniqueId()));
            if(didSucceed)
            {
                player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully assigned a loot table to the refilling container!")));
            }
            else
                player.sendMessage(linear(PLUGIN_PREFIX, RED, SOMETHING_WENT_WRONG));
        }
        ChestRefill.PLAYER_LOOT_TABLE_ASSIGN.remove(player.uniqueId());
    }

    private void setPlaceItemsInRandomSlots(ModeExecutionParams params)
    {
        RefillableContainer refillableContainerAtLocation = params.getRefillableContainerAtLocation();
        ServerPlayer player = params.getServerPlayer();

        if(refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, text("This is not a refillable container!")));
        }
        else
        {
            refillableContainerAtLocation.setShouldPlaceItemsInRandomSlots(ChestRefill.CONTAINER_PLACE_ITEMS_IN_RANDOM_SLOTS.get(player.uniqueId()));
            final boolean didSucceed = super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainerAtLocation);
            if(didSucceed)
            {
                player.sendMessage(linear(PLUGIN_PREFIX, GREEN, text("Successfully updated a refilling container!")));
            }
            else
                player.sendMessage(linear(PLUGIN_PREFIX, RED, text("Something went wrong...")));
        }
        ChestRefill.CONTAINER_PLACE_ITEMS_IN_RANDOM_SLOTS.remove(player.uniqueId());
    }

    private static class ModeExecutionParams
    {
        private final RefillableContainer refillableContainer;
        private final RefillableContainer refillableContainerAtLocation;
        private final ServerPlayer serverPlayer;

        ModeExecutionParams(ServerPlayer serverPlayer, RefillableContainer refillableContainer, RefillableContainer refillableContainerAtLocation)
        {
            this.serverPlayer = serverPlayer;
            this.refillableContainer = refillableContainer;
            this. refillableContainerAtLocation = refillableContainerAtLocation;
        }

        public RefillableContainer getRefillableContainerAtLocation()
        {
            return refillableContainerAtLocation;
        }

        public RefillableContainer getRefillableContainer()
        {
            return refillableContainer;
        }

        public ServerPlayer getServerPlayer()
        {
            return serverPlayer;
        }
    }
}
