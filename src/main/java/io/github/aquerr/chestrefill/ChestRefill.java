package io.github.aquerr.chestrefill;

import io.github.aquerr.chestrefill.commands.CreateCommand;
import io.github.aquerr.chestrefill.commands.HelpCommand;
import io.github.aquerr.chestrefill.commands.RemoveCommand;
import io.github.aquerr.chestrefill.listeners.RightClickListener;
import io.github.aquerr.chestrefill.managers.ChestManager;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


/**
 * Created by Aquerr on 2018-02-09.
 */

@Plugin(id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description, authors = PluginInfo.Authors, url = PluginInfo.Url)
public class ChestRefill
{
    public static Map<List<String>, CommandSpec> Subcommands = new HashMap<>();

    public static Map<UUID, ChestMode> PlayersChestMode = new HashMap<>();

    private static ChestRefill chestRefill;
    public static ChestRefill getChestRefill() {return chestRefill;}

    @Inject
    private Logger _logger;
    public Logger getLogger() {return _logger;}

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path _configDir;
    public Path getConfigDir() {return _configDir;}

    @Listener
    public void onGameInitialization(GameInitializationEvent event)
    {
        try
        {
            Files.createDirectories(_configDir);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        chestRefill = this;

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Chest Refill is loading... :D"));
        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Initializing commands..."));

        initCommands();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Commands are ready!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Initializing listeners..."));

        initListeners();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Chest Refill is ready!"));
    }

    @Listener
    public void onGameLoad(GameLoadCompleteEvent event)
    {
        //Start refilling chests that were created on the server before
        ChestManager.restoreRefilling();
    }

    private void initCommands()
    {

        //Help Command
        Subcommands.put(Arrays.asList("help"), CommandSpec.builder()
            .description(Text.of("Displays all available commands"))
            .permission(PluginPermissions.HELP_COMMAND)
            .executor(new HelpCommand())
            .build());

        //Create Command
        Subcommands.put(Arrays.asList("c", "create"), CommandSpec.builder()
            .description(Text.of("Toggles chest creation mode"))
            .permission(PluginPermissions.CREATE_COMMAND)
            .executor(new CreateCommand())
            .build());

        //Remove Command
        Subcommands.put(Arrays.asList("r", "remove"), CommandSpec.builder()
            .description(Text.of("Toggles chest removal mode"))
            .permission(PluginPermissions.REMOVE_COMMAND)
            .executor(new RemoveCommand())
            .build());

        //Build all commands
        CommandSpec mainCommand = CommandSpec.builder()
                .description(Text.of("Displays all available commands"))
                .executor(new HelpCommand())
                .children(Subcommands)
                .build();

        //Register commands
        Sponge.getCommandManager().register(this, mainCommand, "chestrefill", "cr");
    }

    private void initListeners()
    {
        Sponge.getEventManager().registerListeners(this, new RightClickListener());
    }
}
