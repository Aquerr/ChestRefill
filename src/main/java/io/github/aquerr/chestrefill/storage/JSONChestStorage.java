package io.github.aquerr.chestrefill.storage;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ChestLocation;
import io.github.aquerr.chestrefill.entities.RefillingChest;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.item.inventory.ItemStack;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class JSONChestStorage implements Storage
{
    private static Path chestsPath = Paths.get(ChestRefill.getChestRefill().getConfigDir() + "/chests.json");
    private static GsonConfigurationLoader configurationLoader = GsonConfigurationLoader.builder().setPath(chestsPath).build();

    public boolean addChest(RefillingChest refillingChest)
    {
        try
        {
            if (!Files.exists(chestsPath)) Files.createFile(chestsPath);

            ConfigurationNode node = configurationLoader.load();

            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = refillingChest.getChestLocation().getBlockPosition().toString() + "|" + refillingChest.getChestLocation().getWorldUUID();

            //Set chest's items
            node.getNode("chestrefill", "chests", blockPositionAndWorldUUID, "items").setValue(new TypeToken<List<ItemStack>>(){}, refillingChest.getItems());

            //Set chest's regeneration time (in seconds)
            node.getNode("chestrefill", "chests", blockPositionAndWorldUUID, "time").setValue(refillingChest.getRestoreTime());

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

    public boolean removeChest(RefillingChest refillingChest)
    {
        try
        {
            if (!Files.exists(chestsPath)) Files.createFile(chestsPath); //TODO: Move this line of code so that it executes globally.

            ConfigurationNode node = configurationLoader.load();

            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = refillingChest.getChestLocation().getBlockPosition().toString() + "|" + refillingChest.getChestLocation().getWorldUUID();

            node.getNode("chestrefill", "chests").removeChild(blockPositionAndWorldUUID);

            configurationLoader.save(node);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    public List<RefillingChest> getChests()
    {
        try
        {
            if (!Files.exists(chestsPath)) Files.createFile(chestsPath);

            ConfigurationNode node = configurationLoader.load();

            Set<Object> objectList = node.getNode("chestrefill", "chests").getChildrenMap().keySet();
            List<RefillingChest> refillingChestsList = new ArrayList<>();

            for (Object object: objectList)
            {
                //Reset itemstacks List for every chest
                List<ItemStack> itemStacks = new ArrayList<>();
                int time = 0;

                String chestPositionAndWorldUUIDString = (String)object;
                String splitter = "\\|";

                String[] chestPosAndWorldUUID = chestPositionAndWorldUUIDString.split(splitter);

                UUID worldUUID = UUID.fromString(chestPosAndWorldUUID[1]);

                String vectors[] = chestPosAndWorldUUID[0].replace("(", "").replace(")", "").replace(" ", "").split(",");

                int x = Integer.valueOf(vectors[0]);
                int y = Integer.valueOf(vectors[1]);
                int z = Integer.valueOf(vectors[2]);

                ChestLocation chestLocation = new ChestLocation(Vector3i.from(x, y, z), worldUUID);

                //Let's get chest's items
                itemStacks = node.getNode("chestrefill", "chests", chestPositionAndWorldUUIDString, "items").getList(new TypeToken<ItemStack>(){});

                time = node.getNode("chestrefill", "chests", chestPositionAndWorldUUIDString, "time").getInt();

                RefillingChest refillingChest = new RefillingChest(chestLocation, itemStacks, time);

                refillingChestsList.add(refillingChest);
            }

            return refillingChestsList;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        } catch (ObjectMappingException e)
        {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private static Function<Object, ItemStack> objectToItemStackTransformer = input ->
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
