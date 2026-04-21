# Java 链接源信息解析过程记录

## 1. 记录目的

本文用于记录一次从零验证“手动粘贴 URL 后，Java 能否解析出链接背后的源信息”的过程性实验结果，方便后续 AI 或人工继续推进数据库设计、接口设计与正式工程落地时快速复用。

本次实验目标字段共四个：

- `platform`
- `publisher`
- `title`
- `publishedAt`

覆盖的目标平台：

- Bilibili
- YouTube
- X / Twitter
- 微信公众号

当前实验原型代码目录：

- [sandbox/url-metadata-prototype](/home/hang/workspace/AndroidAPP开发/sandbox/url-metadata-prototype)

说明：

- 该目录是本地实验资产，不纳入 Git 版本管理
- 过程记录纳入仓库，便于后续 AI 直接读取思路与结论

## 2. 实验结论

本次实验已经验证：

- Java 标准库即可完成多平台 URL 元信息解析，不需要先搭完整工程
- `platform / publisher / title / publishedAt` 在当前选取的四个平台上都已成功验证到可获取路径
- 各平台不适合用“一个通用 HTML 解析器”硬做，必须采用“平台识别 + 平台定制提取器”的方式

最终结论：

- 正式后端实现应采用“统一入口 + 平台分发 + 平台定制解析”的结构
- `publishedAt` 在不同平台的来源差异很大，不应强求统一解析策略

## 3. 原型目录说明

当前本地实验目录包含：

- `UrlMetadataProbe.java`
- `README.md`
- 编译产物 `.class`
- IDEA 元数据 `.idea/`、`out/`

该目录迁入仓库的目的不是纳入版本控制，而是：

- 方便当前项目内后续 AI 直接读取原型代码
- 避免实验代码继续散落在 `/home/hang/workspace` 根目录
- 在进入正式后端工程前保留一份可执行参考实现

## 4. 平台解析策略

### 4.1 Bilibili

处理方式：

- 先跟随 `b23.tv` 短链跳转到真实视频链接
- 从最终链接中提取 `BV` 号
- 调用公开接口 `https://api.bilibili.com/x/web-interface/view?bvid=...`

字段来源：

- `platform`：固定为 `Bilibili`
- `publisher`：接口返回 `owner.name`
- `title`：接口返回 `title`
- `publishedAt`：接口返回 `pubdate`

结论：

- Bilibili 是当前四个平台里最稳定的一类
- 页面 HTML 解析不如直接走公开视频接口

### 4.2 YouTube

处理方式：

- 从 `youtu.be` 或 `youtube.com` 链接中提取 `videoId`
- 通过 `oEmbed` 获取基础字段
- 通过官方 `YouTube Data API v3` 获取发布时间

字段来源：

- `platform`：固定为 `YouTube`
- `publisher`：`oEmbed.author_name`
- `title`：`oEmbed.title`
- `publishedAt`：`videos?part=snippet` 返回的 `snippet.publishedAt`

结论：

- `title / publisher` 用 `oEmbed` 足够稳定
- `publishedAt` 不适合继续从页面或内置接口硬抓
- 正式实现时，YouTube 发布时间建议明确依赖官方 API

### 4.3 X / Twitter

处理方式：

- 先从 URL 路径中提取 `handle` 与 `statusId`
- 优先请求 `https://cdn.syndication.twimg.com/tweet-result?id=...`
- 根据返回内容区分“文章型”与“普通帖子型”
- 若 syndication 接口不可用，再退回 `publish.twitter.com/oembed`

字段来源：

- `platform`：固定为 `X`
- `publisher`：`user.name`
- `publishedAt`：`created_at`

标题规则：

- 如果是文章型内容：优先取 `article.title`
- 否则取 `article.preview_text`
- 再否则取普通帖子的 `text` 前若干字符
- 最后才退回 `Tweet by @handle`

结论：

- X 不适合只靠 `oEmbed`，因为 `oEmbed` 很可能只返回一条 `t.co` 链接
- 文章型内容和普通帖子必须分开处理

### 4.4 微信公众号

处理方式：

- 必须使用移动端微信 UA 请求页面
- 额外补 `Accept / Accept-Language / Referer`
- 从页面 DOM 或脚本变量中提取字段

字段来源：

- `platform`：固定为 `微信公众号`
- `publisher`：`#js_name` 或 `#js_author_name_text`
- `title`：`#activity-name` 或 `var msg_title`
- `publishedAt`：`var ct`

结论：

- 桌面 UA 容易进入验证码页或 `wappoc_appmsgcaptcha`
- 微信页面解析依赖请求头特征，不能简单用默认 Java HTTP 请求替代浏览器

## 5. 关键踩坑记录

### 5.1 通用 HTML 解析策略不可行

一开始尝试只抓网页 HTML，再统一提取：

- `<title>`
- `og:title`
- `author`
- `publish_time`
- `article:published_time`

结果发现：

- Bilibili、YouTube、X、微信都不是理想的“纯静态网页”
- 单靠通用 meta 标签提取，成功率和稳定性都不够

最终调整为：

- 平台识别优先
- 平台内走专用抓取路径
- 通用 HTML 提取只保留兜底角色

### 5.2 X 的标题不能简单取正文首行

最初版本将 X 标题直接视为“正文前若干字”，但实际验证发现：

- 有些 X 链接是“文章型”内容
- 文章型内容页面中有独立标题
- 直接拿正文开头会丢失更准确的文章标题

最终修正为：

- 文章型：优先用 `article.title`
- 普通帖子：再用正文摘要

### 5.3 YouTube 发布时间不适合继续硬抓

实验中尝试过：

- 直接抓 `watch` 页面
- 抓 `embed` 页面
- 抓页面中的 `INNERTUBE_API_KEY` 后再打内置接口

问题：

- 页面字段越来越不稳定
- 内置接口容易返回 `LOGIN_REQUIRED`
- 继续硬抠发布时间收益不高

最终选择：

- 保留 `oEmbed` 负责基础字段
- `publishedAt` 明确走官方 Data API

### 5.4 `.bashrc` 里的环境变量不一定能被 Java 进程拿到

实验中把：

- `export YOUTUBE_API_KEY="..."`

写入了 `~/.bashrc`，但 Java 程序里：

```java
String apiKey = System.getenv("YOUTUBE_API_KEY");
```

仍然得到 `null`。

定位原因：

- `~/.bashrc` 中存在非交互 shell 早退逻辑
- IDEA 的 Run/Debug 进程也不一定继承 shell 环境变量

最终结论：

- shell 启动程序时，环境变量应放在 `return` 之前
- IDEA 调试时，应在 `Run/Debug Configurations` 里显式配置环境变量

## 6. 当前原型已验证的能力

当前原型已验证：

- Bilibili：四字段可取
- YouTube：四字段可取，但 `publishedAt` 依赖 `YOUTUBE_API_KEY`
- X：
  - 文章型内容可取独立标题
  - 普通帖子可取正文摘要
  - `publishedAt` 可取
- 微信公众号：四字段可取

## 7. 对正式后端实现的建议

正式工程建议采用以下结构：

- `UrlMetadataService`
  - 统一入口，输入 URL，输出元信息对象
- `PlatformDetector`
  - 负责平台识别与短链归一化
- `MetadataExtractor`
  - 接口定义
- 具体实现：
  - `BilibiliExtractor`
  - `YouTubeExtractor`
  - `XExtractor`
  - `WeChatExtractor`
  - `GenericWebExtractor`

统一输出字段建议：

- `originalUrl`
- `finalUrl`
- `platform`
- `publisher`
- `title`
- `publishedAt`
- `metaStatus`
- `metaFetchedAt`

## 8. 下一步建议

当前可以进入下一阶段：

1. 设计数据库字段
2. 设计“保存原始 URL -> 异步抓取元信息 -> 回填字段”的后端流程
3. 设计前端展示接口
4. 将实验代码中的平台策略迁移到正式 Spring Boot 服务中

不建议直接把当前原型代码当生产代码接入：

- 当前实现是碰头代码
- 没有统一异常模型
- 没有缓存、限流、重试策略分层
- 没有测试结构与可维护的模块边界

但它足够作为后续正式实现的高质量参考样本。
