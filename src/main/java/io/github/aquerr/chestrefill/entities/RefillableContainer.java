package io.github.aquerr.chestrefill.entities;

import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.carrier.CarrierBlockEntity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class RefillableContainer
{
    private String name;

    private ContainerLocation containerLocation;
    private List<RefillableItem> items;
    private final BlockType containerBlockType;

    private int restoreTimeInSeconds;
    private boolean refillOneItemAtTime;
    private boolean replaceExistingItems;

    private boolean hiddenIfNoItems;
    private BlockType hidingBlock;

    private ItemProvider itemProvider;
    private String requiredPermission;

    private String openMessage;

    private String firstOpenMessage;
    private boolean hasBeenOpened;

    private boolean placeItemsInRandomSlots;
    private boolean indestructible;

    public RefillableContainer(RefillableContainer.Builder builder)
    {
        this.name = builder.name;
        this.containerLocation = builder.containerLocation;
        this.restoreTimeInSeconds = builder.restoreTimeInSeconds;
        this.items = builder.items;
        this.refillOneItemAtTime = builder.oneItemAtTime;
        this.replaceExistingItems = builder.replaceExistingItems;
        this.hiddenIfNoItems = builder.hiddenIfNoItems;
        this.hidingBlock = builder.hidingBlock;
        this.containerBlockType = builder.containerBlockType;
        this.itemProvider = builder.itemProvider;
        this.requiredPermission = builder.requiredPermission;
        this.openMessage = builder.openMessage;
        this.firstOpenMessage = builder.firstOpenMessage;
        this.hasBeenOpened = builder.hasBeenOpened;
        this.placeItemsInRandomSlots = builder.placeItemsInRandomSlots;
        this.indestructible = builder.indestructible;
    }

    public static RefillableContainer fromInventory(final Inventory inventory, final BlockType blockType, final Vector3i blockPosition, final UUID worldUUID)
    {
        final List<RefillableItem> items = new ArrayList<>();
        int slot = 0;
        for (final Inventory slotInventory : inventory.slots())
        {
            if (slotInventory.peek() != ItemStack.empty())
            {
                items.add(new RefillableItem(slotInventory.peek().createSnapshot(), slot, 1f));
            }
            slot++;
        }

        return builder().location(new ContainerLocation(blockPosition, worldUUID)).blockType(blockType).items(items).build();
    }

    public static RefillableContainer fromBlockEntity(CarrierBlockEntity carrierBlockEntity, UUID worldUUID)
    {
        List<RefillableItem> items = new ArrayList<>();

        int slot = 0;
        for(final Inventory slotInventory : carrierBlockEntity.inventory().slots())
        {
            if (slotInventory.peek() != ItemStack.empty())
            {
                items.add(new RefillableItem(slotInventory.peek().createSnapshot(), slot, 1f));
            }
            slot++;
        }

        return builder()
                .location(new ContainerLocation(carrierBlockEntity.location().blockPosition(), worldUUID))
                .blockType(carrierBlockEntity.block().type())
                .items(items)
                .build();
    }

    public static RefillableContainer.Builder builder()
    {
        return new Builder();
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setRestoreTime(int seconds)
    {
        this.restoreTimeInSeconds = seconds;
    }

    public void setContainerLocation(ContainerLocation containerLocation)
    {
        this.containerLocation = containerLocation;
    }

    public void setItems(List<RefillableItem> items)
    {
        this.items = items;
    }

    public void setRequiredPermission(final String requiredPermission)
    {
        this.requiredPermission = requiredPermission;
    }

    public void setHidingBlock(final BlockType hidingBlock)
    {
        this.hidingBlock = hidingBlock;
    }

    public void setOpenMessage(final String openMessage)
    {
        this.openMessage = openMessage;
    }

    public String getName()
    {
        return this.name;
    }

    public ContainerLocation getContainerLocation()
    {
        return this.containerLocation;
    }

    public List<RefillableItem> getItems()
    {
        return this.items;
    }

    public BlockType getContainerBlockType()
    {
        return this.containerBlockType;
    }

    public int getRestoreTime() { return this.restoreTimeInSeconds; }

    public boolean shouldRefillOneItemAtTime()
    {
        return this.refillOneItemAtTime;
    }

    public boolean shouldReplaceExistingItems()
    {
        return this.replaceExistingItems;
    }

    public boolean shouldBeHiddenIfNoItems()
    {
        return this.hiddenIfNoItems;
    }

    public void setHiddenIfNoItems(boolean hiddenIfNoItems)
    {
        this.hiddenIfNoItems = hiddenIfNoItems;
    }

    public BlockType getHidingBlock()
    {
        return this.hidingBlock;
    }

    public String getRequiredPermission()
    {
        return requiredPermission;
    }

    public String getOpenMessage()
    {
        return this.openMessage;
    }

    public String getFirstOpenMessage()
    {
        return this.firstOpenMessage;
    }

    public boolean hasBeenOpened()
    {
        return this.hasBeenOpened;
    }

    public void setHasBeenOpened(boolean hasBeenOpened)
    {
        this.hasBeenOpened = hasBeenOpened;
    }

    public void setFirstOpenMessage(String firstOpenMessage)
    {
        this.firstOpenMessage = firstOpenMessage;
    }

    public boolean shouldPlaceItemsInRandomSlots()
    {
        return this.placeItemsInRandomSlots;
    }

    public void setShouldPlaceItemsInRandomSlots(boolean value)
    {
        this.placeItemsInRandomSlots = value;
    }

    public ItemProvider getItemProvider()
    {
        return itemProvider;
    }

    public void setItemProvider(ItemProvider itemProvider)
    {
        this.itemProvider = itemProvider;
    }

    public boolean isIndestructible() {
        return indestructible;
    }

    public void setIndestructible(boolean indestructible) {
        this.indestructible = indestructible;
    }

    public boolean hasPermissionToOpen(ServerPlayer player)
    {
        if (requiredPermission == null || requiredPermission.equals(""))
            return true;

        return player.hasPermission(getRequiredPermission());
    }

    public RefillableContainer copy()
    {
        return RefillableContainer.builder()
                .name(name)
                .location(containerLocation)
                .blockType(containerBlockType)
                .items(items)
                .restoreTimeInSeconds(restoreTimeInSeconds)
                .oneItemAtTime(refillOneItemAtTime)
                .replaceExisitngItems(replaceExistingItems)
                .hiddenIfNoItems(hiddenIfNoItems)
                .hidingBlock(hidingBlock)
                .itemProvider(itemProvider)
                .openMessage(openMessage)
                .requiredPermission(requiredPermission)
                .hasBeenOpened(hasBeenOpened)
                .firstOpenMessage(firstOpenMessage)
                .placeItemsInRandomSlots(placeItemsInRandomSlots)
                .indestructible(indestructible)
                .build();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefillableContainer that = (RefillableContainer) o;
        return restoreTimeInSeconds == that.restoreTimeInSeconds && refillOneItemAtTime == that.refillOneItemAtTime && replaceExistingItems == that.replaceExistingItems && hiddenIfNoItems == that.hiddenIfNoItems && hasBeenOpened == that.hasBeenOpened && placeItemsInRandomSlots == that.placeItemsInRandomSlots && Objects.equals(name, that.name) && Objects.equals(containerLocation, that.containerLocation) && Objects.equals(items, that.items) && Objects.equals(containerBlockType, that.containerBlockType) && Objects.equals(hidingBlock, that.hidingBlock) && Objects.equals(itemProvider, that.itemProvider) && Objects.equals(requiredPermission, that.requiredPermission) && Objects.equals(openMessage, that.openMessage) && Objects.equals(firstOpenMessage, that.firstOpenMessage);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, containerLocation, items, containerBlockType, restoreTimeInSeconds, refillOneItemAtTime, replaceExistingItems, hiddenIfNoItems, hidingBlock, itemProvider, requiredPermission, openMessage, firstOpenMessage, hasBeenOpened, placeItemsInRandomSlots);
    }

    @Override
    public String toString()
    {
        return "RefillableContainer{" +
                "name='" + name + '\'' +
                ", containerLocation=" + containerLocation +
                ", items=" + items +
                ", containerBlockType=" + containerBlockType +
                ", restoreTimeInSeconds=" + restoreTimeInSeconds +
                ", oneItemAtTime=" + refillOneItemAtTime +
                ", replaceExistingItems=" + replaceExistingItems +
                ", hiddenIfNoItems=" + hiddenIfNoItems +
                ", hidingBlock=" + hidingBlock +
                ", itemProvider=" + itemProvider +
                ", requiredPermission='" + requiredPermission + '\'' +
                ", openMessage=" + openMessage +
                ", firstOpenMessage=" + firstOpenMessage +
                ", hasBeenOpened=" + hasBeenOpened +
                ", placeItemsInRandomSlots=" + placeItemsInRandomSlots +
                '}';
    }

    public static class Builder
    {
        private String name;

        private ContainerLocation containerLocation;
        private List<RefillableItem> items;
        private BlockType containerBlockType;

        private int restoreTimeInSeconds;
        private boolean oneItemAtTime;
        private boolean replaceExistingItems;

        private boolean hiddenIfNoItems;
        private BlockType hidingBlock;

        private ItemProvider itemProvider;

        private String requiredPermission;

        private String openMessage;

        private String firstOpenMessage;
        private boolean hasBeenOpened;

        private boolean placeItemsInRandomSlots;
        private boolean indestructible;

        private Builder()
        {
            this.name = "";
            this.containerLocation = null;
            this.items = new ArrayList<>();
            this.containerBlockType = null;
            this.restoreTimeInSeconds = 120;
            this.oneItemAtTime = false;
            this.replaceExistingItems = true;
            this.hiddenIfNoItems = false;
            this.hidingBlock = BlockTypes.DIRT.get();
            this.itemProvider = new ItemProvider(ItemProviderType.SELF, "");
            this.requiredPermission = "";
            this.openMessage = null;

            this.firstOpenMessage = null;
            this.hasBeenOpened = false;

            this.placeItemsInRandomSlots = false;
            this.indestructible = false;
        }

        public Builder name(final String name)
        {
            this.name = name;
            return this;
        }

        public Builder location(final ContainerLocation containerLocation)
        {
            this.containerLocation = containerLocation;
            return this;
        }

        public Builder items(final List<RefillableItem> items)
        {
            this.items = items;
            return this;
        }

        public Builder blockType(final BlockType blockType)
        {
            this.containerBlockType = blockType;
            return this;
        }

        public Builder restoreTimeInSeconds(final int restoreTimeInSeconds)
        {
            this.restoreTimeInSeconds = restoreTimeInSeconds;
            return this;
        }

        public Builder oneItemAtTime(final boolean oneItemAtTime)
        {
            this.oneItemAtTime = oneItemAtTime;
            return this;
        }

        public Builder replaceExisitngItems(final boolean replaceExistingItems)
        {
            this.replaceExistingItems = replaceExistingItems;
            return this;
        }

        public Builder hiddenIfNoItems(final boolean hiddenIfNoItems)
        {
            this.hiddenIfNoItems = hiddenIfNoItems;
            return this;
        }

        public Builder hidingBlock(final BlockType hidingBlock)
        {
            this.hidingBlock = hidingBlock;
            return this;
        }

        public Builder requiredPermission(final String requiredPermission)
        {
            this.requiredPermission = requiredPermission;
            return this;
        }

        public Builder openMessage(final String openMessage)
        {
            this.openMessage = openMessage;
            return this;
        }

        public Builder hasBeenOpened(final boolean hasBeenOpened)
        {
            this.hasBeenOpened = hasBeenOpened;
            return this;
        }

        public Builder firstOpenMessage(final String firstOpenMessage)
        {
            this.firstOpenMessage = firstOpenMessage;
            return this;
        }

        public Builder placeItemsInRandomSlots(final boolean placeItemsInRandomSlots)
        {
            this.placeItemsInRandomSlots = placeItemsInRandomSlots;
            return this;
        }

        public Builder itemProvider(ItemProvider itemProvider)
        {
            this.itemProvider = itemProvider;
            return this;
        }

        public Builder indestructible(boolean indestructible) {
            this.indestructible = indestructible;
            return this;
        }

        public RefillableContainer build()
        {
            if (this.name == null)
                this.name = "";
            if (this.requiredPermission == null)
                this.requiredPermission = "";

            return new RefillableContainer(this);
        }
    }
}
