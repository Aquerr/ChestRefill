package io.github.aquerr.chestrefill;

import io.github.aquerr.chestrefill.commands.HelpCommand;
import io.github.aquerr.chestrefill.listeners.LeftClickListener;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Aquerr on 2018-02-09.
 */

@Plugin(id = PluginInfo.Id, name = PluginInfo.Name, version = PluginInfo.Version, description = PluginInfo.Description, authors = PluginInfo.Authors, url = PluginInfo.Url)
public class ChestRefill
{
    public static Map<List<String>, CommandSpec> Subcommands = new HashMap<>();


    @Inject
    private Logger _logger;
    public Logger getLogger() {return _logger;}

    @Listener
    private void onGameInitialization(GameInitializationEvent event)
    {
        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Chest Refill is loading... :D"));
        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Initializing commands..."));

        initCommands();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Commands are ready!"));
        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Initializing listeners..."));

        initListeners();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PluginPrefix, TextColors.YELLOW, "Chest Refill is ready!"));
    }

    private void initCommands()
    {

        //Help Command
        Subcommands.put(Arrays.asList("help"), CommandSpec.builder()
        .description(Text.of("Displays all available commands"))
        .permission("chestrefill.help")
        .executor(new HelpCommand())
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
        Sponge.getEventManager().registerListeners(this, new LeftClickListener());
    }
}
