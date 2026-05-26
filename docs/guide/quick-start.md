# 快速开始

## 环境要求

- JDK 17+
- Maven 3.9+

验证 Java 版本：

```bash
java -version
# 应显示 17.x 或更高
```

## 引入依赖

**推荐**：业务项目继承 `wz-spring-boot-starter-parent`，已内置 JDK 17、`-parameters` 编译参数与 BOM 版本管理：

```xml
<parent>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</parent>

<dependencies>
    <dependency>
        <groupId>com.kwz</groupId>
        <artifactId>wz-spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

**或者**仅导入 BOM（需自行配置编译参数，见下方「常见问题」）：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.kwz</groupId>
            <artifactId>wz-dependencies</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>com.kwz</groupId>
        <artifactId>wz-spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

> `wz-spring-boot-starter-web` 已传递依赖 `wz-spring-boot-starter-core` 和 `wz-common`，无需重复引入。

## 编写 Controller

引入 `wz-spring-boot-starter-web` 后，Controller **可直接返回业务对象**，由 `ResultWrapperResponseBodyAdvice` 自动包装为 `Result`：

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public UserVO getUser(@PathVariable("id") Long id) {
        return userService.getById(id);
    }

    @GetMapping
    public List<UserVO> listUsers() {
        return userService.listAll();
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";  // 自动包装为 Result<String>
    }
}
```

响应（`Accept-Language: zh-CN`）：

```json
{
  "code": 0,
  "message": "成功",
  "data": { "id": 1, "name": "张三" },
  "timestamp": 1716633600000
}
```

也可继续手动返回 `Result`：

```java
@GetMapping("/{id}")
public Result<UserVO> getUser(@PathVariable("id") Long id) {
    return Result.ok(userService.getById(id));
}
```

### 跳过自动包装

文件下载、SSE、第三方回调等场景使用 `@NoWrapResult`：

```java
@NoWrapResult
@GetMapping("/export")
public ResponseEntity<byte[]> export() { ... }
```

## 抛出业务异常

```java
public UserVO getById(Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
        throw new BizException(GlobalErrorCode.NOT_FOUND);
    }
    return UserConverter.toVO(user);
}
```

异常会被 `GlobalExceptionHandler` 自动捕获，返回统一格式：

```json
{
  "code": 404,
  "message": "资源不存在",
  "data": null,
  "timestamp": 1716633600000
}
```

## 配置文件

```yaml
# application.yml
wz:
  app-name: my-service
  debug: false
```

## 本地安装 Starter

在 Starter 项目根目录执行：

```bash
mvn clean install -DskipTests
```

安装完成后，业务项目即可引用 `1.0.0-SNAPSHOT` 版本。

## 常见问题

### `@PathVariable` / `@RequestParam` 报 parameter name not available

```
IllegalArgumentException: Name for argument of type [java.lang.Long] not specified,
and parameter name information not available via reflection.
Ensure that the compiler uses the '-parameters' flag.
```

**原因**：编译时未保留方法参数名，Spring 无法解析 `@PathVariable Long id` 这类省略 `name` 的写法。

**解决方式（任选其一）**：

1. **推荐** — 继承 `wz-spring-boot-starter-parent`（已内置 `-parameters`）

2. **Maven 手动配置**：

```xml
<properties>
    <maven.compiler.parameters>true</maven.compiler.parameters>
</properties>
```

3. **IDEA 运行** — Settings → Build → Compiler → Java Compiler → Additional command line parameters 添加 `-parameters`，然后 **Rebuild Project**

4. **代码层面临时规避** — 显式指定参数名：

```java
@GetMapping("/{id}")
public Result<UserVO> getUser(@PathVariable("id") Long id) { ... }
```

## 下一步

- 了解 [架构设计](/guide/architecture)
- 查看 [模块概览](/modules/overview)
- 阅读 [配置说明](/guide/configuration)
