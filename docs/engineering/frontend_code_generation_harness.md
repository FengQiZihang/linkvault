# 前端代码生成约束规范 (Harness)

本规范（Harness）是为 AI（如 Gemini, Claude, Antigravity）在推进 **LinkVault** 前端工程开发时定制的**硬性约束**。其核心理念是：**不干预 AI 的业务逻辑实现，但必须在“全局样式”、“组件设计”、“uview-plus 使用”以及“代码工程化分层”上进行强力约束。** 

通过牢固的工程底座与高标准的视觉规范，即使业务代码由 AI 快速堆砌，整体项目依然能保持极高的可维护性、高质感的界面表现以及极低的重构成本。

---

## 1. 视觉系统重构：去除“一眼 AI 紫”

原交互原型中高饱和度的紫色（`#7c6fcd`）具有明显的 AI 生成感。为了提升 LinkVault 的 MVP 质感，我们弃用霓虹紫，提供以下三套 **高级、收敛、质感极佳** 的深色科技风调色盘。

AI 在生成样式时，**必须且仅允许** 从中任选一套（推荐**方案 A**），并将所有色值统一注入到 `uni.scss` 或 CSS 全局变量中：

### 推荐方案 A：暗岩与琥珀（Dark Rock & Warm Amber）—— 典雅工业质感
*   **背景色 (Background)**：`#0e0e11`（深石墨黑）
*   **卡片/容器色 (Surface)**：`#18181c`（暗岩灰）
*   **前景色/输入框 (Surface 2)**：`#222227`（中岩灰）—— 全局所有输入控件（`<u-input>`、`<u-textarea>`、`<u-search>` 等）背景色必须统一使用此色。
*   **主色调 (Accent - Primary)**：`#f59e0b` 或 `#d97706`（暖琥珀金）—— 优雅低调，告别 AI 感
*   **辅助色 (Accent2 - Secondary)**：`#fbbf24`（柔和黄）
*   **边框色 (Border)**：`#2e2e35` —— 全局所有输入控件的边框颜色必须统一使用此色，输入后文字颜色统一使用主文字色 `#e8e8f0`。

### 方案 B：北欧静谧绿（Mineral Green & Navy）—— 极致理性质感
*   **背景色 (Background)**：`#0b0f14`（极地墨蓝）
*   **卡片/容器色 (Surface)**：`#141b25`（矿石灰蓝）
*   **前景色/输入框 (Surface 2)**：`#1e293b`
*   **主色调 (Accent - Primary)**：`#10b981`（静谧翡翠绿）
*   **辅助色 (Accent2 - Secondary)**：`#34d399`
*   **边框色 (Border)**：`#243242`

### 方案 C：钛金灰与冰蓝（Titanium & Ice Blue）—— 极客冷峻风格
*   **背景色 (Background)**：`#0c0d0e`（纯黑）
*   **卡片/容器色 (Surface)**：`#16181a`（深空灰）
*   **前景色/输入框 (Surface 2)**：`#232629`
*   **主色调 (Accent - Primary)**：`#5b8bf7`（冰川蓝）
*   **辅助色 (Accent2 - Secondary)**：`#60a5fa`
*   **边框色 (Border)**：`#2d3135`

---

## 2. 全局样式工程化约束（Style Harness）

为了防止 AI 随手在代码中写死魔法值（如内联样式 `style="color: #7c6fcd"` 或自定义 class），必须强制执行以下规则：

1.  **零魔法色值 (Zero Hardcoded Colors)**：
    *   在 Vue 组件的 `<style>` 或内联样式中，**绝对不允许**出现具体的十六进制（`#fff`除外）、`rgb` 或 `hsl` 颜色代码。
    *   所有的颜色必须引用全局变量，例如：
        *   CSS 变量：`var(--color-bg)`，`var(--color-accent)`
        *   SCSS 变量：`$uv-primary`，`$theme-accent`
2.  **空间步长约束 (Spacing Scale)**：
    *   页面内元素的 `padding`、`margin`、`gap`、`height` 必须遵循 **8px 步长系统**（`8px`, `16px`, `24px`, `32px`）或 **4px 微调**（`4px`, `12px`）。
    *   严禁出现诸如 `margin-top: 13px;`，`padding: 9px;` 这样无序的数字。
3.  **圆角归一化**：
    *   全局卡片与按钮圆角统一设定，不允许组件内随意定义：
        *   大容器/卡片：`16px`（或全局变量 `--radius-lg`）
        *   按钮/输入框：`8px`（或全局变量 `--radius-md`）
        *   标签/小气泡：`20px`（或全局变量 `--radius-full`）

---

## 3. uview-plus 组件库调用规范（UI Library Harness）

项目基于 `uview-plus`。AI 在生成页面时，必须遵循以下“组件库优先”准则，防止 AI 使用纯 HTML 标签手写替代：

1.  **原生组件替代律**：
    *   **按钮**：必须使用 `<u-button>`，严禁手写 `<button>` 或 `<view class="btn">`。
    *   **输入框**：必须使用 `<u-input>` 或 `<u-textarea>`，严禁手写 `<input>`。
    *   **标签**：必须使用 `<u-tag>`，严禁手写 `<view class="tag-chip">`。
    *   **弹窗/抽屉**：必须使用 `<u-popup>`，结合 uview-plus 内置属性来实现动画和遮罩，严禁自己手写 `.modal-overlay` 的 JS 显示/隐藏逻辑。
    *   **搜索栏**：必须使用 `<u-search>`，禁止手写搜索框结构。
2.  **样式定制律**：
    *   当需要定制 uview-plus 组件的样式时，AI 应该**优先使用**组件库提供的 Props（如 `color`, `customStyle`, `size`）。
    *   如果必须在 `<style>` 中强改 uview-plus 样式，**必须**使用 `:deep()` 伪类，并做好注释说明，禁止大范围无脑覆写。

---

## 4. 组件化与工程化分层约束（Architecture Harness）

虽然 AI 写的业务逻辑代码可以“脏乱差”，但通过**物理隔离**，可以避免其污染核心的 UI 组件：

```
src/
├── components/          # 1. 基础 UI 组件（AI 必须保持 stateless）
│   ├── BookCard.vue     #    - 纯展示，仅通过 props 接收数据
│   ├── TagChip.vue      #    - 纯 UI，无业务 API 请求
│   └── AvatarPicker.vue #    - 不允许直接修改 Pinia/Global State，只抛出 event
├── pages/               # 2. 页面/业务层（AI 可以放飞自我写业务逻辑）
│   ├── index/index.vue  #    - 处理页面生命周期、API 请求、全局状态变更
│   └── detail/detail.vue#    - 组织基础 UI 组件，传递 props 并监听 events
└── store/               # 3. 数据与状态层（定义基础 State，AI 仅作调用）
```

### AI 代码生成的“三不准”底线：
1.  **不准在 `components/` 下的组件中发起网络请求 (`uni.request` / `api`)**。所有组件的数据必须来自父级传入的 `props`。
2.  **不准在展示组件中直接修改 Vuex 或 Pinia 的全局状态**。组件必须通过 `emits` 向上发送事件（如 `@delete`，`@click`），由 `pages/` 内的页面去修改状态。
3.  **不准在多个页面复制粘贴相同的 UI 段落**。一旦某段 UI 在原型中出现超过 2 次（如 LinkCard、TagChip），AI 必须将其提取到 `components/` 中。

---

## 5. AI 执行命令规范 (Prompts to AI)

为了让 AI 完美消化上述约束，在后续生成代码时，用户可以直接在 prompt 首部附带以下约束声明：

> **[Harness 约束指令]**
> * 严格遵循 `docs/engineering/frontend_code_generation_harness.md`。
> * 配色使用“推荐方案 A（暗岩与琥珀）”。
> * 必须使用 `uview-plus` 的原生组件代替 HTML。
> * 严禁内联写死具体 Hex 颜色，必须使用 SCSS 设计变量。
> * 核心逻辑由 Page 调度，UI 展示由 stateless 的 components 承担。
