package io.github.aquerr.chestrefill.config;

import com.google.gson.Gson;
import io.github.aquerr.chestrefill.ChestRefill;
import io.github.aquerr.chestrefill.entities.RefillingChest;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.block.tileentity.carrier.Chest;
import org.spongepowered.api.item.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aquerr on 2018-02-12.
 */
public class ChestConfig
{
    private static Path chestsPath = Paths.get(ChestRefill.getChestRefill().getConfigDir() + "chests.json");

    public static boolean addChest(Chest chest)
    {
        //Serialize the chest and add it to the file.
        //Or
        //Save chest's location and its contents and save it as a JSON.

        try
        {
            if (!Files.exists(chestsPath)) Files.createFile(chestsPath);

            GsonConfigurationLoader configurationLoader = GsonConfigurationLoader.builder().setPath(chestsPath).build();

            ConfigurationNode node = configurationLoader.load();

            Gson gson = new Gson();

            //Iritate over items in chest inventory
            List<ItemStack> items = new ArrayList<>();

            chest.getInventory().slots().forEach(x->
            {
                if (x.peek().isPresent())
                {
                    items.add(x.peek().get());
                }
            });

            RefillingChest refillingChest = new RefillingChest(chest.getLocation(), items);

            gson.toJson(refillingChest);

            node.getNode("chestrefill", "chests").setValue(gson);

            configurationLoader.save(node);

            return true;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }
}
