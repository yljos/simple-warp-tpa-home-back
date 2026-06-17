package zy.swth.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * 全局配置管理器
 * 统一管理模组设置和所有传送点数据（家、Warp），使用 JSON 文件持久化。
 * <p>
 * 文件位置：config/simple-warp-tpa-home/data.json
 * - 设置区：maxHomes（每玩家最大家的数量）、maxWarps（Warp 最大数量）
 * - 数据区：homes（按玩家 UUID 索引）、warps（按名称索引，供后续使用）
 */
public class ModConfig {

    // ---------- 文件路径 ----------

    private static final Path CONFIG_DIR = FabricLoader.getInstance()
            .getConfigDir().resolve("simple-warp-tpa-home");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("data.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // ---------- 单例 ----------

    private static ModConfig instance;

    public static ModConfig getInstance() {
        if (instance == null) {
            instance = new ModConfig();
        }
        return instance;
    }

    // ---------- 设置 ----------

    /** 每名玩家最多可设置的家数量，默认 5 */
    private int maxHomes = 5;

    /** Warp 最大数量（预留，供后续 Warp 功能使用），默认 20 */
    private int maxWarps = 20;

    // ---------- 数据 ----------

    /** 家的数据：key = 玩家 UUID 字符串，value = 该玩家的家列表 */
    private Map<String, List<HomeEntry>> homes = new HashMap<>();

    /** Warp 数据（预留，供后续 Warp 功能使用） */
    private Map<String, WarpEntry> warps = new HashMap<>();

    // ---------- 内部数据类 ----------

    /**
     * 一个家的数据条目

     * param name  家名称
     * param world 维度标识符，如 "Minecraft:overworld"
     * param x, y, z 坐标
     * param yaw, pitch 视角朝向
     */
    public static class HomeEntry {
        public String name;
        public String world;
        public double x, y, z;
        public float yaw, pitch;

        /** Gson 反序列化用 */
        public HomeEntry() {}

        public HomeEntry(String name, String world, double x, double y, double z, float yaw, float pitch) {
            this.name = name;
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    /**
     * Warp 传送点数据（预留，供后续使用）
     */
    public static class WarpEntry {
        public String world;
        public double x, y, z;
        public float yaw, pitch;

        public WarpEntry() {}

        public WarpEntry(String world, double x, double y, double z, float yaw, float pitch) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    // ---------- 生命周期 ----------

    private static final Logger LOGGER = LoggerFactory.getLogger("simple-warp-tpa-home-config");

    /** 禁止外部直接创建，使用 getInstance() */
    private ModConfig() {}

    /**
     * 从磁盘加载配置文件。如果文件不存在，则使用默认值并新建文件。
     */
    public static void load() {
        ModConfig config = getInstance();

        if (!Files.exists(CONFIG_FILE)) {
            // 文件不存在，保存默认配置
            LOGGER.info("配置文件不存在，将使用默认设置创建新文件");
            config.save();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE)) {
            // 读取现有配置
            JsonData data = GSON.fromJson(reader, JsonData.class);
            if (data != null) {
                config.maxHomes = data.maxHomes;
                config.maxWarps = data.maxWarps;
                config.homes = data.homes != null ? data.homes : new HashMap<>();
                config.warps = data.warps != null ? data.warps : new HashMap<>();
                LOGGER.info("配置文件已加载: " + CONFIG_FILE);
            }
        } catch (Exception e) {
            LOGGER.error("加载配置文件失败，将使用默认设置: " + e.getMessage());
        }
    }

    /**
     * 将当前配置保存到磁盘。
     */
    public void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_FILE)) {
                JsonData data = new JsonData();
                data.maxHomes = this.maxHomes;
                data.maxWarps = this.maxWarps;
                data.homes = this.homes;
                data.warps = this.warps;
                GSON.toJson(data, writer);
                writer.flush();
            }
        } catch (Exception e) {
            LOGGER.error("保存配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 仅用于 JSON 序列化/反序列化的中间结构
     */
    private static class JsonData {
        int maxHomes = 5;
        int maxWarps = 20;
        Map<String, List<HomeEntry>> homes = new HashMap<>();
        Map<String, WarpEntry> warps = new HashMap<>();
    }

    // ---------- Homes 相关方法 ----------

    public int getMaxHomes() {
        return maxHomes;
    }

    public void setMaxHomes(int maxHomes) {
        this.maxHomes = maxHomes;
        save();
    }

    /**
     * 获取某玩家的所有家（不可修改的列表）
     */
    public List<HomeEntry> getHomes(UUID playerUuid) {
        List<HomeEntry> list = homes.get(playerUuid.toString());
        return list != null ? Collections.unmodifiableList(list) : List.of();
    }

    /**
     * 获取某玩家的指定家
     */
    public HomeEntry getHome(UUID playerUuid, String name) {
        List<HomeEntry> list = homes.get(playerUuid.toString());
        if (list == null) return null;
        for (HomeEntry h : list) {
            if (h.name.equals(name)) return h;
        }
        return null;
    }

    /**
     * 检查某玩家是否达到家的数量上限
     */
    public boolean isAtMaxHomes(UUID playerUuid) {
        List<HomeEntry> list = homes.get(playerUuid.toString());
        return list != null && list.size() >= maxHomes;
    }

    /**
     * 检查某玩家是否已存在同名家
     */
    public boolean homeExists(UUID playerUuid, String name) {
        List<HomeEntry> list = homes.get(playerUuid.toString());
        if (list == null) return false;
        return list.stream().anyMatch(h -> h.name.equals(name));
    }

    /**
     * 获取某玩家当前拥有的家数量
     */
    public int getHomeCount(UUID playerUuid) {
        List<HomeEntry> list = homes.get(playerUuid.toString());
        return list != null ? list.size() : 0;
    }

    /**
     * 设置/更新一个家
     * <p>
     * 如果同名家已存在，更新其坐标；否则新增。
     * 需要在调用前通过 {@link #isAtMaxHomes(UUID)} 检查上限。
     */
    public void setHome(UUID playerUuid, HomeEntry entry) {
        String key = playerUuid.toString();
        List<HomeEntry> list = homes.computeIfAbsent(key, k -> new ArrayList<>());

        // 如果同名家已存在，更新之
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).name.equals(entry.name)) {
                list.set(i, entry);
                save();
                return;
            }
        }

        // 新增
        list.add(entry);
        save();
    }

    /**
     * 删除某玩家的一个家
     *
     * @return true 如果删除成功
     */
    public boolean removeHome(UUID playerUuid, String name) {
        String key = playerUuid.toString();
        List<HomeEntry> list = homes.get(key);
        if (list == null) return false;

        boolean removed = list.removeIf(h -> h.name.equals(name));
        if (removed) {
            if (list.isEmpty()) {
                homes.remove(key);
            }
            save();
        }
        return removed;
    }

    // ---------- Warps 相关方法（预留） ----------

    public int getMaxWarps() {
        return maxWarps;
    }

    public void setMaxWarps(int maxWarps) {
        this.maxWarps = maxWarps;
        save();
    }

    public Map<String, WarpEntry> getWarps() {
        return Collections.unmodifiableMap(warps);
    }

    public WarpEntry getWarp(String name) {
        return warps.get(name);
    }

    public void setWarp(String name, WarpEntry entry) {
        warps.put(name, entry);
        save();
    }

    public boolean removeWarp(String name) {
        if (warps.containsKey(name)) {
            warps.remove(name);
            save();
            return true;
        }
        return false;
    }
}
