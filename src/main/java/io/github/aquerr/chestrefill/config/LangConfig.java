package io.github.aquerr.chestrefill.config;

import org.spongepowered.configurate.CommentedConfigurationNode;

public class LangConfig extends AbstractConfig
{
    private String languageTag = "en_US";

    public LangConfig(CommentedConfigurationNode configNode)
    {
        super(configNode);
    }

    @Override
    public void reload()
    {
        this.languageTag = getString("en_US", "language-tag");
    }

    public String getLanguageTag()
    {
        return languageTag;
    }
}
