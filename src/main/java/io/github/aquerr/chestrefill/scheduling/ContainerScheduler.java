package io.github.aquerr.chestrefill.scheduling;

import io.github.aquerr.chestrefill.ChestRefill;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Scheduler;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ContainerScheduler
{
    private final Map<String, ScheduledTask> tasks;
    private final ChestRefill plugin;

    private final Scheduler syncScheduler;
    private final Scheduler asyncScheduler;

    public ContainerScheduler(ChestRefill plugin, final Scheduler syncScheduler, final Scheduler asyncScheduler)
    {
        this.plugin = plugin;
        this.tasks = new HashMap<>();
        this.syncScheduler = syncScheduler;
        this.asyncScheduler = asyncScheduler;
    }

    public Map<String, ScheduledTask> getScheduledTasks()
    {
        return this.tasks;
    }

    public void scheduleWithInterval(String name, long interval, TimeUnit timeUnit, Runnable runnable)
    {
        if(tasks.containsKey(name))
            throw new IllegalArgumentException("Task with such name [" + name + "] already exists");

        Task task = Task.builder()
                .interval(interval, timeUnit)
                .execute(runnable)
                .plugin(this.plugin.getPluginContainer())
                .build();

        ScheduledTask scheduledTask = syncScheduler.submit(task, name);
        this.tasks.put(name, scheduledTask);
    }

    public void scheduleWithIntervalAsync(String name, long interval, TimeUnit timeUnit, Runnable runnable)
    {
        if(tasks.containsKey(name))
            throw new IllegalArgumentException("Task with such name [" + name + "] already exists");

        Task task = Task.builder()
                .interval(interval, timeUnit)
                .execute(runnable)
                .plugin(this.plugin.getPluginContainer())
                .build();
        ScheduledTask scheduledTask = asyncScheduler.submit(task, name);
        this.tasks.put(name, scheduledTask);
    }

    public void scheduleDelayedWithInterval(String name, long delay, TimeUnit delayTimeUnit, long interval, TimeUnit intervalTimeUnit, Runnable runnable)
    {
        if(tasks.containsKey(name))
            throw new IllegalArgumentException("Task with such name [" + name + "] already exists");

        Task task = Task.builder()
                .delay(delay, delayTimeUnit)
                .interval(interval, intervalTimeUnit)
                .execute(runnable)
                .plugin(this.plugin.getPluginContainer())
                .build();
        ScheduledTask scheduledTask = syncScheduler.submit(task, name);
        this.tasks.put(name, scheduledTask);
    }

    public void scheduleDelayedWithIntervalAsync(String name, long delay, TimeUnit delayTimeUnit, long interval, TimeUnit intervalTimeUnit, Runnable runnable)
    {
        if(tasks.containsKey(name))
            throw new IllegalArgumentException("Task with such name [" + name + "] already exists");

        Task task = Task.builder()
                .delay(delay, delayTimeUnit)
                .interval(interval, intervalTimeUnit)
                .execute(runnable)
                .plugin(this.plugin.getPluginContainer())
                .build();
        ScheduledTask scheduledTask = asyncScheduler.submit(task, name);
        this.tasks.put(name, scheduledTask);
    }

    public void cancelTask(String taskName)
    {
        ScheduledTask task = this.tasks.remove(taskName);
        if(task != null)
            task.cancel();
    }

    public Optional<ScheduledTask> getTask(String taskName)
    {
        if(this.tasks.containsKey(taskName))
            return Optional.of(this.tasks.get(taskName));
        return Optional.empty();
    }
}
