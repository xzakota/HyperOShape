**文档更新日期：2025-03-06**

# 定义一个新主题
新建 `config.json` 文件，填写主题配置项。
```JSON
{
    "template": "alpha",
    "id": {
        "slug": "example_theme",
        "name": "Example Theme"
    },
    "author": "xzakota",
    "description": "Example Theme",
    "preview": "preview.png",
    "version": {
        "versionName": "1.0",
        "versionCode": 1
    }
}
```
**配置项说明**
- `template`: 主题模板，当前仅有 `alpha` 模板
- `id`:
  - `slug`: 主题唯一标识符，不能以 `default` 开头
  - `name`: 主题名称
- `author`: 主题作者
- `description`: 主题说明
- `preview`: 预览图，仅支持 `png`、`jpg` 格式文件
- `version`:
  - `versionName`: 主题版本名
  - `versionCode`: 主题版本号

## 图标资源
可以在 `config.json` 同级目录下新建 `icons` 文件夹，并置入图标文件，当前支持的图标如下

| 文件名 | 说明 |
|:----:|:----:|
xiaomi_smart_hub | 融合设备中心
smart_home | 智能生活
tile_wordless_mode | 磁贴无字模式
tile_edit_mode | 磁贴编辑模式
power_menu | 电源菜单
settings | 设置
flashlight_off | 手电筒关
flashlight_on | 手电筒开
camera | 照相机
lsposed | LSPosed
sunlight_mode | 阳光模式
invisible_mode | 隐身模式
expand_notification | 原生通知样式展开按钮图标
collapse_notification | 原生通知样式折叠按钮图标

支持的图标文件格式: **`svg`**、`png`、`jpg`。

## 其他资源
可以在 `config.json` 同级目录下新建 `values` 文件夹；

**颜色资源**
`values` 目录下分别新建 `colors.json` 浅色模式颜色配置文件、`colors_night.json` 深色模式颜色配置文件，并填写颜色项，当前支持的颜色项如下

| 键 | 值类型 | 说明 |
|:----:|:----:|:----:|
home_widget_mi_bg_blend_blur   |   MiBlendBlur   |   桌面组件高级材质混色集合
settings_bg_effect | MiBgEffect | 设置设备页背景动效

- `MiBlendBlur`: JSONObject
  - `color`: JSONArray&lt;String>
  - `mode`: JSONArray&lt;Integer>
- `MiBgEffect`: JSONObject
  - `lightOffset`: Float
  - `saturateOffset`: Float
  - `point`: JSONArray&lt;Float>
  - `color`: JSONArray&lt;String>

# 压缩主题
经过以上配置，目录结构大致如下
```
ExampleThemeDir
├── config.json
├── icons
│   ├── camera.svg
│   ├── flashlight_off.svg
│   ├── flashlight_on.svg
├── preview.png
└── values
    ├── colors.json
    └── colors_night.json
```
选中 `ExampleThemeDir` 目录下的所有直接子文件/夹，压缩为一个 `.zip` 包即可。注意，不是压缩 `ExampleThemeDir` 本身。

# 导入使用
在 OShape 的资源主题管理界面中导入该主题，导入成功后单击预览图可切换到该主题并使用，最后重启对应作用域即可生效。

# 删除主题
长按非默认和非当前主题的预览图可删除主题。