-- LinkVault 本地 MySQL 账号初始化脚本
-- 该脚本只能由管理员账号执行，例如 root 或本地 admin。
-- 仅用于本地开发环境。

CREATE DATABASE IF NOT EXISTS `linkvault`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'linkvault'@'localhost' IDENTIFIED BY '123456';
CREATE USER IF NOT EXISTS 'linkvault'@'127.0.0.1' IDENTIFIED BY '123456';
CREATE USER IF NOT EXISTS 'linkvault'@'%' IDENTIFIED BY '123456';

ALTER USER 'linkvault'@'localhost' IDENTIFIED BY '123456';
ALTER USER 'linkvault'@'127.0.0.1' IDENTIFIED BY '123456';
ALTER USER 'linkvault'@'%' IDENTIFIED BY '123456';

GRANT ALL PRIVILEGES ON `linkvault`.* TO 'linkvault'@'localhost';
GRANT ALL PRIVILEGES ON `linkvault`.* TO 'linkvault'@'127.0.0.1';
GRANT ALL PRIVILEGES ON `linkvault`.* TO 'linkvault'@'%';

FLUSH PRIVILEGES;
