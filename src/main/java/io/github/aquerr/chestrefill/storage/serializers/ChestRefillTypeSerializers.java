package io.github.aquerr.chestrefill.storage.serializers;

import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.util.List;

public class ChestRefillTypeSerializers
{
    public static TypeSerializerCollection TYPE_SERIALIZER_COLLECTION = TypeSerializerCollection.builder()
            .registerAll(TypeSerializerCollection.defaults())
            .register(TypeToken.get(RefillableItem.class), new RefillableItemTypeSerializer())
            .register(TypeToken.get(Kit.class), new KitTypeSerializer())
            .register(new TypeToken<List<RefillableItem>>(){}, new RefillableItemListTypeSerializer())
            .build();

    public static final TypeToken<Kit> KIT_TYPE_TOKEN = TypeToken.get(Kit.class);
    public static final TypeToken<RefillableItem> REFILLABLE_ITEM_TYPE_TOKEN = TypeToken.get(RefillableItem.class);
    public static final TypeToken<List<Kit>> KIT_LIST_TYPE_TOKEN = new TypeToken<List<Kit>>(){};
    public static final TypeToken<List<RefillableItem>> REFILLABLE_ITEM_LIST_TYPE_TOKEN = new TypeToken<List<RefillableItem>>(){};
}
