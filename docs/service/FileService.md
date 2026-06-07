# FileService — 文件服务

**文件**: `service/FileService.java` | **实现**: `FileServiceImp`

| 方法                                                         | 说明                                                     | 事务 |
| ------------------------------------------------------------ | -------------------------------------------------------- | ---- |
| `FileUploadResultDTO upload(MultipartFile, String category)` | 上传文件到本地存储，category 为分类目录                  | N    |
| `boolean deleteByRelativePath(String)`                       | 按相对路径删除文件                                       | N    |
| `String buildAccessUrl(String)`                              | 相对路径 → 完整访问 URL（`publicPrefix + relativePath`） | N    |
| `boolean exists(String)`                                     | 检查文件是否存在                                         | N    |

**安全**: 包含路径穿越防护（`FileAccessPathUtils`），防止 `../` 攻击

**配置**: `farm.file.storage-root` (默认 `./static/oss`), `farm.file.public-prefix` (默认 `/oss`)
