package io.github.coolcrabs.brachyura.project;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.coolcrabs.brachyura.exception.TaskFailedException;

class Tasks implements Consumer<Task> {
    public final Map<String, Task> t = new HashMap<>();

    public void accept(Task task) {
        if (t.putIfAbsent(task.name, task) != null) {
            throw new TaskFailedException("Duplicate task for " + task.name);
        }
    }

    Task get(String name) {
        return Objects.requireNonNull(t.get(name), "Unknown task " + name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Task> a : t.entrySet()) {
            sb.append(a.getKey());
            sb.append(", ");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
            return sb.toString();
        } else {
            return "[None]";
        }
    }
}
