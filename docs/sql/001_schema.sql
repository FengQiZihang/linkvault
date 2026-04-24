-- LinkVault MVP schema
-- MySQL 8.0

CREATE DATABASE IF NOT EXISTS `linkvault`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE `linkvault`;

SET NAMES utf8mb4;

DROP TABLE IF EXISTS `lv_bookmark_tag`;
DROP TABLE IF EXISTS `lv_tag`;
DROP TABLE IF EXISTS `lv_bookmark`;
DROP TABLE IF EXISTS `lv_link`;
DROP TABLE IF EXISTS `lv_sms_code`;
DROP TABLE IF EXISTS `lv_user`;

CREATE TABLE `lv_user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号，登录唯一标识',
  `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(512) DEFAULT NULL COMMENT '头像地址',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态：ACTIVE/DISABLED',
  `last_login_at` DATETIME(3) DEFAULT NULL COMMENT '最近登录时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户账号';

CREATE TABLE `lv_sms_code` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
  `code` VARCHAR(10) NOT NULL COMMENT '验证码，MVP阶段可先明文',
  `scene` VARCHAR(30) NOT NULL DEFAULT 'LOGIN' COMMENT '验证码场景',
  `expires_at` DATETIME(3) NOT NULL COMMENT '过期时间',
  `used_at` DATETIME(3) DEFAULT NULL COMMENT '使用时间',
  `fail_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '校验失败次数',
  `send_ip` VARCHAR(64) DEFAULT NULL COMMENT '发送请求IP',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sms_phone_created` (`phone`, `created_at`),
  KEY `idx_sms_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='短信验证码临时记录';

CREATE TABLE `lv_link` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '链接ID',
  `original_url` VARCHAR(2048) NOT NULL COMMENT '首次入库时的原始URL，仅用于排查',
  `normalized_url` VARCHAR(2048) NOT NULL COMMENT '用于去重的规范化URL',
  `final_url` VARCHAR(2048) DEFAULT NULL COMMENT '短链跳转后的最终URL',
  `url_hash` CHAR(64) NOT NULL COMMENT 'normalized_url的SHA-256',
  `platform` VARCHAR(50) NOT NULL DEFAULT 'UNKNOWN' COMMENT '信息源/发布平台',
  `publisher` VARCHAR(255) NOT NULL DEFAULT '未知作者' COMMENT '发布作者、UP主、频道、公众号或账号名',
  `title` VARCHAR(500) NOT NULL DEFAULT '未解析标题' COMMENT '标题；普通X帖子可使用正文摘要',
  `published_at` DATETIME(3) DEFAULT NULL COMMENT '内容发布时间，UTC存储',
  `meta_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '元信息抓取状态：PENDING/SUCCESS/PARTIAL/FAILED',
  `meta_fetched_at` DATETIME(3) DEFAULT NULL COMMENT '最近一次元信息抓取完成时间',
  `meta_error` VARCHAR(1000) DEFAULT NULL COMMENT '最近一次抓取失败原因摘要',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_link_url_hash` (`url_hash`),
  KEY `idx_link_platform` (`platform`),
  KEY `idx_link_meta_status` (`meta_status`, `meta_fetched_at`),
  KEY `idx_link_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='链接对象与可复用元信息';

CREATE TABLE `lv_bookmark` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
  `link_id` BIGINT UNSIGNED NOT NULL COMMENT '链接ID',
  `original_url` VARCHAR(2048) NOT NULL COMMENT '当前用户本次粘贴的原始URL',
  `note` VARCHAR(1000) DEFAULT NULL COMMENT '用户备注',
  `saved_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '用户保存时间',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bookmark_user_link` (`user_id`, `link_id`),
  KEY `idx_bookmark_user_saved` (`user_id`, `saved_at`),
  KEY `idx_bookmark_link` (`link_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收藏关系、原始URL与私有备注';

CREATE TABLE `lv_tag` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '所属用户ID',
  `name` VARCHAR(20) NOT NULL COMMENT '标签名称',
  `is_pinned` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否置顶',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_user_name` (`user_id`, `name`),
  KEY `idx_tag_user_pinned` (`user_id`, `is_pinned`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户标签';

CREATE TABLE `lv_bookmark_tag` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `bookmark_id` BIGINT UNSIGNED NOT NULL COMMENT '收藏ID',
  `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '冗余用户ID，便于用户维度查询和清理',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bookmark_tag` (`bookmark_id`, `tag_id`),
  KEY `idx_bt_tag_bookmark` (`tag_id`, `bookmark_id`),
  KEY `idx_bt_user_tag` (`user_id`, `tag_id`),
  KEY `idx_bt_user_bookmark` (`user_id`, `bookmark_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏与标签多对多关系';
