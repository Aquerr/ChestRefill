package io.github.aquerr.chestrefill.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.loot.LootSerializers;
import net.minecraft.loot.LootTable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LootTableLoader
{
    private final Gson gson;

    private final Path lootTablesDir;

    public LootTableLoader(Path configDir)
    {
        this.lootTablesDir = configDir.resolve("loot_tables");
        try
        {
            Files.createDirectories(this.lootTablesDir);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
        this.gson = LootSerializers.createLootTableSerializer().create();
    }

    public LootTable loadLootTable(String path)
    {
        Path lootTableFilePath = this.lootTablesDir.resolve(path);
        try
        {
            JsonElement lootTableJson = gson.fromJson(new FileReader(lootTableFilePath.toAbsolutePath().toString()), JsonElement.class);
            return ForgeHooks.loadLootTable(gson, new ResourceLocation("chestrefill", path), lootTableJson, true, null);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        return null;
    }

    public List<String> findAllLootTablesNames()
    {
        try
        {
            return Files.list(this.lootTablesDir)
                    .map(Path::getFileName)
                    .map(name -> "chestrefill:" + name)
                    .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
