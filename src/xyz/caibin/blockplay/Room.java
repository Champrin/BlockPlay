package xyz.caibin.blockplay;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3;

import java.util.*;


public class Room implements Listener {

    public BlockPlay plugin;
    public String id;

    public LinkedHashMap<String, Object> data;

    public Player gamePlayer = null;
    public int game = 0;
    public boolean finish = false;
    public int rank = 0;

    public Room(String id, BlockPlay plugin) {
        this.plugin = plugin;
        this.id = id;
        this.data = plugin.rooms_message.get(id);
        this.plugin.getServer().getScheduler().scheduleRepeatingTask(new RoomSchedule(this), 20);

    }

    public ArrayList<Integer> layout = new ArrayList<>(Arrays.asList(14, 1, 4, 5, 13, 9, 3, 11));

    @EventHandler
    public void onTouch(PlayerInteractEvent event) {
        if (this.game != 1) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        int x = (int) Math.round(Math.floor(block.x));
        int y = (int) Math.round(Math.floor(block.y));
        int z = (int) Math.round(Math.floor(block.z));
        int[] pos = {x, y, z};
        if (this.isInGame(player)) {
            if (this.isInArena(pos)) {
                event.setCancelled(true);
                this.updateBlock(block);
            }
        }

    }

    public void updateBlock(Block block) {
        if (block.getId() == 20) return;
        String direction = (String) this.data.get("direction");
        int x = (int) Math.round(Math.floor(block.x));
        int y = (int) Math.round(Math.floor(block.y));
        int z = (int) Math.round(Math.floor(block.z));

        if (checkBlock(block, new Vector3(x, y + 1, z))) {
            if (checkBlock(block, new Vector3(x, y - 1, z))) {
                if (direction.equals("x+") || direction.equals("x-")) {
                    if (checkBlock(block, new Vector3(x + 1, y, z))) {
                        checkBlock(block, new Vector3(x - 1, y, z));
                    }
                } else if (direction.equals("z+") || direction.equals("z-")) {
                    if (checkBlock(block, new Vector3(x, y, z + 1))) {
                        checkBlock(block, new Vector3(x, y, z - 1));
                    }
                }
            }
        }
        this.checkFinish(block);
    }

    public boolean checkBlock(Block blocK, Vector3 v3) {
        Level level = blocK.level;
        Block block = level.getBlock(v3);
        if (block.getId() == 20) {
            level.setBlock(blocK, Block.get(20, 0));
            level.setBlock(block, blocK);
            return false;
        }
        return true;
    }

    public void checkFinish(Block block) {
        ArrayList<Integer> check = new ArrayList<>();

        String direction = (String) this.data.get("direction");
        String[] p1 = ((String) this.data.get("pos1")).split("\\+");
        String[] p2 = ((String) this.data.get("pos2")).split("\\+");
        switch (direction) {
            case "x+": {
                int z = Integer.parseInt(p1[2]);
                for (int y = Integer.parseInt(p1[1]); y >= Integer.parseInt(p2[1]); y--) {
                    for (int x = Integer.parseInt(p1[0]); x <= Integer.parseInt(p2[0]); x++) {
                        Vector3 v3 = new Vector3(x, y, z);
                        int id = block.getLevel().getBlock(v3).getDamage();
                        check.add(id);
                    }
                }
                break;
            }
            case "x-": {
                int z = Integer.parseInt(p1[2]);
                for (int y = Integer.parseInt(p1[1]); y >= Integer.parseInt(p2[1]); y--) {
                    for (int x = Integer.parseInt(p1[0]); x >= Integer.parseInt(p2[0]); x--) {
                        Vector3 v3 = new Vector3(x, y, z);
                        int id = block.getLevel().getBlock(v3).getDamage();
                        check.add(id);
                    }
                }
                break;
            }
            case "z+": {
                int x = Integer.parseInt(p1[0]);
                for (int y = Integer.parseInt(p1[1]); y >= Integer.parseInt(p2[1]); y--) {
                    for (int z = Integer.parseInt(p1[2]); z <= Integer.parseInt(p2[2]); z++) {
                        Vector3 v3 = new Vector3(x, y, z);
                        int id = block.getLevel().getBlock(v3).getDamage();
                        check.add(id);
                    }
                }
                break;
            }
            case "z-": {
                int x = Integer.parseInt(p1[0]);
                for (int y = Integer.parseInt(p1[1]); y >= Integer.parseInt(p2[1]); y--) {
                    for (int z = Integer.parseInt(p1[2]); z >= Integer.parseInt(p2[2]); z--) {
                        Vector3 v3 = new Vector3(x, y, z);
                        int id = block.getLevel().getBlock(v3).getDamage();
                        check.add(id);
                    }
                }
                break;
            }
        }
        if (this.layout.equals(check)) {
            this.finish = true;
        }
    }

    public void setGameArena() {
        String direction = (String) data.get("direction");

        String[] p1 = ((String) data.get("pos1")).split("\\+");
        String[] p2 = ((String) data.get("pos2")).split("\\+");
        int xi = (Math.min(Integer.parseInt(p1[0]), Integer.parseInt(p2[0])));
        int xa = (Math.max(Integer.parseInt(p1[0]), Integer.parseInt(p2[0])));
        int yi = (Math.min(Integer.parseInt(p1[1]), Integer.parseInt(p2[1])));
        int ya = (Math.max(Integer.parseInt(p1[1]), Integer.parseInt(p2[1])));
        int zi = (Math.min(Integer.parseInt(p1[2]), Integer.parseInt(p2[2])));
        int za = (Math.max(Integer.parseInt(p1[2]), Integer.parseInt(p2[2])));

        Level level = this.plugin.getServer().getLevelByName((String) data.get("room_world"));

        switch (direction) {
            case "x+":
            case "x-":
                for (int y = yi; y <= ya; y++) {
                    for (int x = xi; x <= xa; x++) {
                        level.setBlock(new Vector3(x, y, zi), Block.get(20, 0));
                    }
                }
                break;
            case "z+":
            case "z-":
                for (int y = yi; y <= ya; y++) {
                    for (int z = zi; z <= za; z++) {
                        level.setBlock(new Vector3(xi, y, z), Block.get(20, 0));
                    }
                }
                break;
        }

    }

    public boolean isInArena(int[] pos) {
        String[] p1 = ((String) data.get("pos1")).split("\\+");
        String[] p2 = ((String) data.get("pos2")).split("\\+");
        int xi = (Math.min(Integer.parseInt(p1[0]), Integer.parseInt(p2[0])));
        int xa = (Math.max(Integer.parseInt(p1[0]), Integer.parseInt(p2[0])));
        int yi = (Math.min(Integer.parseInt(p1[1]), Integer.parseInt(p2[1])));
        int ya = (Math.max(Integer.parseInt(p1[1]), Integer.parseInt(p2[1])));
        int zi = (Math.min(Integer.parseInt(p1[2]), Integer.parseInt(p2[2])));
        int za = (Math.max(Integer.parseInt(p1[2]), Integer.parseInt(p2[2])));
        return pos[0] >= xi && pos[0] <= xa && pos[1] >= yi && pos[1] <= ya && pos[2] >= zi && pos[2] <= za;
    }

    public boolean isInGame(Player p)//获取玩家当前状态
    {
        return gamePlayer == p;
    }

    public void joinToRoom(Player p) {
        if (this.gamePlayer != null) {
            p.sendMessage("> 已经有玩家加入游戏了");
            return;
        }
        if (this.isInGame(p)) {
            p.sendMessage("> 你已经加入一个游戏了");
            return;
        }
        this.finish = false;
        this.setGameArena();
        this.gamePlayer = p;
        p.sendMessage(">  你加入了游戏,等待游戏开始");
        p.sendMessage(">  输入@hub可退出游戏！");
    }

    public void setStartArena() {
        setStartArena_NumPLAY();
    }


    public void setStartArena_NumPLAY() {
        ArrayList<Integer> layout = new ArrayList<>(Arrays.asList(14, 1, 4, 5, 13, 9, 3, 11));
        Collections.shuffle(layout);

        String direction = (String) this.data.get("direction");
        String[] p1 = ((String) this.data.get("pos1")).split("\\+");
        String[] p2 = ((String) this.data.get("pos2")).split("\\+");
        Level level = this.plugin.getServer().getLevelByName((String) data.get("room_world"));
        int a = 0;
        switch (direction) {
            case "x+": {
                int z = Integer.parseInt(p1[2]);
                for (int y = Integer.parseInt(p1[1]); y >= Integer.parseInt(p2[1]); y--) {
                    for (int x = Integer.parseInt(p1[0]); x <= Integer.parseInt(p2[0]); x++) {
                        Vector3 v3 = new Vector3(x, y, z);
                        int mate = layout.get(a);
                        level.setBlock(v3, Block.get(35, mate));
                        a = a + 1;
                        if (a == 16) break;
                    }
                }
                break;
            }
            case "x-": {
                int z = Integer.parseInt(p1[2]);
                for (int y = Integer.parseInt(p1[1]); y >= Integer.parseInt(p2[1]); y--) {
                    for (int x = Integer.parseInt(p1[0]); x >= Integer.parseInt(p2[0]); x--) {
                        Vector3 v3 = new Vector3(x, y, z);
                        int mate = layout.get(a);
                        level.setBlock(v3, Block.get(35, mate));
                        a = a + 1;
                        if (a == 16) break;
                    }
                }
                break;
            }
            case "z+": {
                int x = Integer.parseInt(p1[0]);
                for (int y = Integer.parseInt(p1[1]); y >= Integer.parseInt(p2[1]); y--) {
                    for (int z = Integer.parseInt(p1[2]); z <= Integer.parseInt(p2[2]); z++) {
                        if (a == 8) break;
                        Vector3 v3 = new Vector3(x, y, z);
                        int mate = layout.get(a);
                        level.setBlock(v3, Block.get(35, mate));
                        a = a + 1;

                    }
                }
                break;
            }
            case "z-": {
                int x = Integer.parseInt(p1[0]);
                for (int y = Integer.parseInt(p1[1]); y >= Integer.parseInt(p2[1]); y--) {
                    for (int z = Integer.parseInt(p1[2]); z >= Integer.parseInt(p2[2]); z--) {
                        if (a == 8) break;
                        Vector3 v3 = new Vector3(x, y, z);
                        int mate = layout.get(a);
                        level.setBlock(v3, Block.get(35, mate));
                        a = a + 1;
                    }
                }
                break;
            }
        }
    }


    public void startGame() {
        this.game = 1;
        setStartArena();
    }

    public void stopGame() {
        this.game = 0;

        if (gamePlayer != null) {
            gamePlayer.sendMessage(">>>   游戏结束");
        }
        this.gamePlayer = null;
        this.rank = 0;

        this.setGameArena();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (this.isInGame(event.getPlayer())) {
            this.stopGame();
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent event) {
        if (this.isInGame(event.getPlayer())) {
            this.stopGame();
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (this.isInGame(event.getEntity())) {
            this.stopGame();
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (this.isInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (this.isInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (this.isInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}