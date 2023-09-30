package io.github.aquerr.chestrefill.storage.serializers;

import io.github.aquerr.chestrefill.entities.ItemProvider;
import io.github.aquerr.chestrefill.entities.ItemProviderType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

public class ItemProviderTypeSerializer implements TypeSerializer<ItemProvider>
{
    @Override
    public ItemProvider deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        try
        {
            ItemProviderType itemProviderType = node.node("type").get(ItemProviderType.class, ItemProviderType.SELF);
            String location = node.node("location").getString();
            return new ItemProvider(itemProviderType, location);
        }
        catch (final Exception exception)
        {
            throw new SerializationException(ItemProvider.class, exception);
        }
    }

    @Override
    public void serialize(Type type, @Nullable ItemProvider obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        node.node("type").set(obj.getType());
        node.node("location").set(obj.getLocation());
    }
}
