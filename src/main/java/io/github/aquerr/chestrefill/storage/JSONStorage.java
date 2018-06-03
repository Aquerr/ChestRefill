package io.github.aquerr.chestrefill.storage;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.RefillableItem;
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

            List<RefillableItem> items = new ArrayList<>(refillableContainer.getItems());

            //Set container's items
            node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "items").setValue(new TypeToken<List<RefillableItem>>(){}, items);

            //Set container's regeneration time (in seconds)
            node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "time").setValue(refillableContainer.getRestoreTime());

            //Set container's "one itemstack at time"
            node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "one-item-at-time").setValue(refillableContainer.isOneItemAtTime());

            //Set container's should-replace-existing-items property
            node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "replace-existing-items").setValue(refillableContainer.shouldReplaceExistingItems());

            configurationLoader.save(node);

            return true;
        }
        catch (IOException | ObjectMappingException exception)
        {
            exception.printStackTrace();
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
                List<RefillableItem> chestItems = node.getNode("chestrefill", "refillable-containers", chestPositionAndWorldUUIDString, "items").getList(new TypeToken<RefillableItem>(){});

                int time = node.getNode("chestrefill", "refillable-containers", chestPositionAndWorldUUIDString, "time").getInt();
                boolean isOneItemAtTime = node.getNode("chestrefill", "refillable-containers", chestPositionAndWorldUUIDString, "one-item-at-time").getBoolean();
                boolean shouldReplaceExistingItems = node.getNode("chestrefill", "refillable-containers", chestPositionAndWorldUUIDString, "replace-existing-items").getBoolean();

                RefillableContainer refillableContainer = new RefillableContainer(containerLocation, chestItems, time, isOneItemAtTime, shouldReplaceExistingItems);

                refillingContainersList.add(refillableContainer);
            }

            return refillingContainersList;
        }
        catch (ObjectMappingException e)
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
                //Let's get chest's items
                List<RefillableItem> chestItems = node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "items").getList(new TypeToken<RefillableItem>(){});

                int time = node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "time").getInt();
                boolean isOneItemAtTime = node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "one-item-at-time").getBoolean();
                boolean shouldReplaceExistingItems = node.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "replace-existing-items").getBoolean();

                RefillableContainer refillableContainer = new RefillableContainer(containerLocation, chestItems, time, isOneItemAtTime, shouldReplaceExistingItems);

                return refillableContainer;
            }
        }
        catch (ObjectMappingException exception)
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

//    private Function<Object, ItemStack> objectToItemStackTransformer = input ->
//    {
//        try
//        {
//            ItemStack test = (ItemStack)input;
//            return test;
//        }
//        catch (ClassCastException exception)
//        {
//            exception.printStackTrace();
//            return null;
//        }
//    };
}
