import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StartPlugin extends JavaPlugin implements CommandExecutor, Listener {

    private Map<String, List<Player>> parties = new HashMap<>();
    private Map<Player, Integer> points = new HashMap<>();
    private Map<Player, String> ranks = new HashMap<>();
    private Map<UUID, Integer> coins = new HashMap<>();
    private boolean pvpEnabled = false;
    private BukkitTask countdownTask;
    private BukkitTask winnerTask;

    private FileConfiguration ranksConfig;
    private File ranksFile;
    private FileConfiguration coinsConfig;
    private File coinsFile;

    @Override
    public void onEnable() {
        
        getCommand("party").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);

        
        loadRanks();
        loadCoins();

        getLogger().info("StartPlugin wurde aktiviert!");
    }

    @Override
    public void onDisable() {
        
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        if (winnerTask != null) {
            winnerTask.cancel();
        }

        
        saveRanks();
        saveCoins();

        getLogger().info("StartPlugin wurde deaktiviert!");
    }

    private void loadRanks() {
        ranksFile = new File(getDataFolder(), "ranks.yml");

        if (!ranksFile.exists()) {
            
            ranksFile.getParentFile().mkdirs();
            saveResource("ranks.yml", false);
        }

        ranksConfig = YamlConfiguration.loadConfiguration(ranksFile);

        
        ConfigurationSection ranksSection = ranksConfig.getConfigurationSection("ranks");
        if (ranksSection != null) {
            for (String playerName : ranksSection.getKeys(false)) {
                Player player = Bukkit.getPlayerExact(playerName);
                String rank = ranksSection.getString(playerName);
                if (player != null && rank != null) {
                    ranks.put(player, rank);
                }
            }
        }
    }

    private void saveRanks() {
        ConfigurationSection ranksSection = ranksConfig.createSection("ranks");
        for (Map.Entry<Player, String> entry : ranks.entrySet()) {
            Player player = entry.getKey();
            String rank = entry.getValue();
            ranksSection.set(player.getName(), rank);
        }

        try {
            ranksConfig.save(ranksFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCoins() {
        coinsFile = new File(getDataFolder(), "coins.yml");

        if (!coinsFile.exists()) {
            coinsFile.getParentFile().mkdirs();
            saveResource("coins.yml", false);
        }

        coinsConfig = YamlConfiguration.loadConfiguration(coinsFile);
      
        ConfigurationSection coinsSection = coinsConfig.getConfigurationSection("coins");
        if (coinsSection != null) {
            for (String playerUUID : coinsSection.getKeys(false)) {
                Player player = Bukkit.getPlayer(UUID.fromString(playerUUID));
                int coinAmount = coinsSection.getInt(playerUUID);
                if (player != null) {
                    coins.put(player.getUniqueId(), coinAmount);
                }
            }
        }
    }

    private void saveCoins() {
        ConfigurationSection coinsSection = coinsConfig.createSection("coins");
        for (Map.Entry<UUID, Integer> entry : coins.entrySet()) {
            UUID playerUUID = entry.getKey();
            int coinAmount = entry.getValue();
            coinsSection.set(playerUUID.toString(), coinAmount);
        }

        try {
            coinsConfig.save(coinsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setRank(Player player, String rank) {
        ranks.put(player, rank);
    }

    private String getRank(Player player) {
        return ranks.getOrDefault(player, "Spieler");
    }

    private void addCoins(Player player, int amount) {
        int currentCoins = coins.getOrDefault(player.getUniqueId(), 0);
        coins.put(player.getUniqueId(), currentCoins + amount);
    }
}
