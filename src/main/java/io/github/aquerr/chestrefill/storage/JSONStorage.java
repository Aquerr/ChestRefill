package io.github.aquerr.chestrefill.storage;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.typesafe.config.parser.ConfigNode;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.PluginInfo;
import io.github.aquerr.chestrefill.entities.ContainerLocation;
import io.github.aquerr.chestrefill.entities.Kit;
import io.github.aquerr.chestrefill.entities.RefillableContainer;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import io.github.aquerr.chestrefill.storage.serializers.ChestRefillTypeSerializers;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class JSONStorage implements Storage
{
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
            ChestRefillGsonConfigurationLoader configurationLoader = new ChestRefillGsonConfigurationLoader(GsonConfigurationLoader.builder().setDefaultOptions(getDefaultOptions()).setPath(path));
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

            containersLoader = new ChestRefillGsonConfigurationLoader(GsonConfigurationLoader.builder().setDefaultOptions(getDefaultOptions()).setPath(containersPath));

            containersNode = containersLoader.load(getDefaultOptions());

            //Register watcher
            watchService = configDir.getFileSystem().newWatchService();
            key = configDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            Task.Builder changeTask = Sponge.getScheduler().createTaskBuilder();
            //Run a checkFileUpdate task every 2,5 second
            changeTask.async().intervalTicks(50L).execute(this::checkFileUpdate).submit(ChestRefill.getInstance());


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
        final ChestRefillGsonConfigurationLoader containersLoader = new ChestRefillGsonConfigurationLoader(GsonConfigurationLoader.builder().setDefaultOptions(getDefaultOptions()).setPath(oldKitsFile));

        try
        {
            final ConfigurationNode containersNode = containersLoader.load(getDefaultOptions());
            final List<Kit> kits = containersNode.getNode("kits").getList(ChestRefillTypeSerializers.KIT_TYPE_TOKEN, new ArrayList<>());
            for (final Kit kit : kits)
            {
                createKit(kit);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of(TextColors.RED, "Could not convert old kits.json file into new format.")));
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
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "name").setValue(refillableContainer.getName());

            //Set container's block type
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "container-block-type").setValue(TypeToken.of(BlockType.class), refillableContainer.getContainerBlockType());

            //Set container's kit
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "kit").setValue(refillableContainer.getKitName());

            if(refillableContainer.getKitName().equals(""))
            {
                //Set container's items
                containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "items").setValue(ChestRefillTypeSerializers.REFILLABLE_ITEM_LIST_TYPE_TOKEN, items);
            }
            else
            {
                containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "items").setValue(new ArrayList<>());
            }

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

            //Set open message
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "open-message").setValue(TextSerializers.FORMATTING_CODE.serialize(refillableContainer.getOpenMessage()));

            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "has-been-opened").setValue(refillableContainer.hasBeenOpened());

            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "first-open-message").setValue(TextSerializers.FORMATTING_CODE.serialize(refillableContainer.getFirstOpenMessage()));

            containersLoader.save(containersNode);

            return true;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of("Could not add/update container in the storage. Container = " + refillableContainer)));
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
            Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of("Could not remove container from the storage. Container location = " + containerLocation)));
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
            containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "time").setValue(time);

            containersLoader.save(containersNode);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
            Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of("Could not update container restore time. Container location = " + containerLocation + " | New time = " + time)));
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
            Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of("Could not change container name. Container location = " + containerLocation + " | Container name = " + containerName)));
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
                            return configNode.getValue(ChestRefillTypeSerializers.KIT_TYPE_TOKEN);
                        }
                        catch (IOException | ObjectMappingException e)
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
            Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of("Could not get kits from the storage.")));
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

            final ChestRefillGsonConfigurationLoader kitConfigLoader = new ChestRefillGsonConfigurationLoader(GsonConfigurationLoader.builder().setDefaultOptions(getDefaultOptions()).setPath(kitPath));
            final ConfigurationNode configurationNode = kitConfigLoader.createEmptyNode();
            configurationNode.setValue(ChestRefillTypeSerializers.KIT_TYPE_TOKEN, kit);

            kitConfigLoader.save(configurationNode);
            this.kitsLoaders.put(kit.getName().toLowerCase(), kitConfigLoader);
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of("Could not add kit to the storage. Kit = " + kit)));
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
            final Set<Object> blockPositionsAndWorldUUIDs = containersNode.getNode("chestrefill", "refillable-containers").getChildrenMap().keySet();
            for(final Object blockPositionAndWorldUUID : blockPositionsAndWorldUUIDs)
            {
                if(!(blockPositionAndWorldUUID instanceof String))
                    continue;
                final String blockPositionAndWorldUUIDString = String.valueOf(blockPositionAndWorldUUID);
                final Object kitValue = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUIDString, "kit").getValue();
                if(kitValue != null && String.valueOf(kitValue).equalsIgnoreCase(kitName))
                    containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUIDString, "kit").setValue("");
            }
            return true;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of("Could not remove kit from the storage. Kit name = " + kitName)));
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
            Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of("Could not assign kit to the container location. Container location = " + containerLocation + " | Kit name = " + kitName)));
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
                    Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Detected changes in containers.json file. Reloading!"));
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

            final Object containersName = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "name").getValue();
            String name = null;
            if (containersName != null) name = (String)containersName;

            final BlockType containerBlockType = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "container-block-type").getValue(TypeToken.of(BlockType.class));
            final String kitName = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "kit").getString("");
            List<RefillableItem> chestItems = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "items").getValue(ChestRefillTypeSerializers.REFILLABLE_ITEM_LIST_TYPE_TOKEN);
            final int time = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "time").getInt(120);
            final boolean isOneItemAtTime = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "one-item-at-time").getBoolean(false);
            final boolean shouldReplaceExistingItems = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "replace-existing-items").getBoolean(true);
            final boolean hiddenIfNoItems = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "hidden-if-no-items").getBoolean(false);
            final BlockType hidingBlockType = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "hiding-block").getValue(TypeToken.of(BlockType.class));
            final String requiredPermission = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "required-permission").getString("");
            final Text openMessage = TextSerializers.FORMATTING_CODE.deserialize(containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "open-message").getString(""));
            final boolean hasBeenOpened = containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "has-been-opened").getBoolean(false);
            final Text firstOpenMessage = TextSerializers.FORMATTING_CODE.deserialize(containersNode.getNode("chestrefill", "refillable-containers", blockPositionAndWorldUUID, "first-open-message").getString(""));

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
                    .build();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Sponge.getServer().getConsole().sendMessage(PluginInfo.ERROR_PREFIX.concat(Text.of("Could not get a container from the storage. Container location = " + containerLocation)));
        }

        return null;
    }

    private ConfigurationOptions getDefaultOptions()
    {
        final ConfigurationOptions configurationOptions = ConfigurationOptions.defaults();
        return configurationOptions.setAcceptedTypes(ImmutableSet.of(Map.class, List.class, Double.class, Float.class, Long.class, Integer.class, Boolean.class, String.class,
                Short.class, Byte.class, Number.class));
    }
}
