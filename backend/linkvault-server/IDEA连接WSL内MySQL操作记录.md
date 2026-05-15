# IDEA 连接 WSL 内 MySQL 操作记录

对应问题说明见：[项目避坑.md](/home/hang/workspace/AndroidAPP开发/backend/linkvault-server/项目避坑.md:1)

## 1. 修改 WSL 内 MySQL 监听地址

编辑文件：

```text
/etc/mysql/mysql.conf.d/mysqld.cnf
```

将：

```text
bind-address = 127.0.0.1
```

改为：

```text
bind-address = 0.0.0.0
```

如果 Windows 本机已经有 MySQL 占用 `3306` 和 `33060`，WSL 内 MySQL 会启动失败并在 `/var/log/mysql/error.log` 中出现 `Address already in use`。此时本项目本机环境改用：

```text
port = 3307
mysqlx-port = 33070
```

## 2. 重启 MySQL 并检查监听

执行：

```bash
sudo service mysql restart
sudo ss -lntp | grep 3307
```

期望看到：

```text
0.0.0.0:3307
```

不要看到：

```text
127.0.0.1:3307
```

## 3. 查询当前 WSL IP

执行：

```bash
hostname -I
```

记录返回的内网地址，例如：

```text
172.23.249.121
```

## 4. 给应用账号补充远程访问授权

进入 MySQL：

```bash
sudo mysql
```

先检查现有账号：

```sql
SELECT user, host FROM mysql.user WHERE user = 'linkvault';
```

如果只看到 `localhost` 或 `127.0.0.1`，继续执行：

```sql
CREATE USER IF NOT EXISTS 'linkvault'@'%' IDENTIFIED BY '你的密码';
GRANT ALL PRIVILEGES ON linkvault.* TO 'linkvault'@'%';
FLUSH PRIVILEGES;
```

## 5. 在 IDEA Database 面板中填写连接信息

不要填写：

- Host：`127.0.0.1`

应填写：

- Host：WSL IP，例如 `172.23.249.121`
- Port：`3307`
- User：`linkvault`
- Password：本地约定密码
- Database：`linkvault`

## 6. 分别从 WSL 和 IDEA 验证

在 WSL 中执行：

```bash
mysql -ulinkvault -p -h 127.0.0.1 -P 3307 -e "SELECT USER(), CURRENT_USER();"
mysql -ulinkvault -p -h 127.0.0.1 -P 3307 -e "USE linkvault; SHOW TABLES;"
```

在 IDEA 连上后执行：

```sql
SELECT USER(), CURRENT_USER(), @@hostname, @@port, @@datadir;
```

如果两边都成功，再继续做 Spring Boot 的数据库接入配置。  
