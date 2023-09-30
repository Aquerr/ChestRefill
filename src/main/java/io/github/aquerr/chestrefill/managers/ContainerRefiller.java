package io.github.aquerr.chestrefill.managers;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ItemProviderType;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import io.github.aquerr.chestrefill.util.LootTableHelper;
import io.github.aquerr.chestrefill.util.ModSupport;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.block.entity.carrier.chest.Chest;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.github.aquerr.chestrefill.util.WorldUtils.getWorldByUUID;

public class ContainerRefiller
{
    private final ChestRefill plugin;
    private final ContainerManager containerManager;
    private final LootTableHelper lootTableHelper;

    public ContainerRefiller(ChestRefill plugin, ContainerManager containerManager, LootTableHelper lootTableHelper)
    {
        this.plugin = plugin;
        this.containerManager = containerManager;
        this.lootTableHelper = lootTableHelper;
    }

    public void refillContainer(RefillableContainer refillableContainer)
    {
        final ServerWorld world =  getWorldByUUID(refillableContainer.getContainerLocation().getWorldUUID()).orElse(null);
        if (world == null)
        {
            this.plugin.getLogger().error(String.format("World with UUID = '%s' does not exist!", refillableContainer.getContainerLocation().getWorldUUID().toString()));
            return;
        }

        final ServerLocation location = ServerLocation.of(world, refillableContainer.getContainerLocation().getBlockPosition());

        //If chest is hidden then we need to show it
        showContainer(refillableContainer, location);

        Inventory containerInventory = getContainerInventory(location);
        if (containerInventory == null)
            throw new IllegalStateException("Container does not have inventory!");


        if (refillableContainer.shouldReplaceExistingItems())
        {
            containerInventory.clear();
        }

        final List<RefillableItem> itemsToRefill = getItemsToRefill(refillableContainer, world);
        insertItemsInContainer(refillableContainer, containerInventory, itemsToRefill);
        hideContainer(refillableContainer, containerInventory, location);
    }

    private void insertItemsInContainer(RefillableContainer refillableContainer, Inventory containerInventory, List<RefillableItem> itemsToRefill)
    {
        if (!itemsToRefill.isEmpty())
        {
            if (refillableContainer.shouldPlaceItemsInRandomSlots())
            {
                insertItemsInRandomSlots(containerInventory, itemsToRefill, refillableContainer.shouldRefillOneItemAtTime());
            }
            else
            {
                insertItems(containerInventory, itemsToRefill, refillableContainer.shouldRefillOneItemAtTime());
            }
        }
    }

    private void showContainer(RefillableContainer refillableContainer, ServerLocation location)
    {
        if (!location.blockEntity().isPresent() && refillableContainer.shouldBeHiddenIfNoItems())
        {
            location.setBlockType(refillableContainer.getContainerBlockType());
        }
    }

    private Inventory getContainerInventory(ServerLocation containerLocation)
    {
        if (!containerLocation.blockEntity().isPresent())
            return null;

        final BlockEntity blockEntity = containerLocation.blockEntity().get();
        Inventory blockEntityInventory;
        if (ModSupport.isStorageUnitFromActuallyAdditions(blockEntity))
            blockEntityInventory = ModSupport.getInventoryFromActuallyAdditions(blockEntity);
        else
        {
            final CarrierBlockEntity carrierBlockEntity = (CarrierBlockEntity) blockEntity;
            blockEntityInventory = carrierBlockEntity.inventory();
            if (carrierBlockEntity instanceof Chest)
                blockEntityInventory = ((Chest) carrierBlockEntity).doubleChestInventory().orElse(blockEntityInventory);
        }
        return blockEntityInventory;
    }

    private List<RefillableItem> getItemsToRefill(RefillableContainer container, ServerWorld serverWorld)
    {
        List<RefillableItem> items = new ArrayList<>();
        if (container.getItemProvider().getType() == ItemProviderType.SELF)
        {
            items = getRandomItems(container.getItems());
        }
        else if (container.getItemProvider().getType() == ItemProviderType.KIT)
        {
            items = getRandomItems(this.containerManager.getKit(container.getItemProvider().getLocation()).getItems());
        }
        else if (container.getItemProvider().getType() == ItemProviderType.LOOT_TABLE)
        {
            items = getItemsFromLootTable(container.getItemProvider().getLocation(), serverWorld);
        }

        if (container.shouldRefillOneItemAtTime() && !items.isEmpty())
        {
            RefillableItem item = findLowestChanceItem(items);
            items.clear();
            items.add(item);
        }

        return items;
    }

    private RefillableItem findLowestChanceItem(List<RefillableItem> items)
    {
        RefillableItem lowestChanceItem = items.get(0);
        for (RefillableItem item : items)
        {
            if (item.getChance() < lowestChanceItem.getChance())
            {
                lowestChanceItem = item;
            }
        }
        return lowestChanceItem;
    }

    private List<RefillableItem> getItemsFromLootTable(String lootTableName, ServerWorld serverWorld)
    {
        return this.lootTableHelper.getItemsFromLootTable(lootTableName, serverWorld);
    }

    private List<RefillableItem> getRandomItems(List<RefillableItem> items)
    {
        final List<RefillableItem> itemsAchievedFromRandomizer = new ArrayList<>();
        for (RefillableItem refillableItem : items)
        {
            double number = Math.random();
            if (number <= refillableItem.getChance())
            {
                itemsAchievedFromRandomizer.add(refillableItem);
            }
        }
        return itemsAchievedFromRandomizer;
    }

    private void insertItems(Inventory inventory, List<RefillableItem> refillableItems, boolean stopAtFirstItem)
    {
        for (final RefillableItem item : refillableItems)
        {
            int i = 0;
            for(final Inventory slot : inventory.slots())
            {
                if(item.getSlot() == i)
                {
                    slot.offer(item.getItem().createStack());
                    if (stopAtFirstItem)
                        break;
                }
                i++;
            }
        }
    }

    private void insertItemsInRandomSlots(Inventory inventory, List<RefillableItem> refillableItems, boolean stopAtFirstItem)
    {
        final int numberOfSlots = inventory.capacity();
        int itemIndex = 0;
        for (; itemIndex < refillableItems.size(); itemIndex++)
        {
            final RefillableItem refillableItem = refillableItems.get(itemIndex);
            final int randomSlot = ThreadLocalRandom.current().nextInt(numberOfSlots);
            Slot slot = inventory.slot(randomSlot).orElse(null);
            if (slot == null)
            {
                itemIndex--;
                continue;
            }

            if (slot.totalQuantity() != 0)
            {
                itemIndex--;
                continue;
            }
            slot.offer(refillableItem.getItem().createStack());
            if (stopAtFirstItem)
                break;
        }
    }

    private void hideContainer(final RefillableContainer refillableContainer, final Inventory inventory, final ServerLocation containerLocation)
    {
        if (refillableContainer.shouldBeHiddenIfNoItems() && inventory.totalQuantity() == 0)
        {
            containerLocation.setBlockType(refillableContainer.getHidingBlock());
        }
    }
}
