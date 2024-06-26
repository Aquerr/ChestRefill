package io.github.aquerr.chestrefill.util;

import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class LootTableHelper
{
    private static final Pattern LOOT_TABLE_NAME_PATTERN = Pattern.compile("^([^/:;'\\]\\[,.#$%&*()!?@^]*):(.*)$");

    private final LootTableLoader lootTableLoader;

    public LootTableHelper(ChestRefill plugin)
    {
        this.lootTableLoader = new LootTableLoader(plugin.getConfigDir());
    }

    public List<RefillableItem> getItemsFromLootTable(String lootTableName, ServerWorld serverWorld)
    {
        ResourceLocation resourceLocation = new ResourceLocation(lootTableName);
        if (!LOOT_TABLE_NAME_PATTERN.matcher(lootTableName).matches() || !(serverWorld instanceof ServerLevel))
            return Collections.emptyList();

        LootTable lootTable = ServerLifecycleHooks.getCurrentServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, resourceLocation));
        if (lootTable != LootTable.EMPTY)
        {
            return getItemsFromLootTable(lootTable, (ServerLevel)serverWorld);
        }
        else
        {
            return getItemsFromChestRefillLootTable(resourceLocation, (ServerLevel)serverWorld);
        }
    }

    private List<RefillableItem> getItemsFromLootTable(LootTable lootTable, ServerLevel serverLevel)
    {
        List<ItemStack> itemStacks = lootTable.getRandomItems(new LootParams.Builder(serverLevel).create(new LootContextParamSet.Builder().build()));

        List<RefillableItem> refillableItems = new ArrayList<>();
        int slot = 0;
        for (final ItemStack itemStack : itemStacks)
        {
            org.spongepowered.api.item.inventory.ItemStack spongeItemStack = org.spongepowered.api.item.inventory.ItemStack.class.cast(itemStack);
            refillableItems.add(new RefillableItem(spongeItemStack.createSnapshot(), slot++, 1f));
        }
        return refillableItems;
    }

    private List<RefillableItem> getItemsFromChestRefillLootTable(ResourceLocation resourceLocation, ServerLevel serverLevel)
    {
        String path = resourceLocation.getPath();
        LootTable lootTable = lootTableLoader.loadLootTable(path);
        if (lootTable == null)
            return Collections.emptyList();

        return getItemsFromLootTable(lootTable, serverLevel);
    }

    public Collection<String> getAllChestRefillLootTablesNames()
    {
        return this.lootTableLoader.findAllLootTablesNames();
    }
}
