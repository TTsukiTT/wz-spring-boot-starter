# wz-spring-boot-starter-test

测试 Starter，提供集成测试工具与 MockMvc 封装。

::: warning 开发中
该模块目前仅有 POM 骨架，功能尚未实现。
:::

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## 规划能力

| 能力 | 说明 |
|------|------|
| `@WzSpringBootTest` | 集成测试基类注解 |
| MockMvc 工具 | 简化 Controller 层测试 |
| Testcontainers | 集成测试容器支持 |

## 规划用法

```java
@WzSpringBootTest
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateOrder() throws Exception {
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"productId": 1, "quantity": 2}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data").isNumber());
    }
}
```

## 规划测试基类

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public @interface WzSpringBootTest {
}
```
