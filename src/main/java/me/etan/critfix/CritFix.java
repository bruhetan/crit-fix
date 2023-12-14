package me.etan.critfix;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class CritFix extends JavaPlugin implements Listener {
    private final Map<Player, Float> fallDistances = new HashMap<>();

    private Method getHandleMethod;
    private Field onGroundField;

    FileConfiguration config = getConfig();
    boolean isFallDistanceEnabled = config.getBoolean("enable-fall-distance-fix");
    boolean isLowGroundEnabled = config.getBoolean("enable-low-ground-fix");

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        getConfig().options().copyDefaults(true);
        saveConfig();

        String nmsVersion = getNMSVersion();

        if (isLowGroundEnabled) {
            if (!VersionHelper.isSupportedVersion(nmsVersion)) {
                Bukkit.getLogger().warning("Unsupported version: " + nmsVersion + ". Disabling low-ground fix.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }

            try {
                Class<?> craftEntityClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftEntity");
                getHandleMethod = craftEntityClass.getMethod("getHandle");

                Class<?> nmsEntityClass = Class.forName("net.minecraft.world.entity.Entity");

                String fieldName = VersionHelper.getOnGroundFieldName(nmsVersion);
                if (fieldName == null) {
                    Bukkit.getLogger().severe("Unsupported version: " + nmsVersion + ". Disabling low-ground fix.");
                    getServer().getPluginManager().disablePlugin(this);
                    return;
                }

                onGroundField = nmsEntityClass.getField(fieldName);
                onGroundField.setAccessible(true);
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onDisable() {}


    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isFallDistanceEnabled) return;
        Player player = event.getPlayer();
        fallDistances.put(player, player.getFallDistance());
    }

    @EventHandler
    public void onPreDamage(PrePlayerAttackEntityEvent event) throws InvocationTargetException, IllegalAccessException {
        Player damager = event.getPlayer();

        float fallDistance = damager.getFallDistance();
        float oldFallDistance = fallDistances.getOrDefault(damager, 0f);
        double yVelocity = damager.getVelocity().getY();

        if (isFallDistanceEnabled && fallDistance == 0 && oldFallDistance > 0 && yVelocity < 0)
            damager.setFallDistance(oldFallDistance);

        if (isLowGroundEnabled && fallDistance > 0 && damager.isOnGround())
            onGroundField.set(getHandleMethod.invoke(damager), false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        fallDistances.remove(event.getPlayer());
    }

    public static String getNMSVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 1);
    }
}
