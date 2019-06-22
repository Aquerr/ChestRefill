package io.github.aquerr.chestrefill.storage;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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
    private GsonConfigurationLoader containersLoader;
    private ConfigurationNode containersNode;

    private Path kitsPath;
    private GsonConfigurationLoader kitsLoader;
    private ConfigurationNode kitsNode;

    private WatchService _watchService;
    private WatchKey _key;

    public JSONStorage(Path configDir)
    {
        try
        {
            containersPath = Paths.get(configDir + "/containers.json");
            kitsPath = Paths.get(configDir + "/kits.json");

            if (!Files.exists(containersPath))
            {
                Files.createFile(containersPath);
            }

            if (!Files.exists(kitsPath))
            {
                Files.createFile(kitsPath);
            }


            containersLoader = GsonConfigurationLoader.builder().setPath(containersPath).build();
            containersNode = containersLoader.load();

            kitsLoader = GsonConfigurationLoader.builder().setPath(kitsPath).build();
            kitsNode = kitsLoader.load();

            //Register watcher
            _watchService = configDir.getFileSystem().newWatchService();
            _key = configDir.register(_watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            Task.Builder changeTask = Sponge.getScheduler().createTaskBuilder();
            //Run a checkFileUpdate task every 2,5 second
            changeTask.async().intervalTicks(50L).execute(checkFileUpdate()).submit(ChestRefill.getInstance());

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

            //Set container's name
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "name").setValue(refillableContainer.getName());

            //Set container's block type
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "container-block-type").setValue(TypeToken.of(BlockType.class), refillableContainer.getContainerBlockType());

            //Set container's kit
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "kit").setValue(refillableContainer.getKitName());

            //Set container's items
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "items").setValue(new TypeToken<List<RefillableItem>>(){}, items);

            //Set container's regeneration time (in seconds)
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "time").setValue(refillableContainer.getRestoreTime());

            //Set container's "one itemstack at time"
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "one-item-at-time").setValue(refillableContainer.isOneItemAtTime());

            //Set container's should-replace-existing-items property
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "replace-existing-items").setValue(refillableContainer.shouldReplaceExistingItems());

            //Set container's should-be-hidden-if-no-items
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "hidden-if-no-items").setValue(refillableContainer.shouldBeHiddenIfNoItems());

            //Set container's hidding block
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "hiding-block").setValue(TypeToken.of(BlockType.class), refillableContainer.getHidingBlock());

            //Set required permission
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "required-permission").setValue(refillableContainer.getRequiredPermission());

            containersLoader.save(containersNode);

            return true;
        }
        catch (IOException | ObjectMappingException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    public boolean removeRefillableContainer(ContainerLocation containerLocation)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID();

            containersNode.getNode("chestrefill", "refillable-containers").removeChild(blockPositionAndWorldUUID);

            containersLoader.save(containersNode);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public List<ContainerLocation> getContainerLocations()
    {
        Set<Object> objectList = containersNode.getNode("chestrefill", "refillable-containers").getChildrenMap().keySet();
        List<ContainerLocation> containerLocations = new ArrayList<>();

        for (Object object : objectList)
        {
            String chestPositionAndWorldUUIDString = (String) object;
            String splitter = "\\|";

            String[] chestPosAndWorldUUID = chestPositionAndWorldUUIDString.split(splitter);

            UUID worldUUID = UUID.fromString(chestPosAndWorldUUID[1]);

            String vectors[] = chestPosAndWorldUUID[0].replace("(", "").replace(")", "").replace(" ", "").split(",");

            int x = Integer.valueOf(vectors[0]);
            int y = Integer.valueOf(vectors[1]);
            int z = Integer.valueOf(vectors[2]);

            ContainerLocation containerLocation = new ContainerLocation(Vector3i.from(x, y, z), worldUUID);

            containerLocations.add(containerLocation);
        }

        return containerLocations;
    }

    @Override
    public List<RefillableContainer> getRefillableContainers()
    {
        List<RefillableContainer> refillingContainersList = new ArrayList<>();

        for (ContainerLocation containerLocation : getContainerLocations())
        {
            RefillableContainer refillableContainer = getRefillableContainerFromFile(containerLocation);

            refillingContainersList.add(refillableContainer);
        }

        return refillingContainersList;
    }

    @Override
    public boolean updateContainerTime(ContainerLocation containerLocation, int time)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID();

            //Set chest's regeneration time (in seconds)
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "time").setValue(time);

            containersLoader.save(containersNode);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean changeContainerName(ContainerLocation containerLocation, String containerName)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID();

            //Set chest's name
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "name").setValue(containerName);
            containersLoader.save(containersNode);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public List<Kit> getKits()
    {
        try
        {
            final List<Kit> kits = kitsNode.getNode("kits").getList(new TypeToken<Kit>(){});
            return kits;
        }
        catch(ObjectMappingException e)
        {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    @Override
    public boolean createKit(Kit kit)
    {
        try
        {
            List<Kit> kits = new ArrayList<>(kitsNode.getNode("kits").getList(new TypeToken<Kit>(){}));
            kits.add(kit);
            kitsNode.getNode("kits").setValue(new TypeToken<List<Kit>>(){}, kits);
            kitsLoader.save(kitsNode);
            return true;
        }
        catch(ObjectMappingException | IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean removeKit(String kitName)
    {
        try
        {
            List<Kit> kits = new ArrayList<>(kitsNode.getNode("kits").getList(new TypeToken<Kit>(){}));
            kits.removeIf(x->x.getName().equals(kitName));
            kitsNode.getNode("kits").setValue(new TypeToken<List<Kit>>(){}, kits);
            kitsLoader.save(kitsNode);
            return true;
        }
        catch(ObjectMappingException | IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean assignKit(ContainerLocation containerLocation, String kitName)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID();

            //Set chest's kit
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "kit").setValue(kitName);
            containersLoader.save(containersNode);
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
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
                            containersNode = containersLoader.load();
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

    private RefillableContainer getRefillableContainerFromFile(ContainerLocation containerLocation)
    {
        try
        {
             final String blockPositionAndWorldUUID = containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID().toString();

            final Object containersName = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "name").getValue();
            String name = null;
            if (containersName != null) name = (String)containersName;

            final BlockType containerBlockType = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "container-block-type").getValue(TypeToken.of(BlockType.class));
            //final List<RefillableItem> chestItems = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "items").getList(new TypeToken<RefillableItem>() {});
            final String kitName = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "kit").getString("");
            List<RefillableItem> chestItems = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "items").getValue(new TypeToken<List<RefillableItem>>() {});
            final int time = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "time").getInt();
            final boolean isOneItemAtTime = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "one-item-at-time").getBoolean();
            final boolean shouldReplaceExistingItems = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "replace-existing-items").getBoolean();
            final boolean hiddenIfNoItems = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "hidden-if-no-items").getBoolean();
            final BlockType hidingBlockType = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "hiding-block").getValue(TypeToken.of(BlockType.class));
            final String requiredPermission = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "required-permission").getString("");

            if(chestItems == null)
            {
                chestItems = new ArrayList<>();
            }

            //Check if chest is using a kit. If it does then override its items.
            if(!kitName.equals(""))
            {
                chestItems = getKitItems(kitName);
            }

            return new RefillableContainer(name, containerLocation, containerBlockType, chestItems, time, isOneItemAtTime, shouldReplaceExistingItems, hiddenIfNoItems, hidingBlockType, kitName, requiredPermission);
        }
        catch (ObjectMappingException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private List<RefillableItem> getKitItems(String kitName)
    {
        try
        {
            final List<Kit> kits = kitsNode.getNode("kits").getList(new TypeToken<Kit>(){});
            for(Kit kit : kits)
            {
                if(kit.getName().equals(kitName))
                {
                    return kit.getItems();
                }
            }
        }
        catch(ObjectMappingException e)
        {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
