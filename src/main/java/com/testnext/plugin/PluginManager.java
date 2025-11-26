package com.testnext.plugin;

import com.testnext.execution.StepExecutor;
import com.testnext.execution.StepExecutorRegistry;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

/**
 * Simple plugin manager that loads JARs from a plugins directory and registers StepExecutor implementations
 * that provide META-INF/services/com.testnext.execution.StepExecutor entries.
 */
public class PluginManager {
    private final File pluginsDir;
    private final StepExecutorRegistry registry;

    public PluginManager(String pluginsPath, StepExecutorRegistry registry) {
        this.pluginsDir = new File(pluginsPath);
        this.registry = registry;
    }

    public void discoverAndLoad() throws Exception {
        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) return;
        File[] jars = pluginsDir.listFiles((d, name) -> name.endsWith(".jar"));
        if (jars == null) return;
        for (File jar : jars) {
            URL url = jar.toURI().toURL();
            URLClassLoader cl = new URLClassLoader(new URL[]{url}, this.getClass().getClassLoader());
            ServiceLoader<StepExecutor> loader = ServiceLoader.load(StepExecutor.class, cl);
            for (StepExecutor exec : loader) {
                // register using executor class simple name; plugin should ensure unique name or provide annotation
                registry.register(exec.getClass().getSimpleName().toLowerCase(), exec);
            }
        }
    }
}
