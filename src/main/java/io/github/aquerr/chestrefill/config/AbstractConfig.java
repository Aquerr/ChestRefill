package io.github.aquerr.chestrefill.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

abstract class AbstractConfig
{
    private ConfigurationNode configNode;

    protected AbstractConfig(ConfigurationNode configNode)
    {
        this.configNode = configNode;
    }

    protected void reload(ConfigurationNode configNode)
    {
        this.configNode = configNode;
        reload();
    }

    protected abstract void reload();

    protected int getInt(final int defaultValue, final Object... nodePath)
    {
        return configNode.getNode(nodePath).getInt(defaultValue);
    }

    protected double getDouble(final double defaultValue, final Object... nodePath)
    {
        return configNode.getNode(nodePath).getDouble(defaultValue);
    }

    protected float getFloat(final float defaultValue, final Object... nodePath)
    {
        return configNode.getNode(nodePath).getFloat(defaultValue);
    }

    protected boolean getBoolean(final boolean defaultValue, final Object... nodePath)
    {
        return configNode.getNode(nodePath).getBoolean(defaultValue);
    }

    protected String getString(final String defaultValue, final Object... nodePath)
    {
        return configNode.getNode(nodePath).getString(defaultValue);
    }

    protected List<String> getListOfStrings(final Collection<String> defaultValue, final Object... nodePath)
    {
        try
        {
            return configNode.getNode(nodePath).getList(TypeToken.of(String.class), new ArrayList<>(defaultValue));
        }
        catch(ObjectMappingException e)
        {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    protected Set<String> getSetOfStrings(final Collection<String> defaultValue, final Object... nodePath)
    {
        try
        {
            return new HashSet<>(configNode.getNode(nodePath).getList(TypeToken.of(String.class), new ArrayList<>(defaultValue)));
        }
        catch (ObjectMappingException e)
        {
            e.printStackTrace();
        }
        return new HashSet<>();
    }

}

