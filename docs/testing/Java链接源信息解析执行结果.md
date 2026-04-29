# Java 链接源信息解析执行结果

## 1. 记录目的

本文保存 `sandbox/url-metadata-prototype/UrlMetadataProbe.java` 的一次实际执行结果，可作为：

- 产品原型中的链接卡片示例数据
- 接口文档中的 `LinkMetaVO` 示例数据
- 后续后端元信息抓取服务的测试对照样本

关联资料：

- [示例URL.md](../../示例URL.md)
- [Java链接源信息解析过程记录.md](./Java链接源信息解析过程记录.md)
- [接口文档.md](../engineering/接口文档.md)

## 2. 执行环境

执行时间：

- 北京时间：`2026-04-26T16:44:38+08:00`
- UTC：`2026-04-26T08:44:38.000Z`

执行目录：

```bash
/home/hang/workspace/AndroidAPP开发/sandbox/url-metadata-prototype
```

执行命令：

```bash
javac UrlMetadataProbe.java && java UrlMetadataProbe
```

Java 版本：

```text
java version "21.0.10" 2026-01-20 LTS
Java(TM) SE Runtime Environment (build 21.0.10+8-LTS-217)
Java HotSpot(TM) 64-Bit Server VM (build 21.0.10+8-LTS-217, mixed mode, sharing)
```

说明：

- 本次默认执行 5 个内置示例 URL
- 本次 YouTube 链接成功返回 `publishedAt`，说明执行环境中的 `YOUTUBE_API_KEY` 可用
- `publishedAt` 为内容发布时间，`metaFetchedAt` 为本次抓取时间，两者含义不同
- 本次执行同时记录北京时间和 UTC，后续项目策略以 UTC 作为存储与接口基准，以北京时间作为中国用户默认展示时区

## 3. 原始控制台输出

```text
URL: https://b23.tv/DCVvWnI
status: SUCCESS
platform: Bilibili
publisher: 杜雨说AI
title: Claude突然强实名认证，你的号被封了吗？
publishedAt: 2026-04-16T08:12:36Z
finalUrl: https://www.bilibili.com/video/BV1EYdhB4EH6/?-Arouter=story&buvid=XU51A7F5D98D54E4D11BC0590A7A041766691&from_spmid=tm.recommend.0.0&is_story_h5=false&mid=9OBJNMmjfBCq%2BcQkLAN%2BAg%3D%3D&p=1&plat_id=191&share_from=ugc&share_medium=android&share_plat=android&share_session_id=b076d2ac-46a1-436c-9c06-dc68bd7c5bca&share_source=GENERIC&share_tag=s_i&spmid=main.ugc-video-detail-vertical.0.0&timestamp=1776333107&unique_k=DCVvWnI&up_id=80569724


URL: https://youtu.be/nlK7-zuYDcs?si=K4bfM7OZxc2X9IJv
status: SUCCESS
platform: YouTube
publisher: 技术爬爬虾  TechShrimp
title: 告别一切重复枯燥任务，CLI+Skill搭建AI浏览器自动化框架
publishedAt: 2026-04-11T14:31:29Z
finalUrl: https://www.youtube.com/watch?v=nlK7-zuYDcs


URL: https://x.com/Khazix0918/status/2044258725536690270?s=20
status: SUCCESS
platform: X
publisher: 数字生命卡兹克
title: 一文带你看懂，火爆全网的Harness Engineering到底是个啥。
publishedAt: 2026-04-15T03:37:15.000Z
finalUrl: https://twitter.com/Khazix0918/status/2044258725536690270


URL: https://x.com/abskoop/status/2044718328401572208
status: SUCCESS
platform: X
publisher: ahhhhfs
title: Claude AI IP 风险检测工具🤡 检测当前Claude AI出口 IP ...
publishedAt: 2026-04-16T10:03:33.000Z
finalUrl: https://twitter.com/abskoop/status/2044718328401572208


URL: https://mp.weixin.qq.com/s/1Z2H_SA0Hkv2yv8kxyiG2Q
status: SUCCESS
platform: 微信公众号
publisher: 新智元
title: 谷歌悄悄加了个按钮，Gemini长出手脚变打工人！三巨头抢着教AI干活
publishedAt: 2026-04-16T01:41:38Z
finalUrl: https://mp.weixin.qq.com/s/1Z2H_SA0Hkv2yv8kxyiG2Q?nwr_flag=1#wechat_redirect
```

## 4. 结果汇总

| 序号 | 平台 | 状态 | 发布者 | 标题 | 发布时间 |
| --- | --- | --- | --- | --- | --- |
| 1 | Bilibili | SUCCESS | 杜雨说AI | Claude突然强实名认证，你的号被封了吗？ | 2026-04-16T08:12:36Z |
| 2 | YouTube | SUCCESS | 技术爬爬虾  TechShrimp | 告别一切重复枯燥任务，CLI+Skill搭建AI浏览器自动化框架 | 2026-04-11T14:31:29Z |
| 3 | X | SUCCESS | 数字生命卡兹克 | 一文带你看懂，火爆全网的Harness Engineering到底是个啥。 | 2026-04-15T03:37:15.000Z |
| 4 | X | SUCCESS | ahhhhfs | Claude AI IP 风险检测工具🤡 检测当前Claude AI出口 IP ... | 2026-04-16T10:03:33.000Z |
| 5 | 微信公众号 | SUCCESS | 新智元 | 谷歌悄悄加了个按钮，Gemini长出手脚变打工人！三巨头抢着教AI干活 | 2026-04-16T01:41:38Z |

## 5. LinkMetaVO 示例数据

下面数据按接口文档中的 `LinkMetaVO` 字段整理。为便于前端原型与接口 Mock 使用，平台值统一成接口侧建议枚举，时间统一补齐为 UTC ISO 8601 毫秒格式。中国用户界面展示时应再转换为北京时间 `Asia/Shanghai`。

```json
[
  {
    "linkId": 1,
    "originalUrl": "https://b23.tv/DCVvWnI",
    "finalUrl": "https://www.bilibili.com/video/BV1EYdhB4EH6/?-Arouter=story&buvid=XU51A7F5D98D54E4D11BC0590A7A041766691&from_spmid=tm.recommend.0.0&is_story_h5=false&mid=9OBJNMmjfBCq%2BcQkLAN%2BAg%3D%3D&p=1&plat_id=191&share_from=ugc&share_medium=android&share_plat=android&share_session_id=b076d2ac-46a1-436c-9c06-dc68bd7c5bca&share_source=GENERIC&share_tag=s_i&spmid=main.ugc-video-detail-vertical.0.0&timestamp=1776333107&unique_k=DCVvWnI&up_id=80569724",
    "platform": "BILIBILI",
    "publisher": "杜雨说AI",
    "title": "Claude突然强实名认证，你的号被封了吗？",
    "publishedAt": "2026-04-16T08:12:36.000Z",
    "metaStatus": "SUCCESS",
    "metaFetchedAt": "2026-04-26T08:44:38.000Z"
  },
  {
    "linkId": 2,
    "originalUrl": "https://youtu.be/nlK7-zuYDcs?si=K4bfM7OZxc2X9IJv",
    "finalUrl": "https://www.youtube.com/watch?v=nlK7-zuYDcs",
    "platform": "YOUTUBE",
    "publisher": "技术爬爬虾  TechShrimp",
    "title": "告别一切重复枯燥任务，CLI+Skill搭建AI浏览器自动化框架",
    "publishedAt": "2026-04-11T14:31:29.000Z",
    "metaStatus": "SUCCESS",
    "metaFetchedAt": "2026-04-26T08:44:38.000Z"
  },
  {
    "linkId": 3,
    "originalUrl": "https://x.com/Khazix0918/status/2044258725536690270?s=20",
    "finalUrl": "https://twitter.com/Khazix0918/status/2044258725536690270",
    "platform": "X",
    "publisher": "数字生命卡兹克",
    "title": "一文带你看懂，火爆全网的Harness Engineering到底是个啥。",
    "publishedAt": "2026-04-15T03:37:15.000Z",
    "metaStatus": "SUCCESS",
    "metaFetchedAt": "2026-04-26T08:44:38.000Z"
  },
  {
    "linkId": 4,
    "originalUrl": "https://x.com/abskoop/status/2044718328401572208",
    "finalUrl": "https://twitter.com/abskoop/status/2044718328401572208",
    "platform": "X",
    "publisher": "ahhhhfs",
    "title": "Claude AI IP 风险检测工具🤡 检测当前Claude AI出口 IP ...",
    "publishedAt": "2026-04-16T10:03:33.000Z",
    "metaStatus": "SUCCESS",
    "metaFetchedAt": "2026-04-26T08:44:38.000Z"
  },
  {
    "linkId": 5,
    "originalUrl": "https://mp.weixin.qq.com/s/1Z2H_SA0Hkv2yv8kxyiG2Q",
    "finalUrl": "https://mp.weixin.qq.com/s/1Z2H_SA0Hkv2yv8kxyiG2Q?nwr_flag=1#wechat_redirect",
    "platform": "WECHAT_OFFICIAL_ACCOUNT",
    "publisher": "新智元",
    "title": "谷歌悄悄加了个按钮，Gemini长出手脚变打工人！三巨头抢着教AI干活",
    "publishedAt": "2026-04-16T01:41:38.000Z",
    "metaStatus": "SUCCESS",
    "metaFetchedAt": "2026-04-26T08:44:38.000Z"
  }
]
```

## 6. 单条导入接口响应示例

以 Bilibili 短链为例，后续“手动粘贴 URL 导入”接口可以返回：

```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "bookmarkId": 1,
    "link": {
      "linkId": 1,
      "originalUrl": "https://b23.tv/DCVvWnI",
      "finalUrl": "https://www.bilibili.com/video/BV1EYdhB4EH6/?-Arouter=story&buvid=XU51A7F5D98D54E4D11BC0590A7A041766691&from_spmid=tm.recommend.0.0&is_story_h5=false&mid=9OBJNMmjfBCq%2BcQkLAN%2BAg%3D%3D&p=1&plat_id=191&share_from=ugc&share_medium=android&share_plat=android&share_session_id=b076d2ac-46a1-436c-9c06-dc68bd7c5bca&share_source=GENERIC&share_tag=s_i&spmid=main.ugc-video-detail-vertical.0.0&timestamp=1776333107&unique_k=DCVvWnI&up_id=80569724",
      "platform": "BILIBILI",
      "publisher": "杜雨说AI",
      "title": "Claude突然强实名认证，你的号被封了吗？",
      "publishedAt": "2026-04-16T08:12:36.000Z",
      "metaStatus": "SUCCESS",
      "metaFetchedAt": "2026-04-26T08:44:38.000Z"
    }
  }
}
```

## 7. 使用建议

- 产品原型展示建议优先使用第 5 节 JSON 数组
- 接口文档示例建议直接引用第 6 节单条响应结构
- 自动化测试断言建议以 `platform / publisher / title / metaStatus` 为主，`finalUrl` 中的平台追踪参数可能随平台跳转策略变化
- 自动化测试中可断言接口时间统一为 UTC ISO 8601 毫秒格式；展示层测试再单独断言北京时间转换结果
- 正式后端可以考虑对 Bilibili、微信等链接的 `finalUrl` 做规范化，避免把一次分享链路中的追踪参数长期保存为展示 URL
