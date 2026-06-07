# Bean 目录文档

`bean/` 目录包含两类数据传输对象：**DTO**（请求参数）和 **VO**（响应视图），共计约 78 个类。

## 目录结构

```
bean/
├── dto/    (38 个请求参数类)
│   ├── PageQueryDTO.java          — 通用分页排序参数
│   ├── IdDTO.java                 — 通用 ID 参数
│   ├── User*.java                 — 用户模块
│   ├── Seed*.java                 — 种子/商店模块
│   ├── Crop*.java                 — 作物模块
│   ├── Plot*.java                 — 地块模块
│   ├── SoilType*.java             — 土壤管理
│   └── File*.java                 — 文件模块
└── vo/     (40 个响应视图类)
    ├── R.java                     — 统一响应体 (在 common/apis/)
    ├── PageResult.java            — 分页结果 (在 common/pages/)
    ├── OptionVO.java              — 通用下拉选项 {id, text}
    ├── User*.java                 — 用户模块
    ├── Seed*.java                 — 种子/商店模块
    ├── Crop*.java                 — 作物模块
    ├── Plot*.java                 — 地块模块
    └── Soil*.java                 — 土壤
```

## 通用类

| 类              | 位置                           | 说明                                      |
| --------------- | ------------------------------ | ----------------------------------------- |
| `R<T>`          | `common/apis/R.java`           | 统一响应 `{code, msg, data}`              |
| `PageResult<T>` | `common/pages/PageResult.java` | 分页 `{pageNo, pageSize, total, records}` |
| `PageQueryDTO`  | `dto/PageQueryDTO.java`        | `page, rows, sort, order`                 |
| `IdDTO`         | `dto/IdDTO.java`               | `id` (Long)                               |
| `OptionVO`      | `vo/OptionVO.java`             | `{id, text}` 下拉选项                     |
