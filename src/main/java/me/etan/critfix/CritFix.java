package me.etan.critfix;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.Bukkit;
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

    private boolean useLowGroundFix = true;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        String minecraftVersion = Bukkit.getMinecraftVersion();

        // Check if the version is supported for low-ground fix
        if (!VersionHelper.isSupportedVersion(minecraftVersion)) {
            Bukkit.getLogger().warning("Unsupported version: " + minecraftVersion + ". Disabling low-ground fix.");
            useLowGroundFix = false;
        }

        try {
            Class<?> craftEntityClass = Class.forName("org.bukkit.craftbukkit.entity.CraftEntity");
            getHandleMethod = craftEntityClass.getMethod("getHandle");

            Class<?> nmsEntityClass = Class.forName("net.minecraft.world.entity.Entity");

            // Get the onGround field for the version, or disable the fix if it's not found
            String fieldName = VersionHelper.getOnGroundFieldName(minecraftVersion);
            if (fieldName == null) {
                Bukkit.getLogger().severe("Could not find onGround field for version: " + minecraftVersion + ". Disabling low-ground fix.");
                useLowGroundFix = false;
            } else {
                Bukkit.getLogger().info("Using onGround field: " + fieldName);
                onGroundField = nmsEntityClass.getField(fieldName);
                onGroundField.setAccessible(true);
            }

        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {}


    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        fallDistances.put(player, player.getFallDistance()); // Store the fall distance before it's reset
    }

    @EventHandler
    public void onPreDamage(PrePlayerAttackEntityEvent event) throws InvocationTargetException, IllegalAccessException {
        Player damager = event.getPlayer();

        float fallDistance = damager.getFallDistance();
        float oldFallDistance = fallDistances.getOrDefault(damager, 0f);
        double yVelocity = damager.getVelocity().getY();

        // Use old fall distance if fall distance was reset due to damager being attacked right before
        if (fallDistance == 0 && oldFallDistance > 0 && yVelocity < 0)
            damager.setFallDistance(oldFallDistance);

        // Set player to in-air if they're still falling
        if (useLowGroundFix && fallDistance > 0 && damager.isOnGround())
            onGroundField.set(getHandleMethod.invoke(damager), false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        fallDistances.remove(event.getPlayer());
    }
}
