# ParkourGame

Minecraft 跑酷小游戏插件，可设置多条路线的起点、终点和检查点，在 Action Bar 显示用时。

作者：**aojiangQAQ（鳌江）**。

## 环境

- 编译依赖：Spigot API `1.16.5-R0.1-SNAPSHOT`。
- 插件声明的 API 版本：`1.16`。
- Java 源码和字节码目标：Java 8。
- 构建工具：仓库内的 Gradle Wrapper `8.7`，建议使用 JDK 17 构建。

## 构建与安装

```powershell
git clone https://github.com/aojiangQAQ/ParkourGame.git
cd ParkourGame
.\gradlew.bat build
```

Linux/macOS 使用 `./gradlew build`。产物为 `build/libs/ParkourGame-1.0.jar`。

将 JAR 放入服务端 `plugins/` 目录后重启服务器。当前版本不生成配置文件，路线和玩家状态只保存在内存中，重启后需重新设置路线。

## 命令

`/parkour` 是 `/pk` 的别名。全部命令只能由玩家执行。

| 命令 | 说明 |
|---|---|
| `/pk create <名称> s` | 设置路线起点 |
| `/pk create <名称> e` | 设置路线终点，需先设置起点 |
| `/pk create <名称> cp <编号>` | 添加检查点，需先设置起点与终点，编号为正整数 |
| `/pk list` | 列出路线 |
| `/pk tp <名称>` | 传送到路线起点 |
| `/pk help` | 显示命令帮助 |
| `/pk reload` | 保留命令；当前只返回重载提示，不读取配置或保存路线 |

设置跑酷点时，准星需对准 5 格内的轻质测重压力板。

当前实现没有命令权限检查，普通玩家也能创建路线和使用上述命令。公开服务器使用前应通过外部权限或命令管理机制限制管理操作。

## 跑酷流程

1. 触发起点压力板后开始计时，并记录原背包内容。
2. 触发检查点后记录位置，播放音效和粒子效果。
3. 到达终点后显示总用时、恢复背包，并传送到起点附近。
4. 玩家退出服务器时结束跑酷并恢复背包。

Action Bar 每秒更新一次，完成时间以毫秒差值计算后显示为秒。开始时会放入羽毛和屏障，但当前没有实现这两个物品的返回检查点或主动退出操作，也没有成绩持久化。

## 源码

- `src/main/java/com/sgly/aojiang/parkourgame/ParkourGame.java`：命令、事件、路线和计时状态。
- `src/main/resources/plugin.yml`：插件入口与命令别名。
- `build.gradle`：编译依赖、Java 目标与资源处理。

## 反馈与许可

问题和改进建议请提交到 [Issues](https://github.com/aojiangQAQ/ParkourGame/issues)。

采用 [MIT License](LICENSE)。
