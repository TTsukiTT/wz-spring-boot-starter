# wz-spring-boot-starter-oss

对象存储 Starter，提供统一的文件存储抽象，支持多种存储后端。

::: warning 开发中
该模块目前仅有 POM 骨架，功能尚未实现。
:::

## 依赖

```xml
<dependency>
    <groupId>com.kwz</groupId>
    <artifactId>wz-spring-boot-starter-oss</artifactId>
</dependency>
```

## 规划能力

| 能力 | 说明 |
|------|------|
| FileStorage SPI | 统一文件上传/下载/删除接口 |
| 多后端支持 | MinIO、阿里云 OSS、AWS S3 |
| 自动配置 | 根据 `provider` 配置自动选择实现 |

## 规划配置

```yaml
wz:
  oss:
    enabled: true
    provider: minio
    endpoint: http://localhost:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: default
```

## 规划 SPI 接口

```java
public interface FileStorage {
    String upload(InputStream input, String filename, String contentType);
    InputStream download(String path);
    void delete(String path);
    String getUrl(String path);
}
```

## 规划用法

```java
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorage fileStorage;

    public String upload(MultipartFile file) {
        return fileStorage.upload(
            file.getInputStream(),
            file.getOriginalFilename(),
            file.getContentType()
        );
    }
}
```

## 扩展自定义存储后端

```java
@Component
@ConditionalOnProperty(prefix = "wz.oss", name = "provider", havingValue = "custom")
public class CustomFileStorage implements FileStorage {
    // 实现接口方法
}
```
