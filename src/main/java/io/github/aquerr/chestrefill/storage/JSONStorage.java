package io.github.aquerr.chestrefill.storage;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.TileEntityLocation;
import io.github.aquerr.chestrefill.entities.RefillableTileEntity;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class JSONStorage implements Storage
{
    private Path chestsPath;
    private GsonConfigurationLoader configurationLoader;
    ConfigurationNode node;

    WatchService _watchService;
    WatchKey _key;

    public JSONStorage(Path configDir)
    {
        try
        {
            chestsPath = Paths.get(configDir + "/chests.json");

            if (!Files.exists(chestsPath)) Files.createFile(chestsPath);

            configurationLoader = GsonConfigurationLoader.builder().setPath(chestsPath).build();
            node = configurationLoader.load();

            //Register watcher
            _watchService = configDir.getFileSystem().newWatchService();
            _key =  configDir.register(_watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            Task.Builder changeTask = Sponge.getScheduler().createTaskBuilder();
            //Run a checkFileUpdate task every 2,5 second
            changeTask.async().intervalTicks(50L).execute(checkFileUpdate()).submit(ChestRefill.getChestRefill());

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public boolean addOrUpdateRefillableEntity(RefillableTileEntity refillableTileEntity)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = refillableTileEntity.getTileEntityLocation().getBlockPosition().toString() + "|" + refillableTileEntity.getTileEntityLocation().getWorldUUID();

            //Set chest's items
            node.getNode("chestrefill", "refillable-entities", blockPositionAndWorldUUID, "items").setValue(new TypeToken<List<ItemStack>>(){}, refillableTileEntity.getItems());

            //Set chest's regeneration time (in seconds)
            node.getNode("chestrefill", "refillable-entities", blockPositionAndWorldUUID, "time").setValue(refillableTileEntity.getRestoreTime());

            configurationLoader.save(node);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
        catch (ObjectMappingException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    public boolean removeRefillableEntity(TileEntityLocation tileEntityLocation)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = tileEntityLocation.getBlockPosition().toString() + "|" + tileEntityLocation.getWorldUUID();

            node.getNode("chestrefill", "refillable-entities").removeChild(blockPositionAndWorldUUID);

            configurationLoader.save(node);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    public List<RefillableTileEntity> getRefillableEntities()
    {
        try
        {
            Set<Object> objectList = node.getNode("chestrefill", "refillable-entities").getChildrenMap().keySet();
            List<RefillableTileEntity> refillingChestsList = new ArrayList<>();

            for (Object object: objectList)
            {
                //Reset itemstacks List for every chest
                List<ItemStack> itemStacks;
                int time;

                String chestPositionAndWorldUUIDString = (String)object;
                String splitter = "\\|";

                String[] chestPosAndWorldUUID = chestPositionAndWorldUUIDString.split(splitter);

                UUID worldUUID = UUID.fromString(chestPosAndWorldUUID[1]);

                String vectors[] = chestPosAndWorldUUID[0].replace("(", "").replace(")", "").replace(" ", "").split(",");

                int x = Integer.valueOf(vectors[0]);
                int y = Integer.valueOf(vectors[1]);
                int z = Integer.valueOf(vectors[2]);

                TileEntityLocation tileEntityLocation = new TileEntityLocation(Vector3i.from(x, y, z), worldUUID);

                //Let's get chest's items
                itemStacks = node.getNode("chestrefill", "refillable-entities", chestPositionAndWorldUUIDString, "items").getList(new TypeToken<ItemStack>(){});

                time = node.getNode("chestrefill", "refillable-entities", chestPositionAndWorldUUIDString, "time").getInt();

                RefillableTileEntity refillableTileEntity = new RefillableTileEntity(tileEntityLocation, itemStacks, time);

                refillingChestsList.add(refillableTileEntity);
            }

            return refillingChestsList;
        }
        catch (ObjectMappingException e)
        {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    @Nullable
    public RefillableTileEntity getRefillableEntity(TileEntityLocation tileEntityLocation)
    {
        try
        {
            String blockPositionAndWorldUUID = tileEntityLocation.getBlockPosition().toString() + "|" + tileEntityLocation.getWorldUUID();

            Object chestObject = node.getNode("chestrefill", "refillable-entities", blockPositionAndWorldUUID).getValue();

            if (chestObject != null)
            {
                List<ItemStack> itemStacks;
                int time;

                //Let's get chest's items
                itemStacks = node.getNode("chestrefill", "refillable-entities", blockPositionAndWorldUUID, "items").getList(new TypeToken<ItemStack>(){});

                time = node.getNode("chestrefill", "refillable-entities", blockPositionAndWorldUUID, "time").getInt();

                RefillableTileEntity refillableTileEntity = new RefillableTileEntity(tileEntityLocation, itemStacks, time);

                return refillableTileEntity;
            }
        }
        catch (ObjectMappingException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean updateEntityTime(TileEntityLocation tileEntityLocation, int time)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = tileEntityLocation.getBlockPosition().toString() + "|" + tileEntityLocation.getWorldUUID();

            //Set chest's regeneration time (in seconds)
            node.getNode("chestrefill", "refillable-entities", blockPositionAndWorldUUID, "time").setValue(time);

            configurationLoader.save(node);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    private Runnable checkFileUpdate()
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    for (WatchEvent<?> event : _key.pollEvents())
                    {
                        final Path changedFilePath = (Path) event.context();

                        if (changedFilePath.toString().contains("chests.json"))
                        {
                            node = configurationLoader.load();
                            Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Detected changes in chests.json file. Reloading!"));
                        }
                    }

                    _key.reset();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    private Function<Object, ItemStack> objectToItemStackTransformer = input ->
    {
        try
        {
            ItemStack test = (ItemStack)input;
            return test;
        }
        catch (ClassCastException exception)
        {
            exception.printStackTrace();
            return null;
        }
    };
}
