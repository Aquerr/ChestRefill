package io.github.aquerr.chestrefill.config;

import io.github.aquerr.chestrefill.util.resource.Resource;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigurationImpl implements Configuration
{
    public static final String CONFIG_FILE_NAME = "config.conf";

    private Path configDirectoryPath;
    private Path configPath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    //Configs
    private final LangConfig langConfig;

    public ConfigurationImpl(PluginContainer pluginContainer, Path configDir, Resource configAsset) throws IOException
    {
        this.configDirectoryPath = configDir;
        Files.createDirectories(this.configDirectoryPath);

        this.configPath = this.configDirectoryPath.resolve(CONFIG_FILE_NAME);

        try
        {
            if (Files.notExists(this.configPath))
                Files.copy(configAsset.getInputStream(), this.configPath);
        }
        catch (final IOException e)
        {
            throw new IllegalStateException(e);
        }

        this.configLoader = (HoconConfigurationLoader.builder()).path(this.configPath).build();
        loadConfiguration();

        this.langConfig = new LangConfig(this.configNode);
        reloadConfiguration();
    }

    public void reloadConfiguration() throws IOException
    {
        loadConfiguration();
        this.langConfig.reload(this.configNode);
    }

    private void loadConfiguration() throws IOException
    {
        configNode = configLoader.load(ConfigurationOptions.defaults().shouldCopyDefaults(true));
    }

    @Override
    public LangConfig getLangConfig()
    {
        return langConfig;
    }
}
