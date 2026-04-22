package it.italiahud.listeners;

import it.italiahud.ItaliaHUD;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HUDListener implements Listener {

    private final ItaliaHUD plugin;

    public HUDListener(ItaliaHUD plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        plugin.getScoreboardManager().setupScoreboard(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getScoreboardManager().removeScoreboard(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();

        plugin.getScoreboardManager().addDeath(victim.getUniqueId());

        if (killer != null && !killer.equals(victim)) {
            plugin.getScoreboardManager().addKill(killer.getUniqueId());
        }
    }
}
