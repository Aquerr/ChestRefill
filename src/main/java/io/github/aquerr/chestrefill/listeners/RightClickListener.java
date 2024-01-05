package io.github.aquerr.chestrefill.listeners;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.ModeExecutionParams;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.SelectionMode;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.util.ModSupport;
import joptsimple.internal.Strings;
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

import java.util.EnumSet;
import java.util.Optional;

import static io.github.aquerr.chestrefill.PluginInfo.PLUGIN_PREFIX;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class RightClickListener extends AbstractListener
{
    private static final TextComponent THIS_IS_NOT_A_REFILLABLE_CONTAINER = text("This is not a refillable container!");

    public RightClickListener(ChestRefill plugin)
    {
        super(plugin);
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
        if (isContainerRequiredAtPosition(selectionParams) && refillableContainerAtLocation == null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, THIS_IS_NOT_A_REFILLABLE_CONTAINER));
            return;
        }
        else if (!isContainerRequiredAtPosition(selectionParams) && refillableContainerAtLocation != null)
        {
            player.sendMessage(linear(PLUGIN_PREFIX, RED, text("This container is already marked as a refilling container!")));
            return;
        }

        ModeExecutionParams params = new ModeExecutionParams(player, refillableContainer, refillableContainerAtLocation, selectionParams.getExtraData());

        selectionParams.getExecutor().accept(params);
    }

    private boolean isContainerRequiredAtPosition(SelectionParams selectionParams)
    {
        return !EnumSet.of(SelectionMode.CREATE, SelectionMode.CREATE_KIT).contains(selectionParams.getSelectionMode());
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
            if (!Strings.isNullOrEmpty(refillableContainer.getFirstOpenMessage()))
                player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(refillableContainer.getFirstOpenMessage()));

            refillableContainer.setHasBeenOpened(true);
            super.getPlugin().getContainerManager().updateRefillableContainer(refillableContainer);
            return;
        }

        if (!Strings.isNullOrEmpty(refillableContainer.getOpenMessage()) || LegacyComponentSerializer.legacyAmpersand().deserialize(refillableContainer.getOpenMessage()).content().equals(""))
        {
            player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(refillableContainer.getOpenMessage()));
        }
    }
}
