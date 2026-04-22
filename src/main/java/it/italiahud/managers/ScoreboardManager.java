package it.italiahud.managers;

import it.italiahud.ItaliaHUD;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final ItaliaHUD plugin;
    private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();
    private final Map<UUID, Integer> kills = new HashMap<>();
    private final Map<UUID, Integer> deaths = new HashMap<>();

    public ScoreboardManager(ItaliaHUD plugin) {
        this.plugin = plugin;
    }

    public void addKill(UUID uuid) { kills.put(uuid, kills.getOrDefault(uuid, 0) + 1); }
    public void addDeath(UUID uuid) { deaths.put(uuid, deaths.getOrDefault(uuid, 0) + 1); }
    public int getKills(UUID uuid) { return kills.getOrDefault(uuid, 0); }
    public int getDeaths(UUID uuid) { return deaths.getOrDefault(uuid, 0); }

    public void setupScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager sbManager = Bukkit.getScoreboardManager();
        Scoreboard board = sbManager.getNewScoreboard();
        Objective obj = board.registerNewObjective("hud", Criteria.DUMMY,
                ChatColor.AQUA + "" + ChatColor.BOLD + "SteelThrone");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        scoreboards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
        updateScoreboard(player);
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = scoreboards.get(player.getUniqueId());
        if (board == null) return;
        Objective obj = board.getObjective("hud");
        if (obj == null) return;

        for (String entry : board.getEntries()) board.resetScores(entry);

        Economy eco = plugin.getEconomy();
        LuckPerms lp = plugin.getLuckPerms();

        String balance = eco != null ? formatMoney(eco.getBalance(player)) : "0";

        // Fazione tramite reflection (softdepend)
        String factionName = "Nessuna";
        String ruoloFazione = "";
        try {
            Plugin factionsPlugin = Bukkit.getPluginManager().getPlugin("ItaliaFactions");
            if (factionsPlugin != null && factionsPlugin.isEnabled()) {
                Method getFM = factionsPlugin.getClass().getMethod("getFactionManager");
                Object fm = getFM.invoke(factionsPlugin);
                Method getFactionByPlayer = fm.getClass().getMethod("getFactionByPlayer", UUID.class);
                Object faction = getFactionByPlayer.invoke(fm, player.getUniqueId());
                if (faction != null) {
                    Method getName = faction.getClass().getMethod("getName");
                    factionName = (String) getName.invoke(faction);
                    Method getRank = faction.getClass().getMethod("getRank", UUID.class);
                    Object rank = getRank.invoke(faction, player.getUniqueId());
                    if (rank != null) ruoloFazione = rank.toString();
                }
            }
        } catch (Exception ignored) {}

        String gruppo = "Giocatore";
        if (lp != null) {
            User user = lp.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                String g = user.getPrimaryGroup();
                gruppo = Character.toUpperCase(g.charAt(0)) + g.substring(1);
            }
        }

        int k = getKills(player.getUniqueId());
        int d = getDeaths(player.getUniqueId());

        int score = 12;
        setScore(obj, ChatColor.DARK_GRAY + "steelthrone.it", score--);
        setScore(obj, ChatColor.BLACK + "a", score--);
        setScore(obj, ChatColor.GRAY + "K: " + ChatColor.GREEN + k + ChatColor.GRAY + "  D: " + ChatColor.RED + d, score--);
        if (!ruoloFazione.isEmpty()) {
            setScore(obj, ChatColor.GRAY + "Grado: " + ChatColor.GOLD + ruoloFazione, score--);
        }
        setScore(obj, ChatColor.GRAY + "Faz: " + ChatColor.YELLOW + factionName, score--);
        setScore(obj, ChatColor.GRAY + "Ruolo: " + ChatColor.AQUA + gruppo, score--);
        setScore(obj, ChatColor.GRAY + "Soldi: " + ChatColor.GREEN + "$" + balance, score--);
        setScore(obj, ChatColor.BLACK + "b", score--);
        setScore(obj, ChatColor.DARK_AQUA + "" + ChatColor.STRIKETHROUGH + "-------------", score--);
    }

    private void setScore(Objective obj, String text, int score) {
        if (text.length() > 40) text = text.substring(0, 40);
        obj.getScore(text).setScore(score);
    }

    private String formatMoney(double amount) {
        if (amount >= 1_000_000_000) return String.format("%.1fMld", amount / 1_000_000_000);
        if (amount >= 1_000_000) return String.format("%.1fMln", amount / 1_000_000);
        if (amount >= 1_000) return String.format("%.1fK", amount / 1_000);
        return String.valueOf((long) amount);
    }

    public void removeScoreboard(Player player) { scoreboards.remove(player.getUniqueId()); }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) updateScoreboard(player);
    }
}
