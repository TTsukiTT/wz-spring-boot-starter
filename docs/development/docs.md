# 文档站点

项目文档基于 [VitePress](https://vitepress.dev/) 构建，源码位于 `docs/` 目录。

## 本地开发

```bash
cd docs
pnpm install
pnpm docs:dev
```

启动后访问 `http://localhost:5173` 预览文档。

## 搜索

文档站点内置**本地全文搜索**（基于 [minisearch](https://github.com/lucaong/minisearch/)），无需第三方服务：

- 点击导航栏 **搜索** 按钮，或使用快捷键 `Ctrl + K`（Mac：`Cmd + K`）
- 支持模糊匹配，可搜索标题与正文内容
- 构建时自动生成搜索索引（`pnpm docs:build`）

配置位于 `.vitepress/config.mts` 的 `themeConfig.search`。

## 构建

```bash
cd docs
pnpm docs:build
```

构建产物输出到 `docs/.vitepress/dist/`。

## 预览构建结果

```bash
cd docs
pnpm docs:preview
```

## 文档结构

```
docs/
├── .vitepress/
│   └── config.mts          # VitePress 配置（导航、侧边栏）
├── index.md                # 首页
├── guide/                  # 指南
│   ├── introduction.md     # 项目介绍
│   ├── quick-start.md      # 快速开始
│   ├── architecture.md     # 架构设计
│   └── configuration.md    # 配置说明
├── modules/                # 模块文档
│   ├── overview.md         # 模块概览
│   ├── common.md
│   ├── core.md
│   ├── web.md
│   └── ...
└── development/            # 开发文档
    ├── build.md            # 构建与发布
    ├── extend.md           # 扩展机制
    └── docs.md             # 本文档
```

## 编写规范

- 所有文档使用中文
- 代码示例需完整可运行
- 未实现的模块使用 VitePress 的 `::: warning` 提示块标注
- 新增模块文档后，在 `.vitepress/config.mts` 的 sidebar 中注册

## 部署

构建产物为静态 HTML，可部署到任意静态托管服务：

- GitHub Pages
- Nginx
- Vercel / Netlify

```bash
# 示例：部署到 GitHub Pages
pnpm docs:build
# 将 docs/.vitepress/dist/ 内容推送到 gh-pages 分支
```
