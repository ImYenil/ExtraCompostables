package net.choco.extracompostables;

import lombok.Getter;
import net.choco.extracompostables.manager.FileManager;
import net.minecraft.core.IRegistry;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.level.IMaterial;
import net.minecraft.world.level.block.BlockComposter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Main extends JavaPlugin
{

    @Getter
    private static Main instance;

    private FileManager fileManager;

    @Getter
    private static String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.RED + "ExtraCompostables" + ChatColor.DARK_GRAY + "]" + ChatColor.RESET;

    public void onEnable() {
        instance = this;

        long startTime = System.currentTimeMillis();

        fileManager = new FileManager(this);
        fileManager.getConfig("config.yml").copyDefaults(true).save();

        this.readConfig().forEach(this::registerCompostable);

        log(LOG_LEVEL.INFO, "The plugin has been activated (" + (System.currentTimeMillis() - startTime) / 1000.0 + "s)");
    }

    public void onDisable() {
        HandlerList.unregisterAll(this);

        getServer().getScheduler().cancelTasks(this);

        log(LOG_LEVEL.INFO, "The plugin has been disabled");
    }

    @NotNull
    private Map<Material, Integer> readConfig() {
        ConfigurationSection compostables = fileManager.getConfig("config.yml").get().getConfigurationSection("compostables");
        if (compostables == null)
            return Collections.emptyMap();
        Objects.requireNonNull(compostables);
        return compostables.getKeys(false).stream().collect(Collectors.toMap(Material::valueOf, compostables::getInt));
    }

    private void registerCompostable(@NotNull Material material, int chance) {
        Item item = (Item)IRegistry.Z.get(new MinecraftKey(material.getKey().toString()));
        BlockComposter.e.put((IMaterial) item, chance / 100.0F);
    }

    public static void log(LOG_LEVEL level, String text) {
        getInstance().getServer().getConsoleSender().sendMessage(getPREFIX() + " " + ChatColor.DARK_GRAY + "[" + level.getName() + ChatColor.DARK_GRAY + "] " + ChatColor.RESET + text);
    }

    public enum LOG_LEVEL
    {
        INFO("INFO", 0, ChatColor.GREEN + "INFO"),
        WARNING("WARNING", 1, ChatColor.YELLOW + "WARNING"),
        ERROR("ERROR", 2, ChatColor.RED + "ERROR"),
        DEBUG("DEBUG", 3, ChatColor.AQUA + "DEBUG");

        @Getter
        private String name;

        private LOG_LEVEL(String s, int n, String name) {
            this.name = name;
        }
    }
}
