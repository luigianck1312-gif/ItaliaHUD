package it.italiahud.managers;

import it.italiahud.ItaliaHUD;
import it.italiafactions.managers.FactionManager;
import it.italiafactions.models.Faction;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

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

    public void addKill(UUID uuid) {
        kills.put(uuid, kills.getOrDefault(uuid, 0) + 1);
    }

    public void addDeath(UUID uuid) {
        deaths.put(uuid, deaths.getOrDefault(uuid, 0) + 1);
    }

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

        // Pulisci vecchi score
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        Economy eco = plugin.getEconomy();
        FactionManager fm = plugin.getFactionManager();
        LuckPerms lp = plugin.getLuckPerms();

        // Soldi
        String balance = eco != null ? String.valueOf((int) eco.getBalance(player)) : "0";

        // Fazione
        String factionName = "Nessuna";
        String ruoloFazione = "";
        if (fm != null) {
            Faction faction = fm.getFactionByPlayer(player.getUniqueId());
            if (faction != null) {
                factionName = faction.getName();
                Faction.Rank rank = faction.getRank(player.getUniqueId());
                ruoloFazione = rank != null ? rank.name() : "";
            }
        }

        // Ruolo LuckPerms
        String gruppo = "Giocatore";
        if (lp != null) {
            User user = lp.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                gruppo = user.getPrimaryGroup();
                gruppo = Character.toUpperCase(gruppo.charAt(0)) + gruppo.substring(1);
            }
        }

        // KD
        int k = getKills(player.getUniqueId());
        int d = getDeaths(player.getUniqueId());

        // Costruisci scoreboard (dal basso verso l'alto)
        int score = 12;

        // IP in fondo
        setScore(obj, ChatColor.DARK_GRAY + "steelthrone.it", score--);

        // Linea vuota
        setScore(obj, ChatColor.BLACK + "" + ChatColor.DARK_GRAY + "a", score--);

        // KD
        setScore(obj, ChatColor.GRAY + "K: " + ChatColor.GREEN + k + ChatColor.GRAY + "  D: " + ChatColor.RED + d, score--);

        // Fazione
        setScore(obj, ChatColor.GRAY + "Faz: " + ChatColor.YELLOW + factionName, score--);

        // Ruolo fazione se presente
        if (!ruoloFazione.isEmpty()) {
            setScore(obj, ChatColor.GRAY + "Grado: " + ChatColor.GOLD + ruoloFazione, score--);
        }

        // Ruolo server
        setScore(obj, ChatColor.GRAY + "Ruolo: " + ChatColor.AQUA + gruppo, score--);

        // Soldi
        setScore(obj, ChatColor.GRAY + "Soldi: " + ChatColor.GREEN + "$" + balance, score--);

        // Linea vuota
        setScore(obj, ChatColor.BLACK + "" + ChatColor.GRAY + "b", score--);

        // Separatore
        setScore(obj, ChatColor.DARK_AQUA + "" + ChatColor.STRIKETHROUGH + "-------------", score--);
    }

    private void setScore(Objective obj, String text, int score) {
        // Tronca a 40 caratteri
        if (text.length() > 40) text = text.substring(0, 40);
        Score s = obj.getScore(text);
        s.setScore(score);
    }

    public void removeScoreboard(Player player) {
        scoreboards.remove(player.getUniqueId());
    }

    public void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateScoreboard(player);
        }
    }
}
