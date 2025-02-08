package nekonic;

import nekonic.utils.Create_GUI_Item;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicGraphPlugin extends JavaPlugin implements Listener {

    private Map<Player, BukkitTask> updateTasks = new HashMap<>();
    private List<Integer> memoryData = new ArrayList<>();
    private static final int DATA_POINTS = 7; // 7개의 데이터 포인트

    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage(
                Component.text("GraphPlugin has been enabled!", NamedTextColor.DARK_AQUA)
        );
    }

    @Override
    public void onDisable() {
        for (BukkitTask task : updateTasks.values()) {
            task.cancel();
        }
        getServer().getConsoleSender().sendMessage(
                Component.text("GraphPlugin has been disabled!", NamedTextColor.DARK_AQUA)
        );
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("graph")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(
                        Component.text("This command can only be run by a player.",NamedTextColor.RED)
                );
                return true;
            }

            Player player = (Player) sender;
            openGraphGUI(player);
            return true;
        }
        return false;
    }

    private void initializeMemoryData() {
        memoryData.clear();
        for (int i = 0; i < DATA_POINTS; i++) {
            memoryData.add(getCurrentMemoryUsage());
        }
    }

    private int getCurrentMemoryUsage() {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        return (int) ((totalMemory - freeMemory) / (1024 * 1024)); // MB 단위로 변환
    }

    private void updateMemoryData() {
        memoryData.removeFirst();
        memoryData.add(getCurrentMemoryUsage()); // 새로운 데이터 추가
        for (int i : memoryData){
            getServer().getConsoleSender().sendMessage(
                    Component.text(i, NamedTextColor.AQUA)
            );
        }
    }



    private void openGraphGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "§f궯샮");

        initializeMemoryData(); // 초기 데이터 설정
        Create_GUI_Item.initGUIItem();

        player.openInventory(inventory);
        // 인벤토리 업데이트 작업 시작
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.getOpenInventory().getTitle().equals("§f궯샮")) {
                    this.cancel(); // 인벤토리가 닫혔거나 다른 인벤토리가 열렸다면 작업 중지
                    updateTasks.remove(player);
                }updateGraph(player,inventory);
            }
        }.runTaskTimer(this, 0L, 20L); // 1초마다 실행

        updateTasks.put(player, task);

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("§f궯샮")) {
            Player player = (Player) event.getPlayer();
            BukkitTask task = updateTasks.remove(player);
            if (task != null) {
                task.cancel();
            }
        }
    }

    private void updateGraph(Player player, Inventory inventory) {
        if (inventory == null) return;
        updateMemoryData();

        getServer().getConsoleSender().sendMessage(
                Component.text("memoryData : ", NamedTextColor.RED)
        );

        int[] modelDataList = {
                100,
                101,
                102,
                103,
                104,
                105,
                106,
                107,
                108,
                109,
                110,
                111,
                112
        };

        int[][] initPos = {
                { 0,  1,  2,  3,  4,  5,  6},
                { 9, 10, 11, 12, 13, 14, 15},
                {18, 19, 20, 21, 22, 23, 24},
                {27, 28, 29, 30, 31, 32, 33},
                {36, 37, 38, 39, 40, 41, 42}
        };


        int[][] grid = drowGraph(memoryData.stream().mapToInt(Integer::intValue).toArray());
        boolean isIncrease = memoryData.getFirst()<memoryData.getLast();

        for (int i=0; i<5; i++) {
            for(int j=0; j<7; j++){
                int modleNum = 0;
                modleNum += grid[i*2][j*2]<<3;
                modleNum += grid[i*2][j*2+1]<<2;
                modleNum += grid[i*2+1][j*2]<<1;
                modleNum += grid[i*2+1][j*2+1];
                if(modleNum==0){
                    inventory.setItem(initPos[i][j],new ItemStack(Material.AIR));
                    continue;
                }
                ItemStack graphItem = Create_GUI_Item.createGUIItem(modelDataList[modleNum], isIncrease);
                inventory.setItem(initPos[i][j], graphItem);
            }
        }
    }

    public int[][] drowGraph(int[] data){


        int[] yData = new int[7];

        // 최소값과 최대값 계산
        int min = data[0];
        int max = data[0];
        for (int n : data) {
            if (n < min) min = n;
            if (n > max) max = n;
        }

        // 그리드 크기 설정
        int width = 14;
        int height = 10;

        // 그리드 초기화
        int[][] grid = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = 0;
            }
        }

        // x축 위치 계산 (데이터를 2칸마다 배치)
        int[] xPositions = new int[DATA_POINTS];
        for (int i = 0; i < DATA_POINTS; i++) {
            xPositions[i] = i * 2; // x 위치: 1, 3, 5, ..., 13
        }

        // 데이터 포인트를 그리드에 매핑
        for (int i = 0; i < DATA_POINTS; i++) {
            int n = data[i];
            // y 좌표 계산: (n - min) / (max - min) * 9 후 반올림(뱅커스라운딩 사용)
            int y = (int) Math.round(((double)(n - min) / (max - min)) * (height - 1));
            int x = xPositions[i];
            int yPos = height - 1 - y; // 그리드의 y 좌표는 위에서 아래로 증가하므로 반전
            yData[i] = yPos;

            grid[yPos][x] = 1;
        }

        // 점과 점 연결
        for (int i=1; i<7; i++){
            int x = xPositions[i-1]+1;
            int y1 = yData[i-1];
            int y2 = yData[i];
            if(y1>y2){
                int tmp = y1;
                y1 = y2;
                y2 = tmp;
            }
            if(y2-y1<2)grid[y1][x]=1;
            for (int j=y1+1; j<y2; j++){
                grid[j][x]=1;
            }
        }
        return grid;
    }
}
