# WSL 开发环境配置记录

## 0. 文档说明

本文记录当前 WSL 环境中与 LinkVault MVP 开发直接相关的 MySQL、Maven 与 Java 配置信息，便于后续排查环境问题、复现本地开发环境和编写启动说明。

记录时间：`2026-04-29 11:11:24 CST +0800`

记录范围：

- WSL / Ubuntu 基础环境
- Java / Maven 路径、版本和 Maven 仓库配置
- MySQL 客户端、服务端路径、版本和当前可用性
- 已发现的环境风险与项目使用建议

敏感信息处理：

- 不记录数据库密码、JWT 密钥、token、验证码等敏感值。
- Maven `settings.xml` 中即使存在 `server`、`proxy`、`username`、`password` 节点，也不在本文展开。

## 1. 系统环境

| 项 | 当前值 |
| --- | --- |
| OS | Ubuntu 24.04.4 LTS on WSL2 |
| Kernel | `6.6.87.2-microsoft-standard-WSL2` |
| 主机名 | `Navigator-Z` |
| 架构 | `x86_64` |

关键环境变量：

| 变量 | 当前值 |
| --- | --- |
| `JAVA_HOME` | `/home/hang/workspace/DevTools/java/current` |
| `MAVEN_HOME` | `/home/hang/workspace/DevTools/maven/current` |
| `MYSQL_HOME` | 不再使用自定义 MySQL 目录；当前使用 apt 安装的系统 MySQL |

说明：

- `PATH` 中存在多段重复的 Java、Maven、Android SDK 路径；当前 MySQL 命令使用系统路径 `/usr/bin/mysql`。
- 当前命令优先命中 `/home/hang/workspace/DevTools/...` 下的自定义工具链。

## 2. Java 配置

当前 `java`：

```text
/home/hang/workspace/DevTools/java/current/bin/java
java version "21.0.10" 2026-01-20 LTS
Java(TM) SE Runtime Environment (build 21.0.10+8-LTS-217)
Java HotSpot(TM) 64-Bit Server VM (build 21.0.10+8-LTS-217, mixed mode, sharing)
```

当前 `javac`：

```text
/home/hang/workspace/DevTools/java/current/bin/javac
javac 21.0.10
```

实际路径：

```text
/home/hang/workspace/DevTools/java/current -> /home/hang/workspace/DevTools/java/jdk-21.0.10
```

项目约束：

- 后端按 Java 21 开发。
- Maven 实际使用该 Java 21 运行，符合 `docs/engineering/技术栈规范与环境基线.md` 的基线。

## 3. Maven 配置

当前 `mvn`：

```text
/home/hang/workspace/DevTools/maven/current/bin/mvn
Apache Maven 3.9.14
Maven home: /home/hang/workspace/DevTools/maven/current
Java version: 21.0.10, vendor: Oracle Corporation
Java runtime: /home/hang/workspace/DevTools/java/jdk-21.0.10
```

实际路径：

```text
/home/hang/workspace/DevTools/maven/current -> /home/hang/workspace/DevTools/maven/apache-maven-3.9.14
```

### 3.1 Maven 配置文件

| 文件 | 状态 |
| --- | --- |
| `/home/hang/workspace/DevTools/maven/current/conf/settings.xml` | 存在 |
| `/home/hang/workspace/DevTools/maven/current/conf/toolchains.xml` | 存在 |
| `/home/hang/.m2/settings.xml` | 未发现 |

### 3.2 本地仓库

当前 Maven 生效的本地仓库：

```text
/home/hang/workspace/DevTools/maven/apache-maven-3.9.14/mvn_repo
```

仓库大小：

```text
117M
```

说明：

- Maven 没有使用默认的 `/home/hang/.m2/repository` 作为主仓库。
- `/home/hang/.m2/repository` 目录存在，但不是当前 Maven `settings.xml` 指定的仓库。
- 后续排查依赖下载、缓存污染或离线构建问题时，应优先检查 `mvn_repo`。

### 3.3 镜像配置

`settings.xml` 中当前可见的有效镜像配置：

| id | mirrorOf | url | 说明 |
| --- | --- | --- | --- |
| `aliyunmaven` | `central` | `https://maven.aliyun.com/repository/central` | Maven Central 使用阿里云镜像 |
| `maven-default-http-blocker` | `external:http:*` | `http://0.0.0.0/` | Maven 默认 HTTP 仓库阻断 |

项目建议：

- 项目 `pom.xml` 不要重复硬编码 Maven Central 镜像。
- 若后续依赖无法下载，优先检查该全局 `settings.xml` 和阿里云镜像可用性。
- 不要把带凭据的 `settings.xml` 复制进项目仓库。

## 4. MySQL 配置

当前 MySQL 由 Ubuntu apt 管理。

| 项 | 当前值 |
| --- | --- |
| 客户端路径 | `/usr/bin/mysql` |
| 服务端路径 | `/usr/sbin/mysqld` |
| 版本 | `8.0.45-0ubuntu0.24.04.1` |
| 服务名 | `mysql.service` |
| 服务状态 | `active` |
| 开机状态 | `enabled` |

版本信息：

```text
mysql  Ver 8.0.45-0ubuntu0.24.04.1 for Linux on x86_64 ((Ubuntu))
/usr/sbin/mysqld  Ver 8.0.45-0ubuntu0.24.04.1 for Linux on x86_64 ((Ubuntu))
```

### 4.1 服务管理

启动、停止、查看状态：

```bash
sudo systemctl start mysql
sudo systemctl stop mysql
sudo systemctl restart mysql
sudo systemctl status mysql
```

当前服务已启用开机启动：

```bash
sudo systemctl enable mysql
```

如果 systemd 不可用，可使用：

```bash
sudo service mysql start
sudo service mysql status
```

### 4.2 本地账号

当前本地开发已创建两个 MySQL 账号：

| 用户 | 主机 | 用途 |
| --- | --- | --- |
| `admin` | `localhost` / `127.0.0.1` | 本地管理员账号 |
| `linkvault` | `localhost` / `127.0.0.1` | 项目应用账号 |

密码未在本文档明文记录。当前本地开发约定见个人 `.env` 或 IDE / VS Code 数据库连接配置。

管理员连接验证：

```bash
mysql -uadmin -p -e "SELECT USER(), CURRENT_USER(), VERSION();"
```

项目用户连接验证：

```bash
mysql -ulinkvault -p -e "USE linkvault; SHOW TABLES;"
```

### 4.3 项目数据库

当前项目数据库：

| 项 | 当前值 |
| --- | --- |
| 数据库名 | `linkvault` |
| 字符集 | `utf8mb4` |
| 排序规则 | `utf8mb4_0900_ai_ci` |
| 初始化脚本 | `docs/sql/001_schema.sql` |
| 测试数据脚本 | `docs/sql/002_seed_test_data.sql` |

当前已创建的表：

```text
lv_bookmark
lv_bookmark_tag
lv_link
lv_sms_code
lv_tag
lv_user
```

### 4.4 VS Code 连接参数

项目库连接：

```text
Host: 127.0.0.1
Port: 3306
User: linkvault
Database: linkvault
```

管理员连接：

```text
Host: 127.0.0.1
Port: 3306
User: admin
```

说明：密码只保存在本机 VS Code 数据库插件、`.env` 或 IDE 运行配置中，不提交到 Git。

## 5. 项目使用建议

### 5.1 Maven

- 后端构建默认使用 `/home/hang/workspace/DevTools/maven/current/bin/mvn`。
- 本地依赖缓存以 `/home/hang/workspace/DevTools/maven/apache-maven-3.9.14/mvn_repo` 为准。
- 项目文档或脚本中如需写 Maven 命令，可直接使用：

```bash
mvn test
mvn spring-boot:run
```

前提是当前 shell 已正确加载 `MAVEN_HOME` 和 `PATH`。

### 5.2 MySQL

当前 MySQL 已可用于本地开发：

- 使用 `admin` 做本地数据库管理。
- 使用 `linkvault` 做项目后端连接账号。
- 如本地还没有 `linkvault` 账号，先使用管理员账号执行：

```bash
mysql -uadmin -p < docs/sql/000_local_user.sql
```

- 如需重建表结构，执行：

```bash
mysql -ulinkvault -p < docs/sql/001_schema.sql
mysql -ulinkvault -p < docs/sql/002_seed_test_data.sql
mysql -ulinkvault -p -e "USE linkvault; SHOW TABLES;"
```

### 5.3 环境变量

项目后端本地运行建议至少准备：

```text
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=linkvault
DB_USER=linkvault
DB_PASSWORD=<local-password>
JWT_SECRET=<local-dev-secret>
JWT_EXPIRES_SECONDS=604800
SMS_PROVIDER=mock
LINK_META_TIMEOUT_MS=3000
```

注意：

- `.env` 或 IDE 运行配置可以保存本机真实值。
- `.env.example` 只能保存示例值。
- 真实密码和 JWT 密钥不得提交到 Git。

## 6. 后续待办

- 后端工程创建后，记录实际 `application.yml` / 环境变量映射。
- 若后续引入 Docker MySQL，补充容器端口、数据卷、字符集和时区配置。
