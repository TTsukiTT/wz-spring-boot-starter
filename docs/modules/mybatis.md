# wz-spring-boot-starter-mybatis

MyBatis-Plus 集成 Starter，封装分页、审计字段、多数据源等数据库层通用能力。

::: warning 开发中
该模块目前仅有 POM 骨架，功能尚未实现。
:::

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-mybatis</artifactId>
</dependency>
```

## 规划能力

| 能力 | 说明 |
|------|------|
| 分页插件 | 统一分页参数与 `PageResult` 响应 |
| MetaObjectHandler | 自动填充 `createTime`、`updateTime`、`createBy`、`updateBy` |
| 逻辑删除 | 全局逻辑删除配置 |
| BaseEntity | 通用实体基类，含审计字段 |
| BaseMapper | 扩展 MyBatis-Plus BaseMapper |
| 多数据源 | 读写分离、多库支持 |

## 规划配置

```yaml
wz:
  mybatis:
    enabled: true
    logic-delete: true
    audit: true
```

## 规划用法

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {

    private String orderNo;
    private BigDecimal amount;
}
```

```java
public interface OrderMapper extends BaseMapper<Order> {
}
```

```java
@Service
public class OrderService {

    public PageResult<OrderVO> page(OrderQuery query) {
        Page<Order> page = orderMapper.selectPage(
            query.toPage(),
            query.toWrapper()
        );
        return PageResult.of(page, OrderConverter::toVO);
    }
}
```
