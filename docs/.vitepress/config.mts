import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'WZ Spring Boot Starter',
  description: '微筑 Spring Boot Starter 套件 — 快速创建项目，封装通用能力',
  lang: 'zh-CN',

  themeConfig: {
    nav: [
      { text: '首页', link: '/' },
      { text: '指南', link: '/guide/introduction' },
      { text: '模块', link: '/modules/overview' },
      { text: '开发', link: '/development/build' },
    ],

    sidebar: {
      '/guide/': [
        {
          text: '指南',
          items: [
            { text: '项目介绍', link: '/guide/introduction' },
            { text: '快速开始', link: '/guide/quick-start' },
            { text: '架构设计', link: '/guide/architecture' },
            { text: '配置说明', link: '/guide/configuration' },
          ],
        },
      ],
      '/modules/': [
        {
          text: '模块',
          items: [
            { text: '模块概览', link: '/modules/overview' },
            { text: 'wz-common', link: '/modules/common' },
            { text: 'wz-spring-boot-starter-core', link: '/modules/core' },
            { text: 'wz-spring-boot-starter-web', link: '/modules/web' },
            { text: 'wz-spring-boot-starter-mybatis', link: '/modules/mybatis' },
            { text: 'wz-spring-boot-starter-redis', link: '/modules/redis' },
            { text: 'wz-spring-boot-starter-security', link: '/modules/security' },
            { text: 'wz-spring-boot-starter-log', link: '/modules/log' },
            { text: 'wz-spring-boot-starter-oss', link: '/modules/oss' },
            { text: 'wz-spring-boot-starter-mq', link: '/modules/mq' },
            { text: 'wz-spring-boot-starter-job', link: '/modules/job' },
            { text: 'wz-spring-boot-starter-monitor', link: '/modules/monitor' },
            { text: 'wz-spring-boot-starter-test', link: '/modules/test' },
          ],
        },
      ],
      '/development/': [
        {
          text: '开发',
          items: [
            { text: '构建与发布', link: '/development/build' },
            { text: '扩展机制', link: '/development/extend' },
            { text: '文档站点', link: '/development/docs' },
          ],
        },
      ],
    },

    outline: { level: [2, 3] },

    docFooter: {
      prev: '上一页',
      next: '下一页',
    },

    darkModeSwitchLabel: '主题',
    sidebarMenuLabel: '菜单',
    returnToTopLabel: '回到顶部',

    search: {
      provider: 'local',
      options: {
        locales: {
          root: {
            translations: {
              button: {
                buttonText: '搜索',
                buttonAriaLabel: '搜索文档',
              },
              modal: {
                displayDetails: '显示详细列表',
                resetButtonTitle: '重置搜索',
                backButtonTitle: '返回',
                noResultsText: '未找到相关结果',
                footer: {
                  selectText: '选择',
                  selectKeyAriaLabel: 'Enter 键',
                  navigateText: '切换',
                  navigateUpKeyAriaLabel: '上箭头',
                  navigateDownKeyAriaLabel: '下箭头',
                  closeText: '关闭',
                  closeKeyAriaLabel: 'Esc 键',
                },
              },
            },
          },
        },
      },
    },
  },
})
