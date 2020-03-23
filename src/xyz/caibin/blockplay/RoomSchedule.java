package xyz.caibin.blockplay;

import cn.nukkit.Player;
import cn.nukkit.scheduler.Task;

public class RoomSchedule extends Task {

    private int startTime, spendTime = 0;
    private Room arena;

    public RoomSchedule(Room arena) {
        this.arena = arena;
        this.startTime = Integer.parseInt((String) this.arena.data.get("start_time"))+1;
    }

    @Override
    public void onRun(int tick) {
        if (this.arena.game == 0) {
            if (this.arena.gamePlayer != null) {

                this.startTime=startTime-1;
                Player p = this.arena.gamePlayer;
                p.sendPopup(new Countdown().countDown(startTime));


                if (this.startTime <= 0) {
                    this.arena.startGame();
                    this.spendTime = 0;
                    this.startTime = Integer.parseInt((String) this.arena.data.get("start_time"))+1;
                }
            } else {
                this.startTime = Integer.parseInt((String) this.arena.data.get("start_time"))+1;
                this.spendTime = 0;
            }
        }
        if (this.arena.game == 1) {

            this.spendTime=spendTime+1;

            Player p = this.arena.gamePlayer;
            p.sendPopup(">> §a§lElapsed time:§c" + spendTime +"s  §eYour point:§6"+this.arena.rank);

            if (this.arena.finish) {
                p.sendMessage("§f=======================");
                p.sendMessage(">>  §a完成游戏所用时间: §6§l"+spendTime);
                p.sendMessage(">>  §f你的得分: §6§l" +this.arena.rank);
                p.sendMessage("§f=======================");

                this.arena.stopGame();
                this.spendTime = 0;
            }
            if (this.arena.gamePlayer == null) {
                this.arena.stopGame();
                this.spendTime = 0;
            }
        }
    }

}