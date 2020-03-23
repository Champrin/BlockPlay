package xyz.caibin.blockplay;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;


public class BlockPlay extends PluginBase implements Listener {


    public Config config;
    public final String PLUGIN_NAME = "BlockPlay";
    public final String PLUGIN_No = "20";
    public final String PREFIX = "§a=§l§6BlockPlay§r§a=";
    public final String GAME_NAME = "方块华容道";
    public LinkedHashMap<String, LinkedHashMap<String, Object>> rooms_message = new LinkedHashMap<>();//房间基本信息
    public LinkedHashMap<String, Room> rooms = new LinkedHashMap<>();//开启的房间信息 存储Room实例
    public LinkedHashMap<String, LinkedHashMap<String, String>> setters = new LinkedHashMap<>();//房间设置信息

    @Override
    public void onEnable() {
        long start = new Date().getTime();
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info(PREFIX + "  §d加载中。。。§e|作者：Champrin");
        this.getLogger().info(PREFIX + "  §e ==> Champrin的第§c" + PLUGIN_No + "§e款插件/小游戏 " + GAME_NAME + "！");

        this.LoadConfig();
        this.LoadRoomConfig();

        this.getLogger().info(PREFIX + "  §d已加载完毕。。。");
        this.getLogger().info(PREFIX + "  §e加载耗时" + (new Date().getTime() - start) + "毫秒");
    }

    @Override
    public void onDisable() {
        //给每个房间结算结果
        if (!rooms.isEmpty()) {
            for (Map.Entry<String, Room> map : rooms.entrySet()) {
                map.getValue().stopGame();
            }
        }
    }

    public void LoadConfig() {
        this.getLogger().info("-配置文件加载中...");

        File file = new File(this.getDataFolder() + "/Room/");
        if (!file.exists()) {
            if (!file.mkdirs()) {
                this.getServer().getLogger().info("文件夹创建失败");
            }
        }
    }

    public void LoadRoomConfig() {
        this.getLogger().info("-房间信息加载中...");
        File file = new File(this.getDataFolder() + "/Room/");
        File[] files = file.listFiles();
        if (files != null) {
            for (File FILE : files) {
                if (FILE.isFile()) {
                    Config room = new Config(FILE, Config.YAML);
                    String FileName = FILE.getName().substring(0, FILE.getName().lastIndexOf("."));
                    rooms_message.put(FileName, new LinkedHashMap<>(room.getAll()));
                    if ("true".equals(room.get("state"))) {
                        this.setRoomData(FileName);
                        this.getLogger().info("   房间§b" + FileName + "§r加载完成");
                    }

                }
            }
        }
        this.getLogger().info("-房间信息加载完毕...");
    }

    public void setRoomData(String name) {
        Room game = new Room(name, this);
        rooms.put(name, game);
        this.getServer().getPluginManager().registerEvents(game, this);
    }

    public Config getRoomData(String room_name) {
        config = new Config(this.getDataFolder() + "/Room/" + room_name + ".yml", Config.YAML);
        return config;
    }


    public boolean RoomExist(String name)//判断房间是否存在
    {
        return rooms_message.containsKey(name);
    }

    public Room getPlayerRoom(Player p) {
        for (Map.Entry<String, Room> map : this.rooms.entrySet()) {
            Room room = map.getValue();
            if (room.gamePlayer == p) {
                return room;
            }
        }
        return null;
    }

    public boolean isRoomSet(String name) //判断房间是否存在
    {
        return rooms.containsKey(name);
    }

    public Room getRoom(String room_name) {
        return rooms.getOrDefault(room_name, null);
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        if (setters.containsKey(p.getName())) {
            event.setCancelled(true);
            Block b = event.getBlock();

            String room_name = setters.get(p.getName()).get("room_name");
            Config room = this.getRoomData(room_name);

            int x = (int) Math.round(Math.floor(b.x));
            int y = (int) Math.round(Math.floor(b.y));
            int z = (int) Math.round(Math.floor(b.z));
            String xyz = x + "+" + y + "+" + z;

            int step = Integer.parseInt(setters.get(p.getName()).get("step"));

            switch (step) {
                case 1:
                    setters.get(p.getName()).put("pos1", xyz);
                    room.set("pos1", xyz);
                    room.save();
                    p.sendMessage(">>  请设置点2");
                    setters.get(p.getName()).put("step", String.valueOf(step + 1));
                    break;
                case 2:
                    room.set("pos2", xyz);
                    p.sendMessage(">>  请设置加入游戏按钮");

                    String[] pos1 = setters.get(p.getName()).get("pos1").split("\\+");
                    String[] pos2 = xyz.split("\\+");

                    String d = null;
                    int length = 0;

                    if (pos1[2].equals(pos2[2]) && Integer.parseInt(pos1[0]) < Integer.parseInt(pos2[0]))//从pos1开始运作
                    {
                        d = "x+";
                        length = Math.abs(Math.max(Integer.parseInt(pos1[0]), Integer.parseInt(pos2[0])) - Math.min(Integer.parseInt(pos1[0]), Integer.parseInt(pos2[0]))) + 1;
                    } else if (pos1[2].equals(pos2[2]) && Integer.parseInt(pos1[0]) > Integer.parseInt(pos2[0])) {
                        d = "x-";
                        length = Math.abs(Math.max(Integer.parseInt(pos1[0]), Integer.parseInt(pos2[0])) - Math.min(Integer.parseInt(pos1[0]), Integer.parseInt(pos2[0]))) + 1;
                    } else if (pos1[0].equals(pos2[0]) && Integer.parseInt(pos1[2]) < Integer.parseInt(pos2[2])) {
                        d = "z+";
                        length = Math.abs(Math.max(Integer.parseInt(pos1[2]), Integer.parseInt(pos2[2])) - Math.min(Integer.parseInt(pos1[2]), Integer.parseInt(pos2[2]))) + 1;
                    } else if (pos1[0].equals(pos2[0]) && Integer.parseInt(pos1[2]) > Integer.parseInt(pos2[2])) {
                        d = "z-";
                        length = Math.abs(Math.max(Integer.parseInt(pos1[2]), Integer.parseInt(pos2[2])) - Math.min(Integer.parseInt(pos1[2]), Integer.parseInt(pos2[2]))) + 1;
                    }
                    int width = Math.abs(Math.min(Integer.parseInt(pos1[1]), Integer.parseInt(pos2[1])) - Math.max(Integer.parseInt(pos1[1]), Integer.parseInt(pos2[1]))) + 1;
                    int area = length * width;
                    room.set("direction", d);
                    room.set("area", area);//面积
                    room.set("length", length);
                    room.set("width", width);
                    room.save();
                    setters.get(p.getName()).put("step", String.valueOf(step + 1));
                    break;
                case 3:
                    if (b.getId() == 143) {
                        room.set("button_pos", xyz);
                        room.set("state", "true");
                        room.set("room_world", b.level.getName());
                        room.save();
                        rooms_message.put(room_name, (LinkedHashMap<String, Object>) room.getAll());
                        setRoomData(room_name);
                        rooms.get(room_name).setGameArena();
                        setters.remove(p.getName());
                        p.sendMessage(">>  房间设置已完成");
                    } else {
                        setters.get(p.getName()).put("step", "3");
                        p.sendMessage(">>  请破坏木质按钮");
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player p = event.getPlayer();
        if (getPlayerRoom(p) == null) return;
        if (event.getMessage().contains("@hub")) {
            event.setCancelled(true);
            p.sendMessage(">  你已退出游戏！");
            this.getPlayerRoom(p).stopGame();
        }
    }

    @EventHandler
    public void onTouch(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block b = event.getBlock();
        if (b.getId() == 143) {
            int x = (int) Math.round(Math.floor(b.x));
            int y = (int) Math.round(Math.floor(b.y));
            int z = (int) Math.round(Math.floor(b.z));
            String xyz = x + "+" + y + "+" + z;
            for (Map.Entry<String, LinkedHashMap<String, Object>> map : this.rooms_message.entrySet()) {
                if (xyz.equals(map.getValue().get("button_pos"))) {
                    if (this.getRoom(map.getKey()) != null) {
                        this.getRoom(map.getKey()).joinToRoom(player);
                    }
                    break;
                }
            }

        }
    }

    public void Op_HelpMessage(CommandSender sender) {
        sender.sendMessage(">  §b==========" + PREFIX + "==========§r");
        sender.sendMessage(">  /bplay add [房间名] ------ §d创建新房间");
        sender.sendMessage(">  /bplay set [房间名] ------ §d设置房间");
        sender.sendMessage(">  /bplay del [房间名] ------ §d删除房间");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if ("bplay".equals(command.getName())) {
            if (args.length < 1) {
                this.Op_HelpMessage(sender);
            } else {
                switch (args[0]) {
                    case "set":
                        if (sender instanceof Player) {
                            if (args.length < 2) {
                                sender.sendMessage(">  参数不足");
                                break;
                            }
                            if (!this.RoomExist(args[1])) {
                                sender.sendMessage(">  房间不存在");
                                break;
                            }
                            if (this.isRoomSet(args[1])) {
                                Room a = this.rooms.get(args[1]);
                                if (a.game != 0 || a.gamePlayer != null) {
                                    sender.sendMessage(">  房间正在游戏中");
                                    break;
                                }
                            }
                            LinkedHashMap<String, String> list = new LinkedHashMap<>();
                            list.put("room_name", args[1]);
                            list.put("step", String.valueOf(1));
                            setters.put(sender.getName(), list);
                            sender.sendMessage(">  房间" + args[1] + "正在设置");
                            sender.sendMessage(">>>  边框自行设置且不能使用羊毛 §a自行建造模板！");
                            sender.sendMessage(">>  §l§c!!!设置要求：§r请使用竖立平面且最左上角必须设为点1，最右下角必须设为点2");
                            sender.sendMessage(">>  请破坏方块设置点1，然后破坏一个§a3x3大小§r的竖直平面的方块设置点2");
                        } else {
                            sender.sendMessage(">  请在游戏中运行");
                        }
                        break;
                    case "add":
                        if (args.length < 2) {
                            sender.sendMessage(">  参数不足");
                            break;
                        }
                        if (this.RoomExist(args[1])) {
                            sender.sendMessage(">  房间已存在");
                            break;
                        }
                        Config a = new Config(this.getDataFolder() + "/Room/" + args[1] + ".yml", Config.YAML);
                        a.set("state", "false");
                        a.set("room_world", " ");
                        a.set("start_time", "5");
                        a.save();
                        rooms_message.put(args[1], (LinkedHashMap<String, Object>) a.getAll());
                        sender.sendMessage(">  房间" + args[1] + "成功创建");
                        break;
                    case "del":
                        if (args.length < 2) {
                            sender.sendMessage(">  参数不足");
                            break;
                        }
                        if (!this.RoomExist(args[1])) {
                            sender.sendMessage(">  房间不存在");
                            break;
                        }
                        boolean file = new File(this.getDataFolder() + "/Room/" + args[1] + ".yml").delete();
                        if (file) {
                            if (rooms.containsKey(args[1])) {
                                rooms.get(args[1]).stopGame();
                                rooms.remove(args[1]);
                            }
                            rooms_message.remove(args[1]);
                            sender.sendMessage(">  房间" + args[1] + "已成功删除");
                        } else {
                            sender.sendMessage(">  房间" + args[1] + "删除失败");
                        }
                        break;
                    case "help":
                    default:
                        this.Op_HelpMessage(sender);
                        break;
                }
            }
        }
        return true;
    }
}

