package io.github.aquerr.chestrefill.storage.serializers;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RefillableItemListTypeSerializer implements TypeSerializer<List<RefillableItem>>
{
    @Nullable
    @Override
    public List<RefillableItem> deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        final List<RefillableItem> refillableItems = new ArrayList<>();
        final List<? extends ConfigurationNode> nodes = value.getChildrenList();
        for (final ConfigurationNode configurationNode : nodes)
        {
            final RefillableItem refillableItem = configurationNode.getValue(ChestRefillTypeSerializers.REFILLABLE_ITEM_TYPE_TOKEN);
            refillableItems.add(refillableItem);
        }
        return refillableItems;
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable List<RefillableItem> obj, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        if (obj == null)
            return;

        for (final RefillableItem refillableItem : obj)
        {
            final ConfigurationNode configurationNode = value.getAppendedNode();
            configurationNode.setValue(ChestRefillTypeSerializers.REFILLABLE_ITEM_TYPE_TOKEN, refillableItem);
        }
    }
}
