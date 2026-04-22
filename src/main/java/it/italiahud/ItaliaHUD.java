package it.italiahud;

import it.italiafactions.managers.FactionManager;
import it.italiahud.listeners.HUDListener;
import it.italiahud.managers.ScoreboardManager;
import net.luckperms.api.LuckPerms;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ItaliaHUD extends JavaPlugin {

    private ScoreboardManager scoreboardManager;
    private Economy economy;
    private LuckPerms luckPerms;
    private FactionManager factionManager;

    @Override
    public void onEnable() {
        getLogger().info("ItaliaHUD avviato!");

        setupEconomy();
        setupLuckPerms();
        setupFactionManager();

        scoreboardManager = new ScoreboardManager(this);

        getServer().getPluginManager().registerEvents(new HUDListener(this), this);

        // Aggiorna scoreboard ogni 2 secondi
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            scoreboardManager.updateAll();
        }, 40L, 40L);

        // Setup scoreboard per giocatori già online
        for (Player player : Bukkit.getOnlinePlayers()) {
            scoreboardManager.setupScoreboard(player);
        }

        getLogger().info("ItaliaHUD caricato con successo!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ItaliaHUD disattivato!");
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) economy = rsp.getProvider();
    }

    private void setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> rsp = getServer().getServicesManager().getRegistration(LuckPerms.class);
        if (rsp != null) luckPerms = rsp.getProvider();
    }

    private void setupFactionManager() {
        JavaPlugin factionsPlugin = (JavaPlugin) getServer().getPluginManager().getPlugin("ItaliaFactions");
        if (factionsPlugin instanceof it.italiafactions.ItaliaFactions italiaFactions) {
            factionManager = italiaFactions.getFactionManager();
        }
    }

    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public Economy getEconomy() { return economy; }
    public LuckPerms getLuckPerms() { return luckPerms; }
    public FactionManager getFactionManager() { return factionManager; }
}
