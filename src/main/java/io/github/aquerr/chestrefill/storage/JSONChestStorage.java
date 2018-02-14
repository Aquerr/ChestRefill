package io.github.aquerr.chestrefill.storage;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.ChestLocation;
import io.github.aquerr.chestrefill.entities.RefillingChest;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.Extent;
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
public class JSONChestStorage
{
    private static Path chestsPath = Paths.get(ChestRefill.getChestRefill().getConfigDir() + "/chests.json");
    private static GsonConfigurationLoader configurationLoader = GsonConfigurationLoader.builder().setPath(chestsPath).build();


    public static boolean addChest(RefillingChest refillingChest)
    {
        //Serialize the chest and add it to the file.
        //Or
        //Save chest's location and its contents and save it as a JSON.

        try
        {
            if (!Files.exists(chestsPath)) Files.createFile(chestsPath);

            ConfigurationNode node = configurationLoader.load();

            //TODO: Try to deserialize and save Refilling chest class here instead of List and Time separately.

            //We are using block position and recreating location on retrieval.
            String blockPositionAndWorldUUID = refillingChest.getChestLocation().getBlockPosition().toString() + "|" + refillingChest.getChestLocation().getWorldUUID();

            //Set chest's items
            node.getNode("chestrefill", "chests", blockPositionAndWorldUUID, "items").setValue(new TypeToken<List<ItemStack>>(){}, refillingChest.getItems());

            //Set chest's regeneration time (in seconds)
            node.getNode("chestrefill", "chests", blockPositionAndWorldUUID, "time").setValue(120);

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

    public static boolean removeChest()
    {
        throw new NotImplementedException();
    }

    public static List<RefillingChest> getChests()
    {
        try
        {
            ConfigurationNode node = configurationLoader.load();

            Set<Object> objectList = node.getNode("chestrefill", "chests").getChildrenMap().keySet();
            List<RefillingChest> refillingChestsList = new ArrayList<>();

            for (Object object: objectList)
            {
                List<ItemStack> itemStacks = new ArrayList<>();
                //int time;

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
                itemStacks = node.getNode("chestrefill", "chests", chestPositionAndWorldUUIDString, "items").getList(objectToItemStackTransformer);

                //TODO: Get chest's refill time.

            }
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return new ArrayList<>();
    }

    private static Function<Object, ItemStack> objectToItemStackTransformer = input ->
    {
        if (input instanceof ItemStack)
        {
            return (ItemStack) input;
        }
        else
        {
            return null;
        }
    };
}
