# EnchantmentLimit-Slimefun

![Version](https://img.shields.io/github/v/release/xmk2333/EnchantmentLimit-Slimefun?include_prereleases)
![License](https://img.shields.io/github/license/xmk2333/EnchantmentLimit-Slimefun)

## 📝 介绍

EnchantmentLimit-Slimefun 是一个为 Slimefun4 开发的附属插件，用于限制自动祛魔机中的附魔等级。它几乎重写了原版自动祛魔机的逻辑，使得自动祛魔机可以更灵活地控制祛魔后的等级。主要用于防止玩家使用附魔书进行过度附魔。

## ✨ 特性

- 🛠️ 完全兼容 Slimefun4 的自动祛魔机
- 🎮 支持两种工作模式：
  - 严格模式：放入超过限制的附魔物品时失效，有点硬核因为直接没反应不推荐
  - 转换模式：允许放入，但会将超过限制的附魔等级降低到允许的最高等级
- ⚙️ 可配置的附魔等级限制
- 💬 可自定义的消息系统

## 📥 安装

1. 下载最新版本的插件
2. 将插件放入服务器的 `plugins` 文件夹
3. 重启服务器
4. 编辑 `plugins/EnchantmentLimit/config.yml` 进行配置

## ⚙️ 配置

```yaml
# 功能设置
settings:
  # 祛魔机处理模式
  # strict: 严格模式 - 禁止放入超过限制的附魔物品
  # convert: 转换模式 - 自动将超限附魔降级到允许的最高等级
  mode: "convert"

# 附魔限制示例
enchantments:
  minecraft:sharpness: 5
  minecraft:efficiency: 5
  minecraft:fortune: 3
  # 更多附魔限制...
```

## 📌 命令

- `/sfenchantmentlimit reload` - 重载配置文件
- 需要权限: `enchantmentlimit.admin`

## 🔧 依赖

- Slimefun4
- Paper/Spigot 1.16+

## 加入我们的社区 💬

如果你有任何问题或需要支持，请随时加入我们的社区！

[![加入我们的QQ群](https://img.shields.io/badge/QQGroup-528651839-blue)](https://jq.qq.com/?_wv=1027&k=528651839)


## 📜 许可

本项目采用 [MIT](LICENSE) 许可证 