-- LinkVault MVP seed data
-- 使用 示例URL.md 中的链接样例，时间字段按 UTC 写入。

USE `linkvault`;

SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE `lv_bookmark_tag`;
TRUNCATE TABLE `lv_tag`;
TRUNCATE TABLE `lv_bookmark`;
TRUNCATE TABLE `lv_link`;
TRUNCATE TABLE `lv_sms_code`;
TRUNCATE TABLE `lv_user`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `lv_user`
  (`id`, `phone`, `nickname`, `avatar_url`, `status`, `last_login_at`, `created_at`, `updated_at`)
VALUES
  (1, '13800000000', '风启自航', '/static/avatars/avatar-01.png', 'ACTIVE', '2026-04-26 09:00:00.000', '2026-04-21 10:00:00.000', '2026-04-26 09:00:00.000'),
  (2, '13900000000', '测试用户二', '/static/avatars/avatar-03.png', 'ACTIVE', '2026-04-26 09:05:00.000', '2026-04-21 10:10:00.000', '2026-04-26 09:05:00.000');

INSERT INTO `lv_sms_code`
  (`id`, `phone`, `code`, `scene`, `expires_at`, `used_at`, `fail_count`, `send_ip`, `created_at`, `updated_at`)
VALUES
  (1, '13800000000', '123456', 'LOGIN', '2026-04-26 09:05:00.000', '2026-04-26 09:00:00.000', 0, '127.0.0.1', '2026-04-26 08:59:30.000', '2026-04-26 09:00:00.000'),
  (2, '13900000000', '123456', 'LOGIN', '2026-04-26 09:10:00.000', '2026-04-26 09:05:00.000', 0, '127.0.0.1', '2026-04-26 09:04:30.000', '2026-04-26 09:05:00.000');

INSERT INTO `lv_link`
  (`id`, `original_url`, `normalized_url`, `final_url`, `url_hash`, `platform`, `publisher`, `title`, `published_at`, `meta_status`, `meta_fetched_at`, `meta_error`, `created_at`, `updated_at`)
VALUES
  (
    1,
    'https://b23.tv/DCVvWnI',
    'https://www.bilibili.com/video/BV1EYdhB4EH6',
    'https://www.bilibili.com/video/BV1EYdhB4EH6',
    '30e6731819b201e9a012004e073d298ebd05446e02e1d81bcb6c3c5f7c968eeb',
    'BILIBILI',
    '杜雨说AI',
    'Claude突然强实名认证，你的号被封了吗？',
    '2026-04-16 08:12:36.000',
    'SUCCESS',
    '2026-04-26 09:01:00.000',
    NULL,
    '2026-04-26 09:00:00.000',
    '2026-04-26 09:01:00.000'
  ),
  (
    2,
    'https://youtu.be/nlK7-zuYDcs?si=K4bfM7OZxc2X9IJv',
    'https://www.youtube.com/watch?v=nlK7-zuYDcs',
    'https://www.youtube.com/watch?v=nlK7-zuYDcs',
    '1fb097d86996a0f5f7d192f25a64e66ae479678bd8b77525943bb3f874b6c2b1',
    'YOUTUBE',
    '技术爬爬虾  TechShrimp',
    '告别一切重复枯燥任务，CLI+Skill搭建AI浏览器自动化框架',
    '2026-04-11 14:31:29.000',
    'SUCCESS',
    '2026-04-26 09:01:00.000',
    NULL,
    '2026-04-26 09:00:00.000',
    '2026-04-26 09:01:00.000'
  ),
  (
    3,
    'https://x.com/Khazix0918/status/2044258725536690270?s=20',
    'https://twitter.com/Khazix0918/status/2044258725536690270',
    'https://twitter.com/Khazix0918/status/2044258725536690270',
    '9dd5fd729f5071d7558198b4dadb6a32b81d5f5eccfa8e625f403da32a98e120',
    'X',
    '数字生命卡兹克',
    '一文带你看懂，火爆全网的 Harness Engineering 到底是个啥。',
    '2026-04-15 03:37:15.000',
    'SUCCESS',
    '2026-04-26 09:01:00.000',
    NULL,
    '2026-04-26 09:00:00.000',
    '2026-04-26 09:01:00.000'
  ),
  (
    4,
    'https://x.com/abskoop/status/2044718328401572208',
    'https://twitter.com/abskoop/status/2044718328401572208',
    'https://twitter.com/abskoop/status/2044718328401572208',
    '0ed562b98da58fee591e14867266816a90d907b2ec4ea62797469e80194ce757',
    'X',
    'ahhhhfs',
    'Claude AI IP 风险检测工具：检测当前 Claude AI 出口 IP 风险',
    '2026-04-16 10:03:33.000',
    'SUCCESS',
    '2026-04-26 09:01:00.000',
    NULL,
    '2026-04-26 09:00:00.000',
    '2026-04-26 09:01:00.000'
  ),
  (
    5,
    'https://mp.weixin.qq.com/s/1Z2H_SA0Hkv2yv8kxyiG2Q',
    'https://mp.weixin.qq.com/s/1Z2H_SA0Hkv2yv8kxyiG2Q',
    'https://mp.weixin.qq.com/s/1Z2H_SA0Hkv2yv8kxyiG2Q?nwr_flag=1#wechat_redirect',
    '30c69ecec311aa9ab5e29b08d5b8076291c555358701fcd10486e506f0646c4e',
    'WECHAT_OFFICIAL_ACCOUNT',
    '新智元',
    '谷歌悄悄加了个按钮，Gemini长出手脚变打工人！三巨头抢着教AI干活',
    '2026-04-16 01:41:38.000',
    'SUCCESS',
    '2026-04-26 09:01:00.000',
    NULL,
    '2026-04-26 09:00:00.000',
    '2026-04-26 09:01:00.000'
  );

INSERT INTO `lv_bookmark`
  (`id`, `user_id`, `link_id`, `original_url`, `note`, `saved_at`, `created_at`, `updated_at`)
VALUES
  (1, 1, 1, 'https://b23.tv/DCVvWnI', '关注 AI 服务账号风控与实名策略变化，后续可整理成账号安全清单。', '2026-04-26 09:10:00.000', '2026-04-26 09:10:00.000', '2026-04-26 09:10:00.000'),
  (2, 1, 2, 'https://youtu.be/nlK7-zuYDcs?si=K4bfM7OZxc2X9IJv', '适合作为个人自动化工作流和 Skill 设计的参考样例。', '2026-04-26 08:58:00.000', '2026-04-26 08:58:00.000', '2026-04-26 08:58:00.000'),
  (3, 1, 3, 'https://x.com/Khazix0918/status/2044258725536690270?s=20', '用于补充 AI-Native 工程流程里对执行链路和上下文约束的理解。', '2026-04-25 21:30:00.000', '2026-04-25 21:30:00.000', '2026-04-25 21:30:00.000'),
  (4, 1, 4, 'https://x.com/abskoop/status/2044718328401572208', '', '2026-04-25 17:22:00.000', '2026-04-25 17:22:00.000', '2026-04-25 17:22:00.000'),
  (5, 1, 5, 'https://mp.weixin.qq.com/s/1Z2H_SA0Hkv2yv8kxyiG2Q', '观察 Gemini 从问答工具走向可执行 Agent 的产品方向。', '2026-04-24 19:05:00.000', '2026-04-24 19:05:00.000', '2026-04-24 19:05:00.000'),
  (6, 2, 1, 'https://b23.tv/o6gddkZ', '同一链接被另一个用户用不同原始短链保存，用于验证数据隔离。', '2026-04-26 09:12:00.000', '2026-04-26 09:12:00.000', '2026-04-26 09:12:00.000');

INSERT INTO `lv_tag`
  (`id`, `user_id`, `name`, `is_pinned`, `created_at`, `updated_at`)
VALUES
  (1, 1, 'AI动态', 1, '2026-04-21 10:02:00.000', '2026-04-21 10:02:00.000'),
  (2, 1, '自动化', 1, '2026-04-21 10:02:00.000', '2026-04-21 10:02:00.000'),
  (3, 1, 'AI工程', 0, '2026-04-21 10:02:00.000', '2026-04-21 10:02:00.000'),
  (4, 1, '账号安全', 0, '2026-04-21 10:02:00.000', '2026-04-21 10:02:00.000'),
  (5, 1, '产品观察', 0, '2026-04-21 10:02:00.000', '2026-04-21 10:02:00.000'),
  (6, 2, '账号安全', 1, '2026-04-21 10:12:00.000', '2026-04-21 10:12:00.000');

INSERT INTO `lv_bookmark_tag`
  (`id`, `bookmark_id`, `tag_id`, `user_id`, `created_at`, `updated_at`)
VALUES
  (1, 1, 1, 1, '2026-04-26 09:10:10.000', '2026-04-26 09:10:10.000'),
  (2, 1, 4, 1, '2026-04-26 09:10:10.000', '2026-04-26 09:10:10.000'),
  (3, 2, 2, 1, '2026-04-26 08:58:10.000', '2026-04-26 08:58:10.000'),
  (4, 2, 3, 1, '2026-04-26 08:58:10.000', '2026-04-26 08:58:10.000'),
  (5, 3, 3, 1, '2026-04-25 21:30:10.000', '2026-04-25 21:30:10.000'),
  (6, 5, 1, 1, '2026-04-24 19:05:10.000', '2026-04-24 19:05:10.000'),
  (7, 5, 5, 1, '2026-04-24 19:05:10.000', '2026-04-24 19:05:10.000'),
  (8, 6, 6, 2, '2026-04-26 09:12:10.000', '2026-04-26 09:12:10.000');
