package io.github.aquerr.chestrefill.scheduling;

import io.github.aquerr.chestrefill.ChestRefill;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ContainerScheduler
{
    private final Map<String, Task> tasks;
    private final ChestRefill plugin;

    public ContainerScheduler(ChestRefill plugin)
    {
        this.plugin = plugin;
        this.tasks = new HashMap<>();
    }

    public Map<String, Task> getScheduledTasks()
    {
        return this.tasks;
    }

    public void runDelayed(String name, long delay, TimeUnit timeUnit, Runnable runnable)
    {
        //Run and forget
        Task task = Task.builder()
                .name(name)
                .delay(delay, timeUnit)
                .execute(runnable)
                .submit(this.plugin);
    }

    public void scheduleWithInterval(String name, long interval, TimeUnit timeUnit, Runnable runnable)
    {
        if(tasks.containsKey(name))
            throw new IllegalArgumentException("Task with such name [" + name + "] already exists");

        Task task = Task.builder()
                .name(name)
                .interval(interval, timeUnit)
                .execute(runnable)
                .submit(this.plugin);

        this.tasks.put(name, task);
    }

    public void scheduleWithIntervalAsync(String name, long interval, TimeUnit timeUnit, Runnable runnable)
    {
        if(tasks.containsKey(name))
            throw new IllegalArgumentException("Task with such name [" + name + "] already exists");

        Task task = Task.builder()
                .name(name)
                .interval(interval, timeUnit)
                .execute(runnable)
                .async()
                .submit(this.plugin);

        this.tasks.put(name, task);
    }

    public void scheduleDelayedWithInterval(String name, long delay, TimeUnit delayTimeUnit, long interval, TimeUnit intervalTimeUnit, Runnable runnable)
    {
        if(tasks.containsKey(name))
            throw new IllegalArgumentException("Task with such name [" + name + "] already exists");

        Task task = Task.builder()
                .name(name)
                .delay(delay, delayTimeUnit)
                .interval(interval, intervalTimeUnit)
                .execute(runnable)
                .submit(this.plugin);

        this.tasks.put(name, task);
    }

    public void scheduleDelayedWithIntervalAsync(String name, long delay, TimeUnit delayTimeUnit, long interval, TimeUnit intervalTimeUnit, Runnable runnable)
    {
        if(tasks.containsKey(name))
            throw new IllegalArgumentException("Task with such name [" + name + "] already exists");

        Task task = Task.builder()
                .name(name)
                .delay(delay, delayTimeUnit)
                .interval(interval, intervalTimeUnit)
                .execute(runnable)
                .async()
                .submit(this.plugin);

        this.tasks.put(name, task);
    }

    public Task cancelTask(String taskName)
    {
        Task task = this.tasks.remove(taskName);

        if(task != null)
            task.cancel();
        return task;
    }

    public Optional<Task> getTask(String taskName)
    {
        if(this.tasks.containsKey(taskName))
            return Optional.of(this.tasks.get(taskName));
        return Optional.empty();
    }
}
