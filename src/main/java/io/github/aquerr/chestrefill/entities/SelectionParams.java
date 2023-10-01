package io.github.aquerr.chestrefill.entities;

import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class SelectionParams
{
    private final SelectionMode selectionMode;
    private final Consumer<ModeExecutionParams> executor;

    private final Map<String, Object> extraData;

    public SelectionParams(SelectionMode selectionMode, Consumer<ModeExecutionParams> executor)
    {
        this(selectionMode, executor, Collections.emptyMap());
    }

    public SelectionParams(SelectionMode selectionMode, Consumer<ModeExecutionParams> executor, Map<String, Object> extraData)
    {
        this.selectionMode = selectionMode;
        this.executor = executor;
        this.extraData = extraData;
    }

    public Consumer<ModeExecutionParams> getExecutor()
    {
        return executor;
    }

    public SelectionMode getSelectionMode()
    {
        return selectionMode;
    }

    public Map<String, Object> getExtraData()
    {
        return extraData;
    }
}
