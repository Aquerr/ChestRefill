package io.github.aquerr.chestrefill.config;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

public class VersionConfig extends AbstractConfig
{
    private boolean performVersionCheck = true;

    public VersionConfig(CommentedConfigurationNode configNode)
    {
        super(configNode);
    }

    @Override
    public void reload()
    {
        this.performVersionCheck = getBoolean(true, "version-check");
    }

    public boolean shouldPerformVersionCheck()
    {
        return this.performVersionCheck;
    }
}
