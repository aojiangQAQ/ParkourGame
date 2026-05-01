# ParkourGame - 跑酷小游戏插件

![Minecraft](https://img.shields.io/badge/Minecraft-1.16%2B-green)
![Spigot/Paper](https://img.shields.io/badge/Spigot%2FPaper-Compatible-blue)
![License](https://img.shields.io/badge/license-MIT-blue)

> **ParkourGame** 是一个为 Minecraft 服务器设计的跑酷小游戏插件。支持创建多条跑酷路线，含检查点系统、实时计时和自动背包管理，为玩家提供流畅的跑酷体验。

---

## ✨ 功能亮点

- **多路线管理**  
  支持创建多条独立跑酷路线，每条路线拥有独立的起点、终点和多个检查点

- **实时计时系统**  
  通过 Action Bar 实时显示跑酷用时，精确到毫秒

- **检查点机制**  
  踩踏轻质测重压力板自动触发，到达检查点时播放音效和粒子特效

- **背包管理系统**  
  跑酷开始时自动保存玩家背包，跑酷结束后自动恢复  
  跑酷期间提供羽毛（返回检查点）和屏障（退出跑酷）快捷工具

- **安全退出保护**  
  玩家退出服务器时自动清理跑酷状态，防止数据异常

---

## 📂 目录结构

```
ParkourGame/
├── build.gradle
├── settings.gradle
├── README.md
├── LICENSE
├── .gitignore
└── src/
    └── main/
        ├── java/
        │   └── com/shuguangteam/parkourgame/...
        └── resources/
            └── plugin.yml
```

---

## 🛠 本地构建

确保使用 **JDK 8+**：

```bash
# 克隆仓库
git clone https://github.com/aojiangQAQ/ParkourGame.git
cd ParkourGame

# Gradle 打包
./gradlew build

# 生成 build/libs/ParkourGame-1.0.jar
```

---

## 🚀 安装与配置

1. 将 `ParkourGame-1.0.jar` 复制到服务器 `plugins/` 目录
2. 启动或重载服务器，插件会自动生成配置文件
3. 使用指令创建跑酷路线（详见下方指令列表）

---

## 📝 指令 & 权限

| 指令 | 别名 | 说明 | 权限 |
|------|------|------|----------|
| `/pk create <名称> <s\|e\|cp> [编号]` | `/parkour` | 创建跑酷路线（起点/终点/检查点） | OP |
| `/pk list` | — | 列出所有跑酷路线 | OP |
| `/pk tp <名称>` | — | 传送到指定路线起点 | OP |

> 跑酷路线通过准星对准**轻质测重压力板**并执行指令来设置。

玩家无须任何权限即可参与跑酷。

---

## 🔧 开发环境

- **Java:** 8+
- **Build:** Gradle 7.x+
- **API:** Spigot / Paper API 1.16+
- **测试服务端:** Spigot / Paper 1.16+

---

## 🤝 贡献

欢迎 Issue / PR！

1. Fork 本仓库
2. 创建新分支: `git checkout -b feature/awesome`
3. 提交更改: `git commit -m "Add awesome feature"`
4. 推送分支: `git push origin feature/awesome`
5. 发起 Pull Request

---

## ⚖️ License

ParkourGame 使用 **MIT License**，详见 [LICENSE](LICENSE)。
