package io.github.aquerr.chestrefill.storage;

import com.google.common.collect.ImmutableSet;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import io.github.aquerr.chestrefill.storage.serializers.ChestRefillTypeSerializers;
import io.leangen.geantyref.TypeToken;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.math.vector.Vector3i;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.LinearComponents.linear;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class JSONStorage implements Storage
{
    private static final String NODE_CHEST_REFILL = "chestrefill";
    private static final String NODE_REFILLABLE_CONTAINERS = "refillable-containers";

    private Path containersPath;
    private ChestRefillGsonConfigurationLoader containersLoader;
    private ConfigurationNode containersNode;

    private Map<String, ConfigurationLoader<? extends ConfigurationNode>> kitsLoaders = new HashMap<>();

    private Path kitsDirectoryPath;
    private Function<Path, ConfigurationLoader<? extends ConfigurationNode>> pathToConfigurationLoaderFunction = (path ->
    {
        final String fileName = path.getFileName().toString().toLowerCase();
        if (this.kitsLoaders.containsKey(fileName))
        {
            return this.kitsLoaders.get(fileName);
        }
        else
        {
            ChestRefillGsonConfigurationLoader configurationLoader = new ChestRefillGsonConfigurationLoader(GsonConfigurationLoader.builder()
                    .defaultOptions(getDefaultOptions())
                    .path(path));
            this.kitsLoaders.put(fileName, configurationLoader);
            return configurationLoader;
        }
    });

    private WatchService watchService;
    private WatchKey key;

    public JSONStorage(Path configDir)
    {
        try
        {
            containersPath = Paths.get(configDir + "/containers.json");

            if (!Files.exists(containersPath))
            {
                Files.createFile(containersPath);
            }

            this.kitsDirectoryPath = configDir.resolve("kits");

            if (!Files.exists(this.kitsDirectoryPath))
            {
                Files.createDirectory(this.kitsDirectoryPath);
            }

            containersLoader = new ChestRefillGsonConfigurationLoader(GsonConfigurationLoader.builder()
                    .defaultOptions(getDefaultOptions())
                    .path(containersPath));

            containersNode = containersLoader.load(getDefaultOptions());

            //Register watcher
            watchService = configDir.getFileSystem().newWatchService();
            key = configDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            //Run a checkFileUpdate task every 3 second
            Sponge.asyncScheduler().submit(Task.builder()
                            .interval(3, TimeUnit.SECONDS)
                            .execute(this::checkFileUpdate)
                            .plugin(ChestRefill.getInstance().getPluginContainer())
                            .build());

            //Backwards compatibility with 1.5.0
            //Convert old "kits.json" file into smaller kits files.
            final Path oldKitsFile = Paths.get(configDir + "/kits.json");
            if(Files.exists(oldKitsFile))
            {
                //We do not want to touch kits directory if it is not empty as we could break kits that already exists there.
                if (Files.list(kitsDirectoryPath).count() == 0)
                {
                    convertOldKitFileToNewFormat(oldKitsFile);
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Backwards compatibility with 1.5.0
     * This method will be removed in 1.7.0
     * @param oldKitsFile the path of the old kits file.
     */
    private void convertOldKitFileToNewFormat(Path oldKitsFile)
    {
        final ChestRefillGsonConfigurationLoader containersLoader = new ChestRefillGsonConfigurationLoader(GsonConfigurationLoader.builder()
                .defaultOptions(getDefaultOptions())
                .path(oldKitsFile));

        try
        {
            final ConfigurationNode containersNode = containersLoader.load(getDefaultOptions());
            final List<Kit> kits = containersNode.node("kits").getList(ChestRefillTypeSerializers.KIT_TYPE_TOKEN, new ArrayList<>());
            for (final Kit kit : kits)
            {
                createKit(kit);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Sponge.server().sendMessage(PluginInfo.ERROR_PREFIX.append(linear(RED, text("Could not convert old kits.json file into new format."))));
        }
    }

    @Override
    public boolean addOrUpdateContainer(RefillableContainer refillableContainer)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = refillableContainer.getContainerLocation().getBlockPosition().toString() + "|" + refillableContainer.getContainerLocation().getWorldUUID();

            List<RefillableItem> items = new ArrayList<>(refillableContainer.getItems());

            //Set container's name
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "name").set(refillableContainer.getName());

            //Set container's block type
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "container-block-type").set(TypeToken.get(BlockType.class), refillableContainer.getContainerBlockType());

            //Set container's kit
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "kit").set(refillableContainer.getKitName());

            if(refillableContainer.getKitName().equals(""))
            {
                //Set container's items
                containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "items").set(ChestRefillTypeSerializers.REFILLABLE_ITEM_LIST_TYPE_TOKEN, items);
            }
            else
            {
                containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "items").set(new ArrayList<>());
            }

            //Set container's regeneration time (in seconds)
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "time").set(refillableContainer.getRestoreTime());

            //Set container's "one itemstack at time"
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "one-item-at-time").set(refillableContainer.isOneItemAtTime());

            //Set container's should-replace-existing-items property
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "replace-existing-items").set(refillableContainer.shouldReplaceExistingItems());

            //Set container's should-be-hidden-if-no-items
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "hidden-if-no-items").set(refillableContainer.shouldBeHiddenIfNoItems());

            //Set container's hidding block
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "hiding-block").set(TypeToken.get(BlockType.class), refillableContainer.getHidingBlock());

            //Set required permission
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "required-permission").set(refillableContainer.getRequiredPermission());

            //Set open message
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "open-message").set(LegacyComponentSerializer.legacyAmpersand().serialize(refillableContainer.getOpenMessage()));

            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "has-been-opened").set(refillableContainer.hasBeenOpened());

            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "first-open-message").set(LegacyComponentSerializer.legacyAmpersand().serialize(refillableContainer.getFirstOpenMessage()));

            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "place-items-in-random-slots").set(refillableContainer.shouldPlaceItemsInRandomSlots());

            containersLoader.save(containersNode);

            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            Sponge.server().sendMessage(PluginInfo.ERROR_PREFIX.append(text("Could not add/update container in the storage. Container = " + refillableContainer)));
        }

        return false;
    }

    public boolean removeRefillableContainer(ContainerLocation containerLocation)
    {
        try
        {
            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID();

            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS).removeChild(blockPositionAndWorldUUID);

            containersLoader.save(containersNode);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
            Sponge.server().sendMessage(PluginInfo.ERROR_PREFIX.append(text("Could not remove container from the storage. Container location = " + containerLocation)));
        }

        return false;
    }

    @Override
    public List<ContainerLocation> getContainerLocations()
    {
        Set<Object> objectList = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS).childrenMap().keySet();
        List<ContainerLocation> containerLocations = new ArrayList<>();

        for (Object object : objectList)
        {
            String chestPositionAndWorldUUIDString = (String) object;
            String splitter = "\\|";

            String[] chestPosAndWorldUUID = chestPositionAndWorldUUIDString.split(splitter);
            UUID worldUUID = UUID.fromString(chestPosAndWorldUUID[1]);

            String[] vectors = chestPosAndWorldUUID[0].replace("(", "").replace(")", "").replace(" ", "").split(",");
            int x = Integer.parseInt(vectors[0]);
            int y = Integer.parseInt(vectors[1]);
            int z = Integer.parseInt(vectors[2]);

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
            if (refillableContainer != null)
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
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "time").set(time);

            containersLoader.save(containersNode);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
            Sponge.server().sendMessage(PluginInfo.ERROR_PREFIX.append(text("Could not update container restore time. Container location = " + containerLocation + " | New time = " + time)));
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
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "name").set(containerName);
            containersLoader.save(containersNode);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
            Sponge.server().sendMessage(PluginInfo.ERROR_PREFIX.append(text("Could not change container name. Container location = " + containerLocation + " | Container name = " + containerName)));
        }

        return false;
    }

    @Override
    public List<Kit> getKits()
    {
        try
        {
            return Files.list(this.kitsDirectoryPath)
                    .filter(Files::isRegularFile)
                    .map(pathToConfigurationLoaderFunction)
                    .map(configurationLoader ->
                    {
                        try
                        {
                            final ConfigurationNode configNode = configurationLoader.load();
                            return configNode.get(ChestRefillTypeSerializers.KIT_TYPE_TOKEN);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Sponge.server().sendMessage(PluginInfo.ERROR_PREFIX.append(text("Could not get kits from the storage.")));
        }

        return new ArrayList<>();
    }

    @Override
    public boolean createKit(Kit kit)
    {
        try
        {
            final Path kitPath = this.kitsDirectoryPath.resolve(kit.getName().toLowerCase() + ".json");
            if(Files.notExists(kitPath))
            {
                Files.createFile(kitPath);
            }

            final ChestRefillGsonConfigurationLoader kitConfigLoader = new ChestRefillGsonConfigurationLoader(GsonConfigurationLoader.builder()
                    .defaultOptions(getDefaultOptions())
                    .path(kitPath));
            final ConfigurationNode configurationNode = kitConfigLoader.createNode();
            configurationNode.set(ChestRefillTypeSerializers.KIT_TYPE_TOKEN, kit);

            kitConfigLoader.save(configurationNode);
            this.kitsLoaders.put(kit.getName().toLowerCase(), kitConfigLoader);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Sponge.server().sendMessage(PluginInfo.ERROR_PREFIX.append(text("Could not add kit to the storage. Kit = " + kit)));
        }

        return false;
    }

    @Override
    public boolean removeKit(String kitName)
    {
        try
        {
            Files.deleteIfExists(this.kitsDirectoryPath.resolve(kitName.toLowerCase() + ".json"));
            this.kitsLoaders.remove(kitName.toLowerCase());

            //Remove the kit from containers
            final Set<Object> blockPositionsAndWorldUUIDs = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS).childrenMap().keySet();
            for(final Object blockPositionAndWorldUUID : blockPositionsAndWorldUUIDs)
            {
                if(!(blockPositionAndWorldUUID instanceof String))
                    continue;
                final String blockPositionAndWorldUUIDString = String.valueOf(blockPositionAndWorldUUID);
                final Object kitValue = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUIDString, "kit").get(Objects.class);
                if(kitValue != null && String.valueOf(kitValue).equalsIgnoreCase(kitName))
                    containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUIDString, "kit").set("");
            }
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Sponge.server().sendMessage(PluginInfo.ERROR_PREFIX.append(text("Could not remove kit from the storage. Kit name = " + kitName)));
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
            containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "kit").set(kitName);
            containersLoader.save(containersNode);
            return true;
        }
        catch(IOException e)
        {
            e.printStackTrace();
            Sponge.server().sendMessage(PluginInfo.ERROR_PREFIX.append(text("Could not assign kit to the container location. Container location = " + containerLocation + " | Kit name = " + kitName)));
        }

        return false;
    }

    private void checkFileUpdate()
    {
        try
        {
            for (WatchEvent<?> event : key.pollEvents())
            {
                final Path changedFilePath = (Path) event.context();
                if (changedFilePath.getFileName().toString().equals("containers.json"))
                {
                    Sponge.server().sendMessage(linear(PluginInfo.PLUGIN_PREFIX, YELLOW, text("Detected changes in containers.json file. Reloading!")));
                    containersNode = containersLoader.load();
                    break;
                }
            }
            key.reset();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private RefillableContainer getRefillableContainerFromFile(ContainerLocation containerLocation)
    {
        try
        {
             final String blockPositionAndWorldUUID = containerLocation.getBlockPosition().toString() + "|" + containerLocation.getWorldUUID().toString();

            final Object containersName = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "name").get(Objects.class);
            String name = null;
            if (containersName != null) name = (String)containersName;

            final BlockType containerBlockType = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "container-block-type").get(TypeToken.get(BlockType.class));
            final String kitName = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "kit").getString("");
            List<RefillableItem> chestItems = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "items").get(ChestRefillTypeSerializers.REFILLABLE_ITEM_LIST_TYPE_TOKEN);
            final int time = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "time").getInt(120);
            final boolean isOneItemAtTime = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "one-item-at-time").getBoolean(false);
            final boolean shouldReplaceExistingItems = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "replace-existing-items").getBoolean(true);
            final boolean hiddenIfNoItems = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "hidden-if-no-items").getBoolean(false);
            final BlockType hidingBlockType = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "hiding-block").get(TypeToken.get(BlockType.class));
            final String requiredPermission = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "required-permission").getString("");
            final TextComponent openMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "open-message").getString(""));
            final boolean hasBeenOpened = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "has-been-opened").getBoolean(false);
            final TextComponent firstOpenMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "first-open-message").getString(""));
            final boolean shouldPlaceItemsInRandomSlots = containersNode.node(NODE_CHEST_REFILL, NODE_REFILLABLE_CONTAINERS, blockPositionAndWorldUUID, "place-items-in-random-slots").getBoolean(false);

            if(chestItems == null)
            {
                chestItems = new ArrayList<>();
            }

            return RefillableContainer.builder()
                    .name(name)
                    .location(containerLocation)
                    .blockType(containerBlockType)
                    .items(chestItems)
                    .restoreTimeInSeconds(time)
                    .oneItemAtTime(isOneItemAtTime)
                    .replaceExisitngItems(shouldReplaceExistingItems)
                    .hiddenIfNoItems(hiddenIfNoItems)
                    .hidingBlock(hidingBlockType)
                    .kitName(kitName)
                    .openMessage(openMessage)
                    .requiredPermission(requiredPermission)
                    .hasBeenOpened(hasBeenOpened)
                    .firstOpenMessage(firstOpenMessage)
                    .placeItemsInRandomSlots(shouldPlaceItemsInRandomSlots)
                    .build();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Sponge.server().sendMessage(PluginInfo.ERROR_PREFIX.append(text("Could not get a container from the storage. Container location = " + containerLocation)));
        }

        return null;
    }

    private ConfigurationOptions getDefaultOptions()
    {
        return ConfigurationOptions.defaults()
                .serializers(TypeSerializerCollection.builder()
                        .registerAll(ChestRefillTypeSerializers.TYPE_SERIALIZER_COLLECTION)
                        .build())
                .serializers(ChestRefillTypeSerializers.TYPE_SERIALIZER_COLLECTION)
                .nativeTypes(ImmutableSet.of(Map.class, List.class, Double.class, Float.class, Long.class, Integer.class, Boolean.class, String.class,
                        Short.class, Byte.class, Number.class));
    }
}
