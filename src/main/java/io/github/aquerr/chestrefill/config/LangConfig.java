package io.github.aquerr.chestrefill.config;

import org.spongepowered.configurate.CommentedConfigurationNode;

public class LangConfig extends AbstractConfig
{
    private String languageTag = "en";

    public LangConfig(CommentedConfigurationNode configNode)
    {
        super(configNode);
    }

    @Override
    public void reload()
    {
        this.languageTag = getString("en", "language-tag");
    }

    public String getLanguageTag()
    {
        return languageTag;
    }
}
