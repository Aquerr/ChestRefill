package io.github.aquerr.chestrefill;

import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.commands.*;
import io.github.aquerr.chestrefill.commands.arguments.ContainerNameArgument;
import io.github.aquerr.chestrefill.commands.arguments.KitNameArgument;
import io.github.aquerr.chestrefill.entities.*;
import io.github.aquerr.chestrefill.listeners.*;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import io.github.aquerr.chestrefill.scheduling.ContainerScheduler;
import io.github.aquerr.chestrefill.storage.serializers.KitTypeSerializer;
import io.github.aquerr.chestrefill.storage.serializers.RefillableItemListTypeSerializer;
import io.github.aquerr.chestrefill.storage.serializers.RefillableItemTypeSerializer;
import io.github.aquerr.chestrefill.version.VersionChecker;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameLoadCompleteEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;


/**
 * Created by Aquerr on 2018-02-09.
 */

@Plugin(id = PluginInfo.ID, name = PluginInfo.NAME, version = PluginInfo.VERSION, description = PluginInfo.DESCRIPTION, authors = PluginInfo.AUTHORS, url = PluginInfo.URL)
public class ChestRefill
{
    public static final Map<List<String>, CommandSpec> SUBCOMMANDS = new HashMap<>();
    public static final Map<UUID, SelectionMode> PLAYER_CHEST_SELECTION_MODE = new HashMap<>();
    public static final Map<UUID, String> PLAYER_CHEST_NAME = new HashMap<>();
    public static final Map<UUID, Integer> CONTAINER_TIME_CHANGE_PLAYER = new HashMap<>();
    public static final Map<UUID, RefillableContainer> PLAYER_COPY_REFILLABLE_CONTAINER = new HashMap<>();
    public static final Map<UUID, String> PLAYER_KIT_NAME = new HashMap<>();
    public static final Map<UUID, String> PLAYER_KIT_ASSIGN = new HashMap<>();

    public static final Map<UUID, SelectionPoints> PLAYER_SELECTION_POINTS = new HashMap<>();

    private ContainerScheduler containerScheduler;
    private ContainerManager containerManager;

    private static ChestRefill chestRefill;

    @Inject
    private Logger _logger;

    public static ChestRefill getInstance()
    {
        if(chestRefill != null)
            return chestRefill;
        return new ChestRefill();
    }

    public Logger getLogger() {return _logger;}

    @Inject
    @ConfigDir(sharedRoot = false)
    private Path _configDir;
    public Path getConfigDir() {return _configDir;}

    @Listener
    public void onGameInitialization(GameInitializationEvent event)
    {
        chestRefill = this;
        registerTypeSerializers();
        this.containerManager = new ContainerManager(this, getConfigDir());
        this.containerScheduler = new ContainerScheduler(this);

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Chest Refill is loading... :D"));
        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Initializing commands..."));

        initCommands();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Initializing listeners..."));

        initListeners();

        Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.YELLOW, "Chest Refill is ready!"));

        CompletableFuture.runAsync(() ->{
            if (VersionChecker.isLatest(PluginInfo.VERSION))
            {
                Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, "You are using the latest version!"));
            }
            else
            {
                Sponge.getServer().getConsole().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, "An update for ", TextColors.YELLOW, PluginInfo.NAME, TextColors.RED, " is available online!"));
            }
        });
    }

    @Listener
    public void onGameLoad(GameLoadCompleteEvent event)
    {
        //Start refilling chests that were created on the server before
        this.containerManager.restoreRefilling();
        this.containerManager.startLookingForEmptyContainers();
    }

    public ContainerManager getContainerManager()
    {
        return this.containerManager;
    }

    public ContainerScheduler getContainerScheduler()
    {
        return this.containerScheduler;
    }

    public ConsoleSource getConsole()
    {
        return Sponge.getServer().getConsole();
    }

    private void initCommands()
    {

        //Help Command
        SUBCOMMANDS.put(Arrays.asList("help"), CommandSpec.builder()
            .description(Text.of("Displays all available commands"))
            .permission(PluginPermissions.HELP_COMMAND)
            .executor(new HelpCommand(this))
            .build());

        //Create Command
        SUBCOMMANDS.put(Arrays.asList("c", "create"), CommandSpec.builder()
            .description(Text.of("Toggles chest creation mode"))
            .permission(PluginPermissions.CREATE_COMMAND)
            .arguments(GenericArguments.optional(GenericArguments.string(Text.of("chest name"))))
            .executor(new CreateCommand(this))
            .build());

        //Remove Command
        SUBCOMMANDS.put(Arrays.asList("r", "remove"), CommandSpec.builder()
            .description(Text.of("Toggles chest removal mode"))
            .permission(PluginPermissions.REMOVE_COMMAND)
            .executor(new RemoveCommand(this))
            .build());

        SUBCOMMANDS.put(Collections.singletonList("removeall"), CommandSpec.builder()
                .description(Text.of("Removes all refillable containers"))
                .permission(PluginPermissions.REMOVEALL_COMMAND)
                .executor(new RemoveAllCommand(this))
                .build());

        //Update Command
        SUBCOMMANDS.put(Arrays.asList("u", "update"), CommandSpec.builder()
            .description(Text.of("Toggles chest update mode"))
            .permission(PluginPermissions.UPDATE_COMMAND)
            .executor(new UpdateCommand(this))
            .build());

        //Time Command
        SUBCOMMANDS.put(Arrays.asList("t", "time"), CommandSpec.builder()
            .description(Text.of("Change chest's refill time"))
            .permission(PluginPermissions.TIME_COMMAND)
            .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("time"))))
            .executor(new TimeCommand(this))
            .build());

        //List Command
        SUBCOMMANDS.put(Arrays.asList("l","list"), CommandSpec.builder()
                .description(Text.of("Show all refilling chests"))
                .permission(PluginPermissions.LIST_COMMAND)
                .executor(new ListCommand(this))
                .build());

        //Refill Command
        SUBCOMMANDS.put(Collections.singletonList("refill"), CommandSpec.builder()
                .description(Text.of("Force refill a specific container"))
                .permission(PluginPermissions.REFILL_COMMAND)
                .arguments(GenericArguments.onlyOne(new ContainerNameArgument(Text.of("chest name"))))
                .executor(new RefillCommand(this))
                .build());

        //RefillAll Command
        SUBCOMMANDS.put(Collections.singletonList("refillall"), CommandSpec.builder()
                .description(Text.of("Force refill all containers"))
                .permission(PluginPermissions.REFILLALL_COMMAND)
                .executor(new RefillAllCommand(this))
                .build());

        //Setname Command
        SUBCOMMANDS.put(Collections.singletonList("setname"), CommandSpec.builder()
                .description(Text.of("Set name for a refillable container"))
                .permission(PluginPermissions.SETNAME_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("name"))))
                .executor(new SetnameCommand(this))
                .build());

        //CreateKit Command
        SUBCOMMANDS.put(Collections.singletonList("createkit"), CommandSpec.builder()
                .description(Text.of("Toggles kit creation mode"))
                .permission(PluginPermissions.CREATE_KIT_COMMAND)
                .arguments(GenericArguments.onlyOne(GenericArguments.string(Text.of("kit name"))))
                .executor(new CreateKitCommand(this))
                .build());

        //RemoveKit Command
        SUBCOMMANDS.put(Collections.singletonList("removekit"), CommandSpec.builder()
                .description(Text.of("Removes a kit"))
                .permission(PluginPermissions.REMOVE_KIT_COMMAND)
                .arguments(GenericArguments.onlyOne(new KitNameArgument(Text.of("kit name"))))
                .executor(new RemoveKitCommand(this))
                .build());

        //AssignKit Command
        SUBCOMMANDS.put(Collections.singletonList("assignkit"), CommandSpec.builder()
                .description(Text.of("Toggles assign mode"))
                .permission(PluginPermissions.ASSIGN_KIT_COMMAND)
                .arguments(GenericArguments.onlyOne(new KitNameArgument(Text.of("kit name"))))
                .executor(new AssignKitCommand(this))
                .build());

        //Kits Command
        SUBCOMMANDS.put(Collections.singletonList("kits"), CommandSpec.builder()
                .description(Text.of("Shows available kits"))
                .permission(PluginPermissions.KITS_COMMAND)
                .executor(new KitsCommand(this))
                .build());

        //ScanAndCreateCommand
        SUBCOMMANDS.put(Collections.singletonList("searchandcreate"), CommandSpec.builder()
                .description(Text.of("Scans selected region and converts found containers to refillable containers"))
                .permission(PluginPermissions.SEARCH_AND_CREATE_COMMAND)
                .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("restoreTime"))),
                        GenericArguments.optional(GenericArguments.string(Text.of("requiredPermission"))))
                .executor(new SearchAndCreateCommand(this))
                .build());

        //Deselect Command
        SUBCOMMANDS.put(Arrays.asList("deselect", "desel"), CommandSpec.builder()
                .description(Text.of("Clears selection points marked with ChestRefill's wand"))
                .permission(PluginPermissions.DESELECT_COMMAND)
                .executor(new DeselectCommand(this))
                .build());

        //Wand Command
        SUBCOMMANDS.put(Arrays.asList("wand"), CommandSpec.builder()
                .description(Text.of("Gives ChestRefill wand"))
                .permission(PluginPermissions.WAND_COMMAND)
                .executor(new WandCommand(this))
                .build());

        //Build all commands
        CommandSpec mainCommand = CommandSpec.builder()
                .description(Text.of("Displays all available commands"))
                .executor(new HelpCommand(this))
                .children(SUBCOMMANDS)
                .build();

        //Register commands
        Sponge.getCommandManager().register(this, mainCommand, "chestrefill", "cr");

    }

    private void initListeners()
    {
        Sponge.getEventManager().registerListeners(this, new RightClickListener(this));
        Sponge.getEventManager().registerListeners(this, new ContainerBreakListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerJoinListener(this));
        Sponge.getEventManager().registerListeners(this, new PlayerDisconnectListener(this));
        Sponge.getEventManager().registerListeners(this, new WandUsageListener(this));
//        Sponge.getEventManager().registerListeners(this, new DropItemListener());
    }

    private void registerTypeSerializers()
    {
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(RefillableItem.class), new RefillableItemTypeSerializer());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(Kit.class), new KitTypeSerializer());
        TypeSerializers.getDefaultSerializers().registerType(new TypeToken<List<RefillableItem>>(){}, new RefillableItemListTypeSerializer());
    }
}
