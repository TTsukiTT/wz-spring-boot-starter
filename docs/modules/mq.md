# wz-spring-boot-starter-mq

消息队列 Starter，提供统一的消息发送与消费抽象。

::: warning 开发中
该模块目前仅有 POM 骨架，功能尚未实现。
:::

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-mq</artifactId>
</dependency>
```

## 规划能力

| 能力 | 说明 |
|------|------|
| MessageProducer | 统一消息发送接口 |
| MessageConsumer | 统一消息消费模板 |
| 多后端支持 | RocketMQ、Kafka、RabbitMQ |
| 消息序列化 | 统一 JSON 序列化 |

## 规划配置

```yaml
wz:
  mq:
    enabled: true
    provider: rocketmq
```

## 规划用法

### 发送消息

```java
@Service
@RequiredArgsConstructor
public class OrderService {

    private final MessageProducer messageProducer;

    public void createOrder(CreateOrderRequest request) {
        Order order = saveOrder(request);
        messageProducer.send("order-created", OrderCreatedEvent.of(order));
    }
}
```

### 消费消息

```java
@Component
public class OrderCreatedConsumer {

    @MessageListener(topic = "order-created")
    public void onOrderCreated(OrderCreatedEvent event) {
        // 处理订单创建事件
    }
}
```
