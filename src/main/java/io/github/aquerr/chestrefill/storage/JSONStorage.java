package io.github.aquerr.chestrefill.storage;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataFormats;
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
    private Path containersPath;
    private GsonConfigurationLoader configurationLoader;
    ConfigurationNode node;

    WatchService _watchService;
    WatchKey _key;

    public JSONStorage(Path configDir)
    {
        try
        {
            containersPath = Paths.get(configDir + "/containers.json");

            if (!Files.exists(containersPath)) Files.createFile(containersPath);

            configurationLoader = GsonConfigurationLoader.builder().setPath(containersPath).build();
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

    public boolean addOrUpdateContainer(RefillableContainer refillableContainer)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = refillableContainer.getContainerLocation().getBlockPosition().toString() + "|" + refillableContainer.getContainerLocation().getWorldUUID();

            List<String> test = new ArrayList<>();


            //TEST
            for (ItemStack item : refillableContainer.getItems())
            {
                test.add(DataFormats.JSON.write(item.toContainer()));
                //test.add(item.toContainer());

                DataView view = DataFormats.JSON.read(test.get(test.size() - 1));
            }

            //TEST

            //Set container's items
            node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "items").setValue(new TypeToken<List<String>>(){}, test);

            //Set container's regeneration time (in seconds)
            node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "time").setValue(refillableContainer.getRestoreTime());

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

    public boolean removeRefillableContainers(ContainerLocation containerLocation)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID();

            node.getNode("chestrefill", "refillable-containers").removeChild(blockPositionAndWorldUUID);

            configurationLoader.save(node);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    public List<RefillableContainer> getRefillableContainers()
    {
        try
        {
            Set<Object> objectList = node.getNode("chestrefill", "refillable-containers").getChildrenMap().keySet();
            List<RefillableContainer> refillingContainersList = new ArrayList<>();

            for (Object object: objectList)
            {
                //Reset itemstacks List for every chest
                List<ItemStack> itemStacks = new ArrayList<>();
                int time;

                String chestPositionAndWorldUUIDString = (String)object;
                String splitter = "\\|";

                String[] chestPosAndWorldUUID = chestPositionAndWorldUUIDString.split(splitter);

                UUID worldUUID = UUID.fromString(chestPosAndWorldUUID[1]);

                String vectors[] = chestPosAndWorldUUID[0].replace("(", "").replace(")", "").replace(" ", "").split(",");

                int x = Integer.valueOf(vectors[0]);
                int y = Integer.valueOf(vectors[1]);
                int z = Integer.valueOf(vectors[2]);

                ContainerLocation containerLocation = new ContainerLocation(Vector3i.from(x, y, z), worldUUID);

                //Let's get chest's items
                List chestItems = node.getNode("chestrefill", "refillable-containers", chestPositionAndWorldUUIDString, "items").getList(new TypeToken<String>(){});

                for (Object chestItem : chestItems)
                {
                    String itemString = (String)chestItem;
                    DataView itemDataView = DataFormats.JSON.read(itemString);
                    ItemStack itemStack = Sponge.getDataManager().deserialize(ItemStack.class, itemDataView).get();
                    itemStacks.add(itemStack);
                }

                time = node.getNode("chestrefill", "refillable-containers", chestPositionAndWorldUUIDString, "time").getInt();

                RefillableContainer refillableContainer = new RefillableContainer(containerLocation, itemStacks, time);

                refillingContainersList.add(refillableContainer);
            }

            return refillingContainersList;
        }
        catch (ObjectMappingException | IOException e)
        {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    @Nullable
    public RefillableContainer getRefillableContainer(ContainerLocation containerLocation)
    {
        try
        {
            String blockPositionAndWorldUUID = containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID();

            Object chestObject = node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID).getValue();

            if (chestObject != null)
            {
                List<ItemStack> itemStacks = new ArrayList<>();
                int time;

                //Let's get chest's items
                List chestItems = node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "items").getList(new TypeToken<String>(){});

                for (Object chestItem : chestItems)
                {
                    String itemString = (String)chestItem;
                    DataView itemDataView = DataFormats.JSON.read(itemString);
                    ItemStack itemStack = Sponge.getDataManager().deserialize(ItemStack.class, itemDataView).get();
                    itemStacks.add(itemStack);
                }

                time = node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "time").getInt();

                RefillableContainer refillableContainer = new RefillableContainer(containerLocation, itemStacks, time);

                return refillableContainer;
            }
        }
        catch (ObjectMappingException | IOException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

    @Override
    public boolean updateContainerTime(ContainerLocation containerLocation, int time)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID();

            //Set chest's regeneration time (in seconds)
            node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "time").setValue(time);

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

                        if (changedFilePath.toString().contains("containers.json"))
                        {
                            node = configurationLoader.load();
                            Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Detected changes in containers.json file. Reloading!"));
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
