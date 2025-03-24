import { defineConfig } from "vitepress";

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "MMOItems 中文 Wiki",
  titleTemplate: ":title - MagicMC",
  base: "/",
  sitemap: {
    hostname: 'https://mmoitems.magicmc.top'
  },
  head: [
    [ "meta", { name: "theme-color", content: "#3c8772" }],
    [ "link", { rel: "icon", href: "/icon.png" }]
  ],
  outDir: "./dist",
  // srcDir: './src',
  description: "致力于为开发者带一个来更好社区",
  markdown: {
    async config(md) {},
  },
  lang: "zh-CN",
  lastUpdated: true,
  ignoreDeadLinks: true,
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: "开始", link: "/general/getting-start" },
      { text: "物品创建", link: "/item-creation/item-creation" },
      { text: "GitHub", link: "https://github.com/MagicMC-Dev/MMOItems-Zh" },
    ],
    sidebar: [
      {
        text: "开始",
        items: [
          { text: "开始", link: "/general/getting-start" },
          { text: "安装", link: "/general/install-plugin" },
          { text: "命令", link: "/general/command" },
          { text: "权限", link: "/general/permissions" },
          { text: "占位符", link: "/general/placeholders" },
          { text: "配置文件", link: "/general/config" },
        ],
      },
      {
        text: "物品创建",
        items: [
          { text: "物品创建", link: "/item-creation/item-creation" },
          { text: "物品类型", link: "/item-creation/item-type" },
          { text: "物品模板", link: "/item-creation/item-templates" },
          { text: "物品修饰符", link: "/item-creation/item-modifiers" },
          { text: "物品属性与设定", link: "/item-creation/item-stats-and-options", },
          { text: "物品复杂属性", link: "/item-creation/complex-stats" },
          { text: "物品描述", link: "/item-creation/lore-formats" },
          { text: "物品属性设置", link: "/item-creation/config-format-of-stats", },
        ],
      },
      {
        text: "主要功能",
        items: [
          { text: "伤害减免", link: "/main-feature/damage-mitigation" },
          { text: "元素伤害", link: "/main-feature/elemental-damage" },
          { text: "物品鉴定", link: "/main-feature/item-identification" },
          { text: "物品层", link: "/main-feature/item-tiers" },
          { text: "宝石", link: "/main-feature/gem-stones" },
          { text: "自定义耐久", link: "/main-feature/custom-durability" },
          { text: "自定义配方", link: "/main-feature/custom-recipes" },
          { text: "物品升级", link: "/main-feature/item-upgrading" },
          { text: "灵魂绑定", link: "/main-feature/soulbound" },
          { text: "掉落物飞溅", link: "/main-feature/lootsplosion" },
          { text: "技能", link: "/main-feature/abilities" },
        ],
      },
      {
        text: "合成站",
        items: [
          { text: "合成站", link: "/crafting-stations/crafting-stations" },
          { text: "合成站布局", link: "/crafting-stations/station-layouts" },
          { text: "升级配方", link: "/crafting-stations/upgrading-recipes" },
          { text: "配方条件", link: "/crafting-stations/recipe-conditions" },
          { text: "配方材料", link: "/crafting-stations/recipe-ingredients" },
          { text: "配方触发器", link: "/crafting-stations/recipe-triggers" },
        ],
      },
      {
        text: "自定义方块",
        items: [
          { text: "自定义方块", link: "/custom-blocks/custom-blocks" },
          {
            text: "自定义方块生成",
            link: "/custom-blocks/world-generation-templates",
          },
        ],
      },
      {
        text: "物品管理",
        items: [
          { text: "修订 ID 系统", link: "/item-management/revision-id-system" },
          { text: "物品升级", link: "/item-management/item-updater" },
          { text: "物品掉落", link: "/item-management/item-drop-tables" },
          { text: "获取物品", link: "/item-management/obtaining-an-item" },
        ],
      },
    ],
    footer: {
      message:
        'Released under the <a href="https://github.com/MagicMC-Dev/MMOItems-Zh/blob/Zh_cn/LICENSE">GPL-3.0 License</a>.',
      copyright:
        'Copyright © 2022-2024 <a href="https://github.com/MagicMC-Dev">INSide_734</a>',
    },
    editLink: {
      pattern:
        "https://github.com/MagicMC-Dev/MMOItems-Zh/edit/Zh_cn/docs/:path",
      text: "在 GitHub 上编辑此页面",
    },
    socialLinks: [{ icon: "github", link: "https://github.com/MagicMC-Dev" }],
    lastUpdatedText: "上次更新时间",
    docFooter: {
      prev: "上一节",
      next: "下一节",
    },
    search: {
      provider: "local",
      options: {
        locales: {
          root: {
            translations: {
              button: {
                buttonText: '搜索',
                buttonAriaLabel: '搜索'
              },
              modal: {
                displayDetails: '显示详细列表',
                resetButtonTitle: '重置搜索',
                backButtonTitle: '关闭搜索',
                noResultsText: '没有结果',
                footer: {
                  selectText: '选择',
                  selectKeyAriaLabel: '输入',
                  navigateText: '导航',
                  navigateUpKeyAriaLabel: '上箭头',
                  navigateDownKeyAriaLabel: '下箭头',
                  closeText: '关闭',
                  closeKeyAriaLabel: 'esc'
                }
              }
            }
          }
        }
      }
    },
    logo: "icon.png",
  },
});
