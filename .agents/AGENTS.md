# Project Rules for LinkVault

本文件包含 LinkVault 专用的 AI 开发规则与约束规范。在此工作区中运行的所有 AI 代理均须无条件遵守以下规则。

## 前端开发与代码生成约束 (Harness)

为了在放宽业务逻辑代码质量的同时，确保前端工程的整体可维护性、视觉质感和工程化规范，所有前端代码的生成必须严格遵循以下约束：

1. **统一设计系统 (Design System)**：
   - 严禁使用任何高饱和度的“一眼 AI”紫色（如 `#7c6fcd`）。
   - 前端配色必须严格遵守 `docs/engineering/frontend_code_generation_harness.md` 中的设计规范，统一使用“暗岩与琥珀（Dark Rock & Warm Amber）”作为主色系。
   - 严禁在组件的局部样式中写死 Hex/RGB 色值，所有颜色必须引用全局变量（如 SCSS `$uv-primary` 或 CSS `var(--color-*)`）。

2. **uview-plus 组件库优先**：
   - 凡是 `uview-plus` 已有的基础组件（如按钮 `u-button`、输入框 `u-input`、标签 `u-tag`、弹框 `u-popup`），**严禁**手写原生 HTML/CSS 代替。
   - 定制样式时应优先使用组件提供的 Props 参数，而非强行覆写类名。

3. **空间与间距步长约束**：
   - 所有间距（margin、padding、gap）及大小必须遵循 8px（或 4px 辅助）空间步长，严禁出现奇数或无规律的像素数字。

4. **物理隔离分层 (Stateless Components)**：
   - 核心展示组件（`src/components/`）必须保持 stateless（无状态）。组件内部不准发起任何网络请求（API），数据来源必须且仅能依靠 `props`。
   - 组件内部不能直接修改 Pinia/Vuex 的全局状态，所有操作事件必须通过 `emits` 向上抛给页面（`src/pages/`），由页面负责业务和状态的流转。
   - 核心展示 UI 在页面中复用次数超过 2 次，必须抽取为独立组件，拒绝冗余粘贴。
