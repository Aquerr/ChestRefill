package io.github.aquerr.chestrefill.storage;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.RefillingChest;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

            //Set chest's items
            node.getNode("chestrefill", "chests", refillingChest.getChestLocation(), "items").setValue(new TypeToken<List<ItemStack>>(){}, refillingChest.getItems());

            //Set chest's regeneration time (in seconds)
            node.getNode("chestrefill", "chests", refillingChest.getChestLocation(), "time").setValue(120);

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
            List<RefillingChest> refillingChestList = new ArrayList<>();

            for (Object object: objectList)
            {
                List<ItemStack> itemStacks = new ArrayList<>();
                //int time;

                //This cast is safe because all chests' keys are locations.
                Location chestLocation = (Location) object;

                //Let's get chest's items
                itemStacks = node.getNode("chestrefill", "chests", chestLocation, "items").getList(objectToItemStackTransformer);

//                for (Object itemStackObject : itemStacksObjects)
//                {
//                    //This cast is safe because all items in chest are itemstacks.
//                    itemStacks.add((ItemStack)itemStackObject);
//                }

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
