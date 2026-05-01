package com.sgly.aojiang.parkourgame;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.block.Block;
import org.bukkit.ChatColor;

import java.util.*;

public class ParkourGame extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {
    // 存储跑酷数据的映射（起点、终点和检查点）
    private final Map<String, ParkourData> parkourDataMap = new HashMap<>();
    // 临时存储玩家的物品栏
    private final Map<UUID, ItemStack[]> storedInventories = new HashMap<>();
    // 跟踪玩家的跑酷状态
    private final Map<UUID, PlayerParkourState> playerStates = new HashMap<>();
    // 存储玩家最好成绩
    private final Map<String, List<BestScore>> bestScoresMap = new HashMap<>();
    // 存储实时计时任务
    private final Map<UUID, BukkitRunnable> activeTimers = new HashMap<>();

    @Override
    public void onEnable() {
        // 注册命令和事件监听器
        if (this.getCommand("pk") != null) {
            this.getCommand("pk").setExecutor(this);
            this.getCommand("pk").setTabCompleter(this);
        } else {
            getLogger().severe("无法找到命令 'pk'，请检查 plugin.yml 文件。");
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ParkourGame 插件已启用");
    }

    @Override
    public void onDisable() {
        // 取消所有活动计时器
        for (BukkitRunnable timer : activeTimers.values()) {
            timer.cancel();
        }
        getLogger().info("ParkourGame 插件已禁用");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c[曙光领域] §f该命令只能由玩家执行");
            return true;
        }
        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage("§c[曙光领域] §f用法: /pk <command>");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "create":
                if (args.length < 3) {
                    player.sendMessage("§c[曙光领域] §f用法: /pk create <name> <s|e|cp> [number]");
                    return true;
                }
                String name = args[1];
                String type = args[2].toLowerCase();
                int checkpointNumber = -1;
                if (type.equals("cp")) {
                    if (args.length < 4) {
                        player.sendMessage("§c[曙光领域] §f用法: /pk create <name> cp <number>");
                        return true;
                    }
                    try {
                        checkpointNumber = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§c[曙光领域] §f检查点编号必须是一个整数");
                        return true;
                    }
                }
                handleCreateCommand(player, name, type, checkpointNumber);
                break;
            case "list":
                handleListCommand(player);
                break;
            case "tp":
                if (args.length < 2) {
                    player.sendMessage("§c[曙光领域] §f用法: /pk tp <name>");
                    return true;
                }
                handleTpCommand(player, args[1]);
                break;
            case "reload":
                handleReloadCommand(player);
                break;
            case "help":
                handleHelpCommand(player);
                break;
            default:
                player.sendMessage("§c[曙光领域] §f未知子命令");
                break;
        }
        return true;
    }

    // 处理创建命令
    private void handleCreateCommand(Player player, String name, String type, int checkpointNumber) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            player.sendMessage("§c[曙光领域] §f请对一个轻质测重压力板进行瞄准以创建跑酷点");
            return;
        }
        Location location = targetBlock.getLocation();
        ParkourData parkourData = parkourDataMap.getOrDefault(name, new ParkourData(name));
        switch (type) {
            case "s":
                if (parkourData.getEnd() != null) {
                    player.sendMessage("§c[曙光领域] §f终点已存在，请先删除终点后再设置起点");
                    return;
                }
                parkourData.setStart(location);
                player.sendMessage("§a[曙光领域] §f跑酷 " + name + " 的起点已设置");
                break;
            case "e":
                if (parkourData.getStart() == null) {
                    player.sendMessage("§c[曙光领域] §f请先设置起点");
                    return;
                }
                parkourData.setEnd(location);
                player.sendMessage("§a[曙光领域] §f跑酷 " + name + " 的终点已设置");
                player.sendMessage("§a[曙光领域] §f跑酷 " + name + " 已创建完成");
                break;
            case "cp":
                if (parkourData.getStart() == null || parkourData.getEnd() == null) {
                    player.sendMessage("§c[曙光领域] §f请先设置起点和终点，再添加检查点");
                    return;
                }
                if (checkpointNumber <= 0) {
                    player.sendMessage("§c[曙光领域] §f检查点编号必须是正整数");
                    return;
                }
                parkourData.addCheckpoint(checkpointNumber, location);
                player.sendMessage("§a[曙光领域] §f跑酷 " + name + " 的检查点 " + checkpointNumber + " 已添加");
                break;
            default:
                player.sendMessage("§c[曙光领域] §f无效的类型。使用 's' 表示起点，'e' 表示终点，或 'cp' 表示检查点");
                return;
        }
        parkourDataMap.put(name, parkourData);
    }

    // 处理列出跑酷课程的命令
    private void handleListCommand(Player player) {
        if (parkourDataMap.isEmpty()) {
            player.sendMessage("§c[曙光领域] §f没有可用的跑酷课程");
            return;
        }
        player.sendMessage("§a[曙光领域] §f可用的跑酷课程:");
        for (String parkourName : parkourDataMap.keySet()) {
            player.sendMessage("§e- " + parkourName);
        }
    }

    // 处理传送到跑酷位置的命令
    private void handleTpCommand(Player player, String name) {
        ParkourData parkourData = parkourDataMap.get(name);
        if (parkourData == null) {
            player.sendMessage("§c[曙光领域] §f未找到跑酷课程");
            return;
        }
        player.teleport(parkourData.getStart());
        player.sendMessage("§a[曙光领域] §f已传送到跑酷 " + name);
    }

    // 处理重载插件配置的命令
    private void handleReloadCommand(Player player) {
        // 配置重载逻辑的占位符
        player.sendMessage("§a[曙光领域] §f插件配置已重载");
    }

    // 处理帮助命令
    private void handleHelpCommand(Player player) {
        player.sendMessage("§a==== [曙光领域] ParkourGame 插件帮助 ====");
        player.sendMessage("§b/pk create <name> <s|e|cp> [number]§f - 创建一个跑酷点 (s: 起点, e: 终点, cp: 检查点, [number]: 检查点序号)");
        player.sendMessage("§b/pk list§f - 列出所有可用的跑酷课程");
        player.sendMessage("§b/pk tp <name>§f - 传送到指定跑酷的起点");
        player.sendMessage("§b/pk reload§f - 重载插件配置");
        player.sendMessage("§b/pk help§f - 显示插件命令帮助信息");
        player.sendMessage("§a作者: aojiangQAQ");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Block block = event.getClickedBlock();

        if (block == null || block.getType() != Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            return;
        }

        for (ParkourData parkourData : parkourDataMap.values()) {
            if (parkourData.getStart().equals(block.getLocation())) {
                if (parkourData.getEnd() == null) {
                    player.sendMessage("§c[曙光领域] §f终点尚未设置，无法开始跑酷");
                    return;
                }
                // 防止重复触发起点
                if (playerStates.containsKey(playerId) && playerStates.get(playerId).isRunning()) {
                    player.sendMessage("§c[曙光领域] §f跑酷已经开始，不能重复触发起点");
                    return;
                }
                // 玩家踩到起点，开始跑酷
                PlayerParkourState state = new PlayerParkourState(parkourData.getStart());
                playerStates.put(playerId, state);
                state.setRunning(true);
                state.setStartTime(System.currentTimeMillis());
                storedInventories.put(playerId, player.getInventory().getContents());
                setupParkourInventory(player);
                startRealTimeTimer(player, state);
                player.sendMessage("§a[曙光领域] §f跑酷已开始，加油！");
                return;
            }

            if (parkourData.getEnd().equals(block.getLocation())) {
                // 玩家踩到终点，结束跑酷
                PlayerParkourState state = playerStates.get(playerId);
                if (state != null && state.isRunning()) {
                    long totalTime = System.currentTimeMillis() - state.getStartTime();
                    endParkour(player, state);
                    player.sendMessage("§a[曙光领域] §f恭喜你完成了跑酷！总时间: " + (totalTime / 1000.0) + " 秒");
                    Location teleportLocation = state.getStartPoint().clone().add(2, 0, 2); // 传送到起点旁边
                    player.teleport(teleportLocation);
                }
                return;
            }

            for (Map.Entry<Integer, Location> entry : parkourData.getCheckpoints().entrySet()) {
                if (entry.getValue().equals(block.getLocation())) {
                    PlayerParkourState state = playerStates.get(playerId);
                    if (state != null && state.isRunning()) {
                        state.setLastCheckpoint(block.getLocation(), entry.getKey());
                        player.sendMessage("§e[曙光领域] §f到达检查点 " + entry.getKey());
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation(), 10);
                    }
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        if (playerStates.containsKey(playerId)) {
            endParkour(player, playerStates.get(playerId));
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        if (playerStates.containsKey(playerId)) {
            ItemStack item = event.getCurrentItem();
            if (item != null && (item.getType() == Material.BARRIER || item.getType() == Material.FEATHER)) {
                event.setCancelled(true); // 阻止移动特殊物品
            }
        }
    }

    private void endParkour(Player player, PlayerParkourState state) {
        UUID playerId = player.getUniqueId();
        if (playerStates.containsKey(playerId)) {
            playerStates.remove(playerId);
            player.getInventory().setContents(storedInventories.get(playerId));
            storedInventories.remove(playerId);
            if (activeTimers.containsKey(playerId)) {
                activeTimers.get(playerId).cancel();
                activeTimers.remove(playerId);
            }
        }
    }

    // 开始跑酷时设置物品栏
    private void setupParkourInventory(Player player) {
        ItemStack quitItem = new ItemStack(Material.BARRIER);
        ItemMeta quitMeta = quitItem.getItemMeta();
        if (quitMeta != null) {
            quitMeta.setDisplayName(ChatColor.RED + "退出跑酷");
            quitItem.setItemMeta(quitMeta);
        }

        ItemStack checkpointItem = new ItemStack(Material.FEATHER);
        ItemMeta checkpointMeta = checkpointItem.getItemMeta();
        if (checkpointMeta != null) {
            checkpointMeta.setDisplayName(ChatColor.GOLD + "返回上一个检查点");
            checkpointItem.setItemMeta(checkpointMeta);
        }

        player.getInventory().setItem(0, checkpointItem);
        player.getInventory().setItem(8, quitItem);
    }

    // 开始跑酷实时计时和显示
    private void startRealTimeTimer(Player player, PlayerParkourState state) {
        UUID playerId = player.getUniqueId();
        BukkitRunnable timer = new BukkitRunnable() {
            @Override
            public void run() {
                if (!state.isRunning()) {
                    this.cancel();
                    return;
                }
                long elapsedTime = System.currentTimeMillis() - state.getStartTime();
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.AQUA + "当前时间: " + (elapsedTime / 1000.0) + " 秒"));
            }
        };
        timer.runTaskTimer(this, 0L, 20L); // 每秒更新一次
        activeTimers.put(playerId, timer);
    }

    // 用于管理跑酷数据的类
    private static class ParkourData {
        private final String name;
        private Location start;
        private Location end;
        private final Map<Integer, Location> checkpoints;

        public ParkourData(String name) {
            this.name = name;
            this.checkpoints = new HashMap<>();
        }

        public void setStart(Location start) {
            this.start = start;
        }

        public void setEnd(Location end) {
            this.end = end;
        }

        public void addCheckpoint(int number, Location checkpoint) {
            this.checkpoints.put(number, checkpoint);
        }

        public Location getStart() {
            return start;
        }

        public Location getEnd() {
            return end;
        }

        public Map<Integer, Location> getCheckpoints() {
            return checkpoints;
        }

        public int getCheckpointNumber(Location location) {
            for (Map.Entry<Integer, Location> entry : checkpoints.entrySet()) {
                if (entry.getValue().equals(location)) {
                    return entry.getKey();
                }
            }
            return -1;
        }

        public String getName() {
            return name;
        }
    }

    // 用于管理玩家跑酷状态的类
    private static class PlayerParkourState {
        private final Location startPoint;
        private Location lastCheckpoint;
        private int lastCheckpointNumber;
        private boolean running;
        private long startTime;

        public PlayerParkourState(Location startPoint) {
            this.startPoint = startPoint;
            this.running = false;
            this.lastCheckpointNumber = 0;
        }

        public Location getStartPoint() {
            return startPoint;
        }

        public Location getLastCheckpoint() {
            return lastCheckpoint;
        }

        public void setLastCheckpoint(Location lastCheckpoint, int checkpointNumber) {
            this.lastCheckpoint = lastCheckpoint;
            this.lastCheckpointNumber = checkpointNumber;
        }

        public int getLastCheckpointNumber() {
            return lastCheckpointNumber;
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }
    }

    // 用于存储最佳成绩的类
    private static class BestScore {
        private final String playerName;
        private final long time;

        public BestScore(String playerName, long time) {
            this.playerName = playerName;
            this.time = time;
        }

        public String getPlayerName() {
            return playerName;
        }

        public long getTime() {
            return time;
        }
    }
}
