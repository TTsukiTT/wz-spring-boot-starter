# Maven Central 发布流程

本文档用于指导 `wz-spring-boot-starter` 发布到 Maven Central（Sonatype Central Portal）。

## 0. 前提条件

- 已在 Central Portal 完成账号注册与 namespace 验证
- 本机可使用 GPG（`gpg --list-secret-keys --keyid-format LONG`）
- `~/.m2/settings.xml` 已配置 `serverId=central`
- 待发布版本必须是正式版本（例如 `1.0.0`，不能是 `-SNAPSHOT`）

`settings.xml` 示例：

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>${CENTRAL_TOKEN_USERNAME}</username>
      <password>${CENTRAL_TOKEN_PASSWORD}</password>
    </server>
  </servers>
</settings>
```

## 1. 发布前检查

在仓库根目录执行：

```bash
git pull
mvn -Prelease -Dmaven.deploy.skip=true clean verify
```

检查项：

- 编译、测试通过
- 能生成 `-sources.jar`、`-javadoc.jar`
- GPG 签名无报错

## 2. 切换正式版本

将根 `pom.xml` 的 `${revision}` 改为正式版本，例如：

```xml
<revision>1.0.0</revision>
```

提交版本变更：

```bash
git add .
git commit -m "release: prepare 1.0.0"
```

## 3. 发布到 Maven Central

执行发布命令：

```bash
mvn -Prelease -Dmaven.deploy.skip=true clean deploy
```

说明：

- `release` profile 会自动附加源码包、Javadoc 包、GPG 签名
- 必须显式传入 `-Dmaven.deploy.skip=true`，避免默认 `maven-deploy-plugin` 执行并触发 `distributionManagement` 报错
- `central-publishing-maven-plugin` 使用 `publishingServerId=central`
- 配置为自动发布并等待状态 `published`

## 4. 发布后操作

1. 在 Central Portal 确认发布成功
2. 打 Git Tag 并推送：

```bash
git tag v1.0.0
git push origin v1.0.0
```

3. 回切到下一开发版本（例如 `1.0.1-SNAPSHOT`）并提交：

```bash
git add .
git commit -m "chore: start next development iteration 1.0.1-SNAPSHOT"
git push
```

## 5. 常见失败排查

- `401/403`：检查 `settings.xml` 中 `central` 账号 token 是否有效
- `namespace not verified`：Central Portal 中 namespace 尚未完成验证
- `gpg: signing failed`：检查密钥、密码、`pinentry` 配置
- `SNAPSHOT not allowed`：确认 `revision` 已改为正式版
