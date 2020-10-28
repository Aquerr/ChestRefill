package io.github.aquerr.chestrefill.entities;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.chestrefill.ChestRefill;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.carrier.TileEntityCarrier;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class RefillableContainer
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

    private String kitName;

    private String requiredPermission;

    private Text openMessage;

    private Text firstOpenMessage;
    private boolean hasBeenOpened;

    public RefillableContainer(RefillableContainer.Builder builder)
    {
        this.name = builder.name;
        this.containerLocation = builder.containerLocation;
        this.restoreTimeInSeconds = builder.restoreTimeInSeconds;
        this.items = builder.items;
        this.oneItemAtTime = builder.oneItemAtTime;
        this.replaceExistingItems = builder.replaceExistingItems;
        this.hiddenIfNoItems = builder.hiddenIfNoItems;
        this.hidingBlock = builder.hidingBlock;
        this.containerBlockType = builder.containerBlockType;
        this.kitName = builder.kitName;
        this.requiredPermission = builder.requiredPermission;
        this.openMessage = builder.openMessage;
        this.firstOpenMessage = builder.firstOpenMessage;
        this.hasBeenOpened = builder.hasBeenOpened;
    }

    public static RefillableContainer fromInventory(final Inventory inventory, final BlockType blockType, final Vector3i blockPosition, final UUID worldUUID)
    {
        final List<RefillableItem> items = new ArrayList<>();
        int slot = 0;
        for (final Inventory slotInventory : inventory.slots())
        {
            if (slotInventory.peek().isPresent() && slotInventory.peek().get().getType() != ItemTypes.NONE)
            {
                items.add(new RefillableItem(slotInventory.peek().get().createSnapshot(), slot, 1f));
            }
            slot++;
        }

        return builder().location(new ContainerLocation(blockPosition, worldUUID)).blockType(blockType).items(items).build();
    }

    public static RefillableContainer fromTileEntity(TileEntity tileEntity, UUID worldUUID)
    {
        TileEntityCarrier carrier = (TileEntityCarrier) tileEntity;
        List<RefillableItem> items = new ArrayList<>();

        int slot = 0;
        for(final Inventory slotInventory : carrier.getInventory().slots())
        {
            if (slotInventory.peek().isPresent() && slotInventory.peek().get().getType() != ItemTypes.NONE)
            {
                items.add(new RefillableItem(slotInventory.peek().get().createSnapshot(), slot, 1f));
            }
            slot++;
        }

        return builder().location(new ContainerLocation(tileEntity.getLocation().getBlockPosition(), worldUUID)).blockType(tileEntity.getBlock().getType()).items(items).build();
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

    public void setKit(String kitName)
    {
        this.kitName = kitName;
    }

    public void setRequiredPermission(final String requiredPermission)
    {
        this.requiredPermission = requiredPermission;
    }

    public void setHidingBlock(final BlockType hidingBlock)
    {
        this.hidingBlock = hidingBlock;
    }

    public void setOpenMessage(final Text openMessage)
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

    public boolean isOneItemAtTime()
    {
        return this.oneItemAtTime;
    }

    public boolean shouldReplaceExistingItems()
    {
        return this.replaceExistingItems;
    }

    public boolean shouldBeHiddenIfNoItems()
    {
        return this.hiddenIfNoItems;
    }

    public BlockType getHidingBlock()
    {
        return this.hidingBlock;
    }

    public String getKitName()
    {
        return this.kitName;
    }

    public String getRequiredPermission()
    {
        return requiredPermission;
    }

    public Text getOpenMessage()
    {
        return this.openMessage;
    }

    public int getRestoreTimeInSeconds()
    {
        return restoreTimeInSeconds;
    }

    public Text getFirstOpenMessage()
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

    public void setFirstOpenMessage(Text firstOpenMessage)
    {
        this.firstOpenMessage = firstOpenMessage;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof RefillableContainer))
        {
            return false;
        }
        if (obj == this)
        {
            return true;
        }

        if(!this.containerLocation.equals(((RefillableContainer) obj).containerLocation))
            return false;

        if(!this.name.equals(((RefillableContainer) obj).name))
            return false;

        //Compare items
        if (!this.items.containsAll(((RefillableContainer) obj).getItems()))
        {
            return false;
        }

        //Compare restore time
        if (this.restoreTimeInSeconds != ((RefillableContainer)obj).getRestoreTime())
            return false;

        //Check if randomize is turned on
        if (this.oneItemAtTime != ((RefillableContainer)obj).oneItemAtTime)
            return false;

        //Check equality of replaceExistingItems property
        if (this.replaceExistingItems != ((RefillableContainer)obj).replaceExistingItems)
            return false;

        //Compare kit names
        if (!this.kitName.equals(((RefillableContainer) obj).kitName))
            return false;

        if(!this.requiredPermission.equals(((RefillableContainer)obj).requiredPermission))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.name != null ? this.name.hashCode() : 0);
        result = prime * result + (this.items != null ? this.items.hashCode() : 0);
        result = prime * result + (this.oneItemAtTime ? 0 : 1);
        result = prime * result + (this.hiddenIfNoItems ? 0 : 1);
        result = prime * result + this.restoreTimeInSeconds;
        result = prime * result + (this.containerLocation != null ? this.containerLocation.hashCode() : 0);
        result = prime * result + (this.containerBlockType != null ? this.containerBlockType.hashCode() : 0);
        result = prime * result + (this.hidingBlock != null ? this.hidingBlock.hashCode() : 0);
        result = prime * result + (this.kitName != null ? this.kitName.hashCode() : 0);
        result = prime * result + (this.requiredPermission != null ? this.requiredPermission.hashCode() : 0);
        return result;
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
                ", oneItemAtTime=" + oneItemAtTime +
                ", replaceExistingItems=" + replaceExistingItems +
                ", hiddenIfNoItems=" + hiddenIfNoItems +
                ", hidingBlock=" + hidingBlock +
                ", kitName='" + kitName + '\'' +
                ", requiredPermission='" + requiredPermission + '\'' +
                ", openMessage='" + openMessage + '\'' +
                '}';
    }

//            this("", containerLocation, containerBlockType, refillableItemList, 120, false, true, false, BlockTypes.DIRT, "", "", Text.of());

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

        private String kitName;

        private String requiredPermission;

        private Text openMessage;

        private Text firstOpenMessage;
        boolean hasBeenOpened;

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
            this.hidingBlock = BlockTypes.DIRT;
            this.kitName = "";
            this.requiredPermission = "";
            this.openMessage = Text.EMPTY;

            this.firstOpenMessage = Text.EMPTY;
            this.hasBeenOpened = false;
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

        public Builder kitName(final String kitName)
        {
            this.kitName = kitName;
            return this;
        }

        public Builder requiredPermission(final String requiredPermission)
        {
            this.requiredPermission = requiredPermission;
            return this;
        }

        public Builder openMessage(final Text openMessage)
        {
            this.openMessage = openMessage;
            return this;
        }

        public Builder hasBeenOpened(final boolean hasBeenOpened)
        {
            this.hasBeenOpened = hasBeenOpened;
            return this;
        }

        public Builder firstOpenMessage(final Text firstOpenMessage)
        {
            this.firstOpenMessage = firstOpenMessage;
            return this;
        }

        public RefillableContainer build()
        {
            if (this.name == null)
                this.name = "";
            if (this.kitName == null)
                this.kitName = "";
            if (this.requiredPermission == null)
                this.requiredPermission = "";

            return new RefillableContainer(this);
        }
    }
}
