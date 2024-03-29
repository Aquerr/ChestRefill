package io.github.aquerr.chestrefill;

import com.google.inject.Inject;
import io.github.aquerr.chestrefill.commands.AssignKitCommand;
import io.github.aquerr.chestrefill.commands.AssignLootTableCommand;
import io.github.aquerr.chestrefill.commands.CopyCommand;
import io.github.aquerr.chestrefill.commands.CreateCommand;
import io.github.aquerr.chestrefill.commands.CreateKitCommand;
import io.github.aquerr.chestrefill.commands.DeselectCommand;
import io.github.aquerr.chestrefill.commands.HelpCommand;
import io.github.aquerr.chestrefill.commands.InfoCommand;
import io.github.aquerr.chestrefill.commands.KitsCommand;
import io.github.aquerr.chestrefill.commands.ListCommand;
import io.github.aquerr.chestrefill.commands.RefillAllCommand;
import io.github.aquerr.chestrefill.commands.RefillCommand;
import io.github.aquerr.chestrefill.commands.RemoveAllCommand;
import io.github.aquerr.chestrefill.commands.RemoveCommand;
import io.github.aquerr.chestrefill.commands.RemoveKitCommand;
import io.github.aquerr.chestrefill.commands.SearchAndCreateCommand;
import io.github.aquerr.chestrefill.commands.SetContainerNameCommand;
import io.github.aquerr.chestrefill.commands.SetHiddenIfNoItemsCommand;
import io.github.aquerr.chestrefill.commands.SetHidingBlockCommand;
import io.github.aquerr.chestrefill.commands.SetOpenMessageCommand;
import io.github.aquerr.chestrefill.commands.SetPlaceItemsInRandomSlotsCommand;
import io.github.aquerr.chestrefill.commands.TimeCommand;
import io.github.aquerr.chestrefill.commands.UpdateCommand;
import io.github.aquerr.chestrefill.commands.WandCommand;
import io.github.aquerr.chestrefill.commands.SetIndestructibilityCommand;
import io.github.aquerr.chestrefill.commands.arguments.ChestRefillCommandParameters;
import io.github.aquerr.chestrefill.config.Configuration;
import io.github.aquerr.chestrefill.config.ConfigurationImpl;
import io.github.aquerr.chestrefill.entities.SelectionParams;
import io.github.aquerr.chestrefill.entities.SelectionPoints;
import io.github.aquerr.chestrefill.listeners.ContainerBreakListener;
import io.github.aquerr.chestrefill.listeners.PlayerDisconnectListener;
import io.github.aquerr.chestrefill.listeners.PlayerJoinListener;
import io.github.aquerr.chestrefill.listeners.RightClickListener;
import io.github.aquerr.chestrefill.listeners.WandUsageListener;
import io.github.aquerr.chestrefill.managers.ContainerManager;
import io.github.aquerr.chestrefill.messaging.ChestRefillMessageSource;
import io.github.aquerr.chestrefill.messaging.MessageSource;
import io.github.aquerr.chestrefill.scheduling.ContainerScheduler;
import io.github.aquerr.chestrefill.util.LootTableHelper;
import io.github.aquerr.chestrefill.util.resource.Resource;
import io.github.aquerr.chestrefill.util.resource.ResourceUtils;
import io.github.aquerr.chestrefill.version.VersionChecker;
import org.apache.logging.log4j.Logger;
import org.bstats.sponge.Metrics;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.EventManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static io.github.aquerr.chestrefill.PluginInfo.PLUGIN_PREFIX_PLAIN;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Plugin(PluginInfo.ID)
public class ChestRefill
{
    public static final Map<List<String>, Command.Parameterized> SUBCOMMANDS = new HashMap<>();
    public static final Map<UUID, SelectionParams> SELECTION_MODE = new HashMap<>();
    public static final Map<UUID, SelectionPoints> PLAYER_SELECTION_POINTS = new HashMap<>();

    private Configuration configuration;

    private ContainerScheduler containerScheduler;
    private ContainerManager containerManager;

    private static ChestRefill chestRefill;

    private final Path configDir;
    private final PluginContainer pluginContainer;
    private final Logger logger;

    // Metrics
    private final Metrics metrics;

    private MessageSource messageSource;

    private boolean isDisabled = false;

    @Inject
    public ChestRefill(final PluginContainer pluginContainer,
                       @ConfigDir(sharedRoot = false) final Path configDir,
                       Metrics.Factory factory)
    {
        chestRefill = this;
        this.pluginContainer = pluginContainer;
        this.logger = pluginContainer.logger();
        this.configDir = configDir;
        this.metrics = factory.make(6827);
    }

    public static ChestRefill getInstance()
    {
        return chestRefill;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public PluginContainer getPluginContainer()
    {
        return this.pluginContainer;
    }

    public Path getConfigDir()
    {
        return configDir;
    }

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public MessageSource getMessageSource()
    {
        return messageSource;
    }

    @Listener
    public void onPluginConstruct(ConstructPluginEvent event)
    {
        try
        {
            setupConfigs();

            this.containerManager = new ContainerManager(this, getConfigDir(), new LootTableHelper(this));

            this.logger.info(PLUGIN_PREFIX_PLAIN + "Loading Chest Refill...");

            setupManagers();

            CompletableFuture.runAsync(this::checkVersionAndNotify);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            disablePlugin();
        }
    }

    private void disablePlugin()
    {
        this.isDisabled = true;
        Sponge.eventManager().unregisterListeners(this);
//        Sponge.server().commandManager().registrar(Command.Parameterized.class).get()
//        Sponge.server().commandManager().getOwnedBy(this).forEach(Sponge.getCommandManager()::removeMapping);
        this.logger.info(PLUGIN_PREFIX_PLAIN + PluginInfo.NAME + " has been disabled due to an error!");
    }

    private void setupConfigs() throws IOException
    {
        Resource resource = ResourceUtils.getResource("assets/chestrefill/" + ConfigurationImpl.CONFIG_FILE_NAME);
        if (resource == null)
            return;

        this.configuration = new ConfigurationImpl(this.pluginContainer, configDir, resource);
    }

    @Listener
    public void onRegisterCommand(final RegisterCommandEvent<Command.Parameterized> event)
    {
        if (isDisabled)
            return;

        initCommands(event);
        this.logger.info(PLUGIN_PREFIX_PLAIN + "Commands loaded!");
    }

    @Listener
    public void onGameLoad(LoadedGameEvent event)
    {
        if (isDisabled)
            return;

        try
        {
            //Load cache
            this.containerManager.refreshCache();

            initListeners();

            this.containerScheduler = new ContainerScheduler(this, Sponge.server().scheduler(), Sponge.game().asyncScheduler());

            //Start refilling chests that were created on the server before
            this.containerManager.restoreRefilling();
            this.containerManager.startLookingForEmptyContainers();

            this.logger.info(PLUGIN_PREFIX_PLAIN + "Chest Refill load complete!");
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    public ContainerManager getContainerManager()
    {
        return this.containerManager;
    }

    public ContainerScheduler getContainerScheduler()
    {
        return this.containerScheduler;
    }

    public Server getServer()
    {
        return Sponge.server();
    }

    private void initCommands(final RegisterCommandEvent<Command.Parameterized> event)
    {
        ChestRefillCommandParameters.init(this.containerManager);

        //Help Command
        registerCommand(singletonList("help"), "command.help.desc", PluginPermissions.HELP_COMMAND, new HelpCommand(this), Parameter.integerNumber().key("page").optional().build());
        registerCommand(asList("c", "create"), "command.create.desc", PluginPermissions.CREATE_COMMAND, new CreateCommand(this), Parameter.string().key("name").optional().build());
        registerCommand(singletonList("copy"), "command.copy.desc", PluginPermissions.COPY_COMMAND, new CopyCommand(this));
        registerCommand(asList("r", "remove"), "command.remove.desc", PluginPermissions.REMOVE_COMMAND, new RemoveCommand(this));
        registerCommand(singletonList("remove_all"), "command.removeall.desc", PluginPermissions.REMOVEALL_COMMAND, new RemoveAllCommand(this));
        registerCommand(asList("u", "update"), "command.removeall.desc", PluginPermissions.UPDATE_COMMAND, new UpdateCommand(this));
        registerCommand(singletonList("info"), "command.info.desc", PluginPermissions.INFO_COMMAND, new InfoCommand(this));
        registerCommand(asList("t", "time"), "command.time.desc", PluginPermissions.TIME_COMMAND, new TimeCommand(this), Parameter.integerNumber().key("time").build());
        registerCommand(asList("l", "list"), "command.list.desc", PluginPermissions.LIST_COMMAND, new ListCommand(this));
        registerCommand(singletonList("refill"), "command.refill.desc", PluginPermissions.REFILL_COMMAND, new RefillCommand(this), ChestRefillCommandParameters.refillableContainer());
        registerCommand(singletonList("refill_all"), "command.refillall.desc", PluginPermissions.REFILLALL_COMMAND, new RefillAllCommand(this));
        registerCommand(singletonList("set_container_name"), "command.setcontainername.desc", PluginPermissions.SET_CONTAINER_NAME_COMMAND, new SetContainerNameCommand(this), Parameter.string().key("name").build());
        registerCommand(singletonList("set_open_message"), "command.setopenmessage.desc", PluginPermissions.SET_OPEN_MESSAGE_COMMAND, new SetOpenMessageCommand(this), Parameter.remainingJoinedStrings().optional().key("message").build());
        registerCommand(singletonList("create_kit"), "command.createkit.desc", PluginPermissions.CREATE_KIT_COMMAND, new CreateKitCommand(this), Parameter.string().key("name").build());
        registerCommand(singletonList("remove_kit"), "command.removekit.desc", PluginPermissions.REMOVE_KIT_COMMAND, new RemoveKitCommand(this), Parameter.string().key("name").build());
        registerCommand(singletonList("assign_kit"), "command.assignkit.desc", PluginPermissions.ASSIGN_KIT_COMMAND, new AssignKitCommand(this), ChestRefillCommandParameters.kit());
        registerCommand(singletonList("assign_loot_table"), "command.assignloottable.desc", PluginPermissions.ASSIGN_LOOT_TABLE_COMMAND, new AssignLootTableCommand(this), ChestRefillCommandParameters.lootTable());
        registerCommand(singletonList("kits"), "command.kits.desc", PluginPermissions.KITS_COMMAND, new KitsCommand(this));
        registerCommand(singletonList("search_and_create"), "command.searchandcreate.desc", PluginPermissions.SEARCH_AND_CREATE_COMMAND, new SearchAndCreateCommand(this),
                Parameter.integerNumber().key("restore_time").optional().build(),
                Parameter.string().key("required_permission").optional().build());
        registerCommand(asList("deselect", "desel"), "command.deselect.desc", PluginPermissions.DESELECT_COMMAND, new DeselectCommand(this));
        registerCommand(singletonList("wand"), "command.wand.desc", PluginPermissions.WAND_COMMAND, new WandCommand(this));
        registerCommand(singletonList("set_place_items_in_random_slots"), "command.setplaceitemsinrandomslots.desc", PluginPermissions.SET_PLACE_ITEMS_IN_RANDOM_SLOTS, new SetPlaceItemsInRandomSlotsCommand(this), Parameter.bool().key("value").build());
        registerCommand(singletonList("set_hidden_if_no_items"), "command.sethiddenifnoitems.desc", PluginPermissions.SET_HIDDEN_IF_NO_ITEMS, new SetHiddenIfNoItemsCommand(this), Parameter.bool().key("value").build());
        registerCommand(singletonList("set_hiding_block"), "command.sethidingblock.desc", PluginPermissions.SET_HIDING_BLOCK_COMMAND, new SetHidingBlockCommand(this), Parameter.blockState().key("hiding_block").build());
        registerCommand(singletonList("set_indestructible"), "command.setindestructibility.desc", PluginPermissions.SET_INDESTRUCTIBLE, new SetIndestructibilityCommand(this), Parameter.bool().key("value").build());

        //Build all commands
        Command.Parameterized commandChestRefill = Command.builder()
                .shortDescription(messageSource.resolveComponentWithMessage("command.help.desc"))
                .executor(new HelpCommand(this))
                .addChildren(SUBCOMMANDS)
                .build();

        //Register commands
        event.register(this.pluginContainer, commandChestRefill, "chestrefill", "cr");

    }

    private void registerCommand(List<String> aliases, String descriptionKey, String permission, CommandExecutor commandExecutor, Parameter... parameters)
    {
        SUBCOMMANDS.put(aliases, Command.builder()
                .shortDescription(messageSource.resolveComponentWithMessage(descriptionKey))
                .permission(permission)
                .executor(commandExecutor)
                .addParameters(parameters)
                .build());
    }

    private void setupManagers()
    {
        ChestRefillMessageSource.init(this.configuration.getLangConfig().getLanguageTag());
        this.messageSource = ChestRefillMessageSource.getInstance();
    }

    private void initListeners()
    {
        EventManager eventManager = Sponge.eventManager();
        eventManager.registerListeners(this.pluginContainer, new RightClickListener(this));
        eventManager.registerListeners(this.pluginContainer, new ContainerBreakListener(this));
        eventManager.registerListeners(this.pluginContainer, new PlayerJoinListener(this));
        eventManager.registerListeners(this.pluginContainer, new PlayerDisconnectListener(this));
        eventManager.registerListeners(this.pluginContainer, new WandUsageListener(this));
    }

    private void checkVersionAndNotify()
    {
        if (!this.configuration.getVersionConfig().shouldPerformVersionCheck())
        {
            this.logger.info("Version check: Disabled.");
            return;
        }

        if (!VersionChecker.getInstance().isLatest(PluginInfo.VERSION))
        {
            this.logger.info(PLUGIN_PREFIX_PLAIN + "A new version of " + PluginInfo.NAME + " is available online!");
        }
    }
}
