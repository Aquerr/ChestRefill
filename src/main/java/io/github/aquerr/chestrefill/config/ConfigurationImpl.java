package io.github.aquerr.chestrefill.config;

import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationImpl implements Configuration
{
    public static final String CONFIG_FILE_NAME = "config.conf";

    private Path configDirectoryPath;
    private Path configPath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    //Configs
    private Map<Class<? extends AbstractConfig>, AbstractConfig> configs = new HashMap<>();

    public ConfigurationImpl(Path configDir) throws IOException
    {
        this.configDirectoryPath = configDir;
        Files.createDirectories(this.configDirectoryPath);

        this.configPath = this.configDirectoryPath.resolve(CONFIG_FILE_NAME);
        Asset asset = Sponge.getAssetManager().getAsset(this, "config.conf").orElse(null);

        try
        {
            if (Files.notExists(this.configPath))
                Files.copy(asset.getUrl().openStream(), this.configPath);
        }
        catch (final IOException e)
        {
            throw new IllegalStateException(e);
        }

        this.configLoader = (HoconConfigurationLoader.builder()).setPath(this.configPath).build();
        loadConfiguration();

        this.configs.put(VersionConfig.class, new VersionConfig(this.configNode));
        reloadConfiguration();
    }

    public void reloadConfiguration() throws IOException
    {
        loadConfiguration();
        for (AbstractConfig value : this.configs.values())
        {
            value.reload(this.configNode);
        }
    }

    private void loadConfiguration() throws IOException
    {
        configNode = configLoader.load(ConfigurationOptions.defaults().withShouldCopyDefaults(true));
    }

    @Override
    public VersionConfig getVersionConfig()
    {
        return getConfig(VersionConfig.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractConfig> T getConfig(Class<T> clazz)
    {
        return (T)this.configs.get(clazz);
    }
}
