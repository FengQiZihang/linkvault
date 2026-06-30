# LinkVault - 智能链接保管箱

LinkVault 是一个跨平台的多功能书签与链接管理系统。它能够自动解析用户保存的链接元数据，并根据平台、作者、发布时间进行分类与智能展示。

## 项目结构

- **`frontend/`**：基于 uni-app (Vue 3 + Vite + Pinia) 构建的跨平台前端应用。
- **`backend/linkvault-server/`**：基于 Spring Boot 3 + MyBatis-Plus + MySQL 构建的后端 RESTful 服务。
- **`docker/`**：用于容器化部署的相关配置。

## 核心特性

- **跨平台支持**：前端基于 uni-app，可编译至 H5、小程序、安卓/iOS App 等多端。
- **智能链接解析**：
  - 针对 Bilibili、YouTube、Twitter/X 等平台提供专门 the API 级信息抓取。
  - 通用网页通过 meta 标签兜底抓取，自动获取标题、作者及发布时间。
  - 支持外网代理配置（通过 `HTTP_PROXY` / `HTTPS_PROXY` 环境变量）。
- **短信验证码服务**：集成了阿里云短信服务进行用户注册/登录的身份认证。

## 快速开始

### 后端运行

1. 进入 `backend/linkvault-server/` 目录。
2. 配置必要的环境变量（可选）：
   - `YOUTUBE_API_KEY`（用于 YouTube 视频信息高精度解析）
   - `ALIYUN_ACCESS_KEY_ID` / `ALIYUN_ACCESS_KEY_SECRET`（用于阿里云短信验证）
3. 运行 Maven 命令启动服务：
   ```bash
   mvn clean spring-boot:run
   ```

### 前端运行

1. 进入 `frontend/` 目录。
2. 安装依赖并启动开发服务：
   ```bash
   npm install
   npm run dev
   ```

## 开源协议

本项目采用 MIT 协议开源。
