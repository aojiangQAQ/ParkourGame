# ParkourGame - Minecraft 跑酷小游戏插件

一个基于 Spigot API 的 Minecraft 跑酷小游戏插件。支持创建多条跑酷路线，含检查点系统、实时计时和背包管理。

## 功能特性

- 创建多条跑酷路线，每条路线含起点、终点和多个检查点
- 实时计时器（Action Bar 显示）
- 踩压力板触发起点/终点/检查点
- 检查点到达时播放音效和粒子特效
- 跑酷期间自动保存并替换玩家背包（羽毛=返回检查点，屏障=退出）
- 玩家退出时自动清理跑酷状态

## 指令

| 指令 | 别名 | 说明 | 权限 |
|------|------|------|------|
| `/pk create <name> <s\|e\|cp> [number]` | `/parkour` | 创建跑酷路线（起点/终点/检查点） | OP |
| `/pk list` | | 列出所有跑酷路线 | OP |
| `/pk tp <name>` | | 传送到指定路线起点 | OP |

> 跑酷路线通过准星对准**轻质测重压力板**并执行指令来设置。

## 兼容性

- Minecraft 1.16+
- Spigot / Paper

## 构建

```bash
./gradlew build
```

构建产物位于 `build/libs/`。

## 安装

1. 将构建好的 jar 文件放入服务器 `plugins/` 目录
2. 重启或 reload 服务器

## 作者

SGly (aojiangQAQ)

## 许可证

[MIT License](LICENSE)
