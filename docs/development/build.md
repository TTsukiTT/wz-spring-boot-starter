# 构建与发布

## 本地构建

在项目根目录执行：

```bash
mvn clean install -DskipTests
```

构建全部 13 个子模块并安装到本地 Maven 仓库（`~/.m2/repository/com/kwz/`）。

## 版本管理

项目使用 `${revision}` 作为统一版本号：

```xml
<properties>
    <revision>1.0.0-SNAPSHOT</revision>
</properties>

<version>${revision}</version>
```

修改版本只需更新 `<revision>` 一处，所有子模块自动同步。

## flatten-maven-plugin

项目配置了 `flatten-maven-plugin`，解决 `${revision}` 占位符在发布时无法被下游项目解析的问题。

### 工作原理

```
源码 pom.xml（含 ${revision}）
        ↓  flatten-maven-plugin
.flattened-pom.xml（版本已解析为 1.0.0-SNAPSHOT）
        ↓  install / deploy
Maven 仓库中的 POM（其他项目可正常依赖）
```

### 配置说明

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>flatten-maven-plugin</artifactId>
    <configuration>
        <updatePomFile>true</updatePomFile>
        <flattenMode>resolveCiFriendliesOnly</flattenMode>
    </configuration>
</plugin>
```

| 配置 | 说明 |
|------|------|
| `updatePomFile: true` | 用扁平化后的 POM 参与 install/deploy |
| `flattenMode: resolveCiFriendliesOnly` | 只解析 `revision`、`sha1` 等 CI 友好变量 |

`.flattened-pom.xml` 已加入 `.gitignore`，不会提交到 Git。

## 发布到私服

```bash
mvn clean deploy -DskipTests
```

需在 `~/.m2/settings.xml` 中配置私服认证信息：

```xml
<servers>
    <server>
        <id>your-nexus-id</id>
        <username>admin</username>
        <password>password</password>
    </server>
</servers>
```

并在根 `pom.xml` 或 `distributionManagement` 中配置私服地址。

## 技术版本

| 组件 | 版本 |
|------|------|
| Java | 17+ |
| Spring Boot | 3.4.5 |
| MyBatis-Plus | 3.5.9 |
| Redisson | 3.41.0 |
| Hutool | 5.8.35 |
| MapStruct | 1.6.3 |
| Lombok | 1.18.36 |

## 模块构建顺序

Maven Reactor 按依赖关系自动排序：

```
wz-dependencies → wz-common → wz-spring-boot-starter-core
    → wz-spring-boot-starter-web / mybatis / redis / ...
    → wz-spring-boot-starter-security / test
```
