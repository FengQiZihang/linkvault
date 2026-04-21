-- LinkVault MVP seed data
-- 使用 示例URL.md 中的真实链接样例。

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
  (1, '13800000000', '测试用户', NULL, 'ACTIVE', '2026-04-21 10:00:00.000', '2026-04-21 10:00:00.000', '2026-04-21 10:00:00.000');

INSERT INTO `lv_sms_code`
  (`id`, `phone`, `code`, `scene`, `expires_at`, `used_at`, `send_ip`, `created_at`, `updated_at`)
VALUES
  (1, '13800000000', '123456', 'LOGIN', '2026-04-21 10:05:00.000', '2026-04-21 10:00:30.000', '127.0.0.1', '2026-04-21 10:00:00.000', '2026-04-21 10:00:30.000');

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
    '2026-04-21 10:01:00.000',
    NULL,
    '2026-04-21 10:00:00.000',
    '2026-04-21 10:01:00.000'
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
    '2026-04-21 10:01:00.000',
    NULL,
    '2026-04-21 10:00:00.000',
    '2026-04-21 10:01:00.000'
  ),
  (
    3,
    'https://x.com/Khazix0918/status/2044258725536690270?s=20',
    'https://twitter.com/Khazix0918/status/2044258725536690270',
    'https://twitter.com/Khazix0918/status/2044258725536690270',
    '9dd5fd729f5071d7558198b4dadb6a32b81d5f5eccfa8e625f403da32a98e120',
    'X',
    '数字生命卡兹克',
    '一文带你看懂，火爆全网的Harness Engineering到底是个啥。',
    '2026-04-15 03:37:15.000',
    'SUCCESS',
    '2026-04-21 10:01:00.000',
    NULL,
    '2026-04-21 10:00:00.000',
    '2026-04-21 10:01:00.000'
  ),
  (
    4,
    'https://mp.weixin.qq.com/s/1Z2H_SA0Hkv2yv8kxyiG2Q',
    'https://mp.weixin.qq.com/s/1Z2H_SA0Hkv2yv8kxyiG2Q',
    'https://mp.weixin.qq.com/s/1Z2H_SA0Hkv2yv8kxyiG2Q?nwr_flag=1#wechat_redirect',
    '30c69ecec311aa9ab5e29b08d5b8076291c555358701fcd10486e506f0646c4e',
    'WECHAT_MP',
    '新智元',
    '谷歌悄悄加了个按钮，Gemini长出手脚变打工人！三巨头抢着教AI干活',
    '2026-04-16 01:41:38.000',
    'SUCCESS',
    '2026-04-21 10:01:00.000',
    NULL,
    '2026-04-21 10:00:00.000',
    '2026-04-21 10:01:00.000'
  );

INSERT INTO `lv_bookmark`
  (`id`, `user_id`, `link_id`, `note`, `status`, `saved_at`, `deleted_at`, `created_at`, `updated_at`)
VALUES
  (1, 1, 1, '关注 Claude 账号风控和实名认证变化，后面评估工具选择时参考。', 'ACTIVE', '2026-04-21 10:02:00.000', NULL, '2026-04-21 10:02:00.000', '2026-04-21 10:02:00.000'),
  (2, 1, 2, '浏览器自动化和 CLI Skill 的组合思路，后续可参考到开发工作流。', 'ACTIVE', '2026-04-21 10:03:00.000', NULL, '2026-04-21 10:03:00.000', '2026-04-21 10:03:00.000'),
  (3, 1, 3, 'Harness Engineering 概念解释，适合后续梳理 AI 工程方法。', 'ACTIVE', '2026-04-21 10:04:00.000', NULL, '2026-04-21 10:04:00.000', '2026-04-21 10:04:00.000'),
  (4, 1, 4, 'Gemini 自动化能力案例，适合观察大模型产品方向。', 'ACTIVE', '2026-04-21 10:05:00.000', NULL, '2026-04-21 10:05:00.000', '2026-04-21 10:05:00.000');

INSERT INTO `lv_tag`
  (`id`, `user_id`, `name`, `is_pinned`, `status`, `created_at`, `updated_at`)
VALUES
  (1, 1, 'AI工具', 1, 'ACTIVE', '2026-04-21 10:02:00.000', '2026-04-21 10:02:00.000'),
  (2, 1, '开发效率', 1, 'ACTIVE', '2026-04-21 10:02:00.000', '2026-04-21 10:02:00.000'),
  (3, 1, '账号风控', 0, 'ACTIVE', '2026-04-21 10:02:00.000', '2026-04-21 10:02:00.000'),
  (4, 1, '行业观察', 0, 'ACTIVE', '2026-04-21 10:02:00.000', '2026-04-21 10:02:00.000'),
  (5, 1, '自动化', 0, 'ACTIVE', '2026-04-21 10:02:00.000', '2026-04-21 10:02:00.000');

INSERT INTO `lv_bookmark_tag`
  (`id`, `bookmark_id`, `tag_id`, `user_id`, `created_at`, `updated_at`)
VALUES
  (1, 1, 1, 1, '2026-04-21 10:06:00.000', '2026-04-21 10:06:00.000'),
  (2, 1, 3, 1, '2026-04-21 10:06:00.000', '2026-04-21 10:06:00.000'),
  (3, 2, 1, 1, '2026-04-21 10:06:00.000', '2026-04-21 10:06:00.000'),
  (4, 2, 2, 1, '2026-04-21 10:06:00.000', '2026-04-21 10:06:00.000'),
  (5, 2, 5, 1, '2026-04-21 10:06:00.000', '2026-04-21 10:06:00.000'),
  (6, 3, 1, 1, '2026-04-21 10:06:00.000', '2026-04-21 10:06:00.000'),
  (7, 3, 4, 1, '2026-04-21 10:06:00.000', '2026-04-21 10:06:00.000'),
  (8, 4, 1, 1, '2026-04-21 10:06:00.000', '2026-04-21 10:06:00.000'),
  (9, 4, 4, 1, '2026-04-21 10:06:00.000', '2026-04-21 10:06:00.000'),
  (10, 4, 5, 1, '2026-04-21 10:06:00.000', '2026-04-21 10:06:00.000');
