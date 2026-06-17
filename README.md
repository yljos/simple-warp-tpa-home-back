<details>
<summary>中文</summary>

# Simple Warp TPA Home

一个功能简单的 **Fabric** 模组，提供 **传送请求（TPA）**、**家（Home）**、**公共传送点（Warp）** 三大功能。


---

## 功能总览

| 分类 | 命令 | 权限 | 说明 |
|------|------|------|------|
| **TPA** | `/tpa <玩家>` | 所有人 | 向目标玩家发送传送请求 |
| | `/tpay` | 所有人 | 接受传送请求 |
| | `/tpan` | 所有人 | 拒绝传送请求 |
| **Home** | `/sethome <名称>` | 所有人 | 在当前位置设置家（支持中文名） |
| | `/home <名称>` | 所有人 | 传送到指定的家 |
| | `/homes` | 所有人 | 列出所有家（可点击传送） |
| | `/delhome <名称>` | 所有人 | 删除一个家 |
| **Warp** | `/setwarp <名称>` | **OP 专用** | 设置全局传送点（支持中文名） |
| | `/warp <名称>` | 所有人 | 传送到指定传送点 |
| | `/warps` | 所有人 | 列出所有传送点（可点击传送） |
| | `/delwarp <名称>` | **OP 专用** | 删除传送点 |
| **配置** | `/swthconfig maxHomes <数量>` | **OP 专用** | 修改每名玩家最大家的数量（默认5个） |
| | `/swthconfig maxWarps <数量>` | **OP 专用** | 修改 Warp 最大数量（默认5个） |
| | `/swthconfig reload` | **OP 专用** | 从磁盘重新加载配置 |
| | `/swthconfig save` | **OP 专用** | 手动保存配置到磁盘 |

---

## 功能详情

### TPA 传送请求

玩家 A 执行 `/tpa 玩家B` 后：
- 玩家 A 收到："**[玩家B] 的传送请求已发送，等待回应...**"
- 玩家 B 收到："**[玩家A] 请求传送到你的位置 [接受] [拒绝]**"
- **接受 / 拒绝** 按钮可在聊天栏直接点击执行
- 请求超时时间：**60 秒**
- 支持跨维度传送

### Home 家系统

- 每位玩家默认最多 **5 个家**（可通过 `/swthconfig` 修改）
- 家名称支持中文（如 `/sethome 工业区`）
- 家名相同自动更新坐标（不增加数量）

### Warp 全局传送点

- Warp 是管理员设置的全局公共传送点，所有玩家可用
- `/warps` 列出所有公共传送点，每条可点击传送

---

## 配置文件

所有数据保存在 `saves/<存档>/data/simple-warp-tpa-home/data.json`，

```json
{
  "maxHomes": 5,
  "maxWarps": 5,
  "homes": {
    "玩家UUID": [
      { "name": "home1", "world": "minecraft:overworld", "x": 100, "y": 64, "z": 200, "yaw": 0, "pitch": 0 }
    ]
  },
  "warps": {
    "spawn": { "world": "minecraft:overworld", "x": 0, "y": 70, "z": 0, "yaw": 0, "pitch": 0 }
  }
}
```

- **修改自动保存**：每次 使用`sethome``delhome``setwarp``delwarp``swthconfig`自动写入文件
- **服务器关闭自动保存**
- **服务器启动自动加载**

---

## 多语言支持

内置 **简体中文** 和 **英文** 语言文件。

- 客户装有 mod → 根据客户端语言显示翻译
- 客户端未装 mod → 显示中文

欢迎提交添加更多语言。

---

## 许可证

CC0-1.0 — 自由使用、修改、分享。


</details>


# Simple Warp TPA Home
A lightweight Fabric mod featuring three core functions: TPA teleport requests, Player Homes, and Public Warp Points.

### Feature Overview
| Category | Command | Permission | Description |
|------|------|------|------|
| **TPA** | `/tpa <player>` | All Players | Send a teleport request to the target player |
| | `/tpay` | All Players | Accept an incoming teleport request |
| | `/tpan` | All Players | Decline an incoming teleport request |
| **Home** | `/sethome <name>` | All Players | 	Set a home at your current position |
| | `/home <name>` | All Players | Teleport to the specified home |
| | `/homes` | All Players | List all your homes (clickable for instant teleport) |
| | `/delhome <name>` | All Players | 	Delete a specific home |
| **Warp** | `/setwarp <name>` | **OP Only** | Create a global public warp point  |
| | `/warp <name>` | All Players | 	Teleport to the specified public warp |
| | `/warps` | All Players | List all public warps (clickable for instant teleport) |
| | `/delwarp <name>` | **OP Only** | Delete a public warp point |
| **Config** | `/swthconfig maxHomes <amount>` | **OP Only** | Adjust the maximum number of homes per player (default: 5) |
| | `/swthconfig maxWarps <amount>` | **OP Only** | Adjust the global maximum number of warps (default: 5) |
| | `/swthconfig reload` | **OP Only** | Reload configuration data from disk |
| | `/swthconfig save` | **OP Only** | Manually save all configuration to disk |

## Feature Details
### TPA Teleport Request System
#### When Player A runs /tpa PlayerB:
- Player A receives message: "Teleport request sent to [PlayerB], waiting for response..."
- Player B receives message: "[PlayerA] requests to teleport to your location [Accept] [Deny]"
- The [Accept] and [Deny] buttons are clickable directly in chat
- Request timeout duration: 60 seconds
- Cross-dimension teleportation supported
## Player Home System
- Each player has a default limit of 5 homes (adjustable via /swthconfig)
- Home names support Chinese characters (e.g. /sethome Industrial Zone)
- Re-setting a home with an identical name overwrites coordinates without consuming an extra home slot
## Public Warp Points
- Warps are global public teleport locations created exclusively by server OPs, accessible to all players
- Run /warps to view all public warps; each entry is clickable for one-click teleport
---
## Configuration File
All data is stored uniformly in saves/save/data/simple-warp-tpa-home/data.json:

```
{
  "maxHomes": 5,
  "maxWarps": 5,
  "homes": {
    "PlayerUUID": [
      { "name": "home1", "world": "minecraft:overworld", "x": 100, "y": 64, "z": 200, "yaw": 0, "pitch": 0 }
    ]
  },
  "warps": {
    "spawn": { "world": "minecraft:overworld", "x": 0, "y": 70, "z": 0, "yaw": 0, "pitch": 0 }
  }
}
```

- Auto-save on edits: Changes made via /sethome, /delhome, /setwarp, /delwarp and /swthconfig are automatically written to the file.
- Auto-save on server shutdown
- Auto-load all data on server startup

# Multilingual Support
#### Built-in language files for Simplified Chinese and English.
- If the client has this mod installed: text displays according to the client's game language setting
- If the client does not have this mod installed: text defaults to Simplified Chinese
- Contributions for additional translations are welcome.




