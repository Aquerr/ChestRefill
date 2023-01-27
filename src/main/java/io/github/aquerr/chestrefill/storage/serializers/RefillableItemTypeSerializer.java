package io.github.aquerr.chestrefill.storage.serializers;

import com.google.common.collect.Lists;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class has been mostly copied from Nuclues.
 *
 * <p>See https://github.com/NucleusPowered/Nucleus/blob/sponge-api/7/src/main/java/io/github/nucleuspowered/nucleus/configurate/typeserialisers/NucleusItemStackSnapshotSerialiser.java</p>
 *
 * <p>This class, as such, is copyrighted (c) by NucleusPowered team and Nucleus contributors.</p>
 */
public class RefillableItemTypeSerializer implements TypeSerializer<RefillableItem>
{
    @Override
    public RefillableItem deserialize(Type type, ConfigurationNode node) throws SerializationException
    {
        final float chance = node.node("chance").getFloat(1f);
        final int slot = node.node("slot").getInt();

        final ConfigurationNode itemNode = node.node("item");

        boolean emptyEnchant = false;
        ConfigurationNode ench = itemNode.node("UnsafeData", "ench");
        if (!ench.virtual())
        {
            List<? extends ConfigurationNode> enchantments = ench.childrenList();
            if (enchantments.isEmpty())
            {
                // Remove empty enchantment list.
                itemNode.node("UnsafeData").removeChild("ench");
            }
            else
            {
                enchantments.forEach(x -> {
                    try
                    {
                        short id = Short.parseShort(x.node("id").getString());
                        short lvl = Short.parseShort(x.node("lvl").getString());

                        x.node("id").set(id);
                        x.node("lvl").set(lvl);
                    }
                    catch (NumberFormatException | SerializationException e)
                    {
                        try
                        {
                            x.set(null);
                        }
                        catch (SerializationException ex)
                        {
                            throw new RuntimeException(ex);
                        }
                    }
                });
            }
        }

        ConfigurationNode data = itemNode.node("Data");
        if (!data.virtual() && data.isList())
        {
            List<? extends ConfigurationNode> n = data.childrenList().stream()
                    .filter(x ->
                            !x.node("DataClass").getString("").endsWith("SpongeEnchantmentData")
                                    || (!x.node("ManipulatorData", "ItemEnchantments").virtual() && x.node("ManipulatorData", "ItemEnchantments").isList()))
                    .collect(Collectors.toList());
            emptyEnchant = n.size() != data.childrenList().size();

            if (emptyEnchant)
            {
                if (n.isEmpty())
                {
                    itemNode.removeChild("Data");
                }
                else
                {
                    itemNode.node("Data").set(n);
                }
            }
        }

        DataContainer dataContainer = null;
        try
        {
            String itemNodeAsString = HoconConfigurationLoader.builder().buildAndSaveString(itemNode);
            dataContainer = DataFormats.HOCON.get().read(itemNodeAsString);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Set<DataQuery> ldq = dataContainer.keys(true);

        for (DataQuery dataQuery : ldq)
        {
            String el = dataQuery.asString(".");
            if (el.contains("$Array$"))
            {
                try
                {
                    Tuple<DataQuery, Object> r = TypeHelper.getArray(dataQuery, dataContainer);
                    dataContainer.set(r.first(), r.second());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                dataContainer.remove(dataQuery);
            }
        }

        final Optional<ItemType> itemType = Sponge.game().registry(RegistryTypes.ITEM_TYPE)
                .findEntry(ResourceKey.resolve(String.valueOf(dataContainer.get(DataQuery.of("ItemType")).get())))
                .map(RegistryEntry::value);
        if (!itemType.isPresent())
        {
            throw new SerializationException("ItemType could not be recognized. Probably comes from a mod that has been removed from the server.");
        }

        ItemStack itemStack;
        try
        {
            itemStack = ItemStack.builder().fromContainer(dataContainer).build();
        }
        catch (Exception e)
        {
            throw new SerializationException("Could not create Item Stack from data container.");
        }

        // Validate the item.
        if (itemStack.isEmpty() || itemStack.type() == null)
        {
            // don't bother
            throw new SerializationException("Could not deserialize item. Item is empty.");
        }

//        if (emptyEnchant)
//        {
//            itemStack.offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList());
//            return new RefillableItem(itemStack.createSnapshot(), slot, chance);
//        }
//
//        if (itemStack.get(Keys.ITEM_ENCHANTMENTS).isPresent())
//        {
//            // Reset the data.
//            itemStack.offer(Keys.ITEM_ENCHANTMENTS, itemStack.get(Keys.ITEM_ENCHANTMENTS).get());
//            return new RefillableItem(itemStack.createSnapshot(), slot, chance);
//        }

        return new RefillableItem(itemStack.createSnapshot(), slot, chance);
    }

    @Override
    public void serialize(Type type, @Nullable RefillableItem obj, ConfigurationNode node) throws SerializationException
    {
        if (obj == null)
            return;

        final ItemStackSnapshot itemStack = obj.getItem();
        DataView view;
        try
        {
            view = itemStack.toContainer();
        }
        catch (NullPointerException e)
        {
            throw new SerializationException(e);
        }

        final Map<DataQuery, Object> dataQueryObjectMap = view.values(true);
        for (final Map.Entry<DataQuery, Object> entry : dataQueryObjectMap.entrySet())
        {
            if (entry.getValue().getClass().isArray())
            {
                if (entry.getValue().getClass().getComponentType().isPrimitive())
                {
                    DataQuery old = entry.getKey();
                    Tuple<DataQuery, List<?>> dqo = TypeHelper.getList(old, entry.getValue());
                    view.remove(old);
                    view.set(dqo.first(), dqo.second());
                }
                else
                {
                    view.set(entry.getKey(), Lists.newArrayList((Object[]) entry.getValue()));
                }
            }
        }

        node.node("chance").set(obj.getChance());
        node.node("slot").set(obj.getSlot());
        try
        {
            String itemStackAsString = DataFormats.HOCON.get().write(view);
            ConfigurationNode itemNode = HoconConfigurationLoader.builder().buildAndLoadString(itemStackAsString);
            node.node("item").set(itemNode);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
