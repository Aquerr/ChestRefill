package io.github.aquerr.chestrefill.storage.serializers;

import io.github.aquerr.chestrefill.entities.RefillableItem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RefillableItemListTypeSerializer implements TypeSerializer<List<RefillableItem>>
{
    @Override
    public List<RefillableItem> deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        final List<RefillableItem> refillableItems = new ArrayList<>();
        final List<? extends ConfigurationNode> nodes = node.childrenList();
        for (final ConfigurationNode configurationNode : nodes)
        {
            final RefillableItem refillableItem = configurationNode.get(ChestRefillTypeSerializers.REFILLABLE_ITEM_TYPE_TOKEN);
            refillableItems.add(refillableItem);
        }
        return refillableItems;
    }

    @Override
    public void serialize(Type type, @Nullable List<RefillableItem> obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        for (final RefillableItem refillableItem : obj)
        {
            final ConfigurationNode configurationNode = node.appendListNode();
            configurationNode.set(ChestRefillTypeSerializers.REFILLABLE_ITEM_TYPE_TOKEN, refillableItem);
        }
    }
}
