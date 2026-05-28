# SQL 脚本分类与执行说明（UTF-8）

## 1) 建表脚本

1. `sql/database.sql`
2. `src/main/resources/database.sql`

要求：两份文件内容必须保持一致。

## 2) 示例数据初始化脚本

1. `sql/data.sql`
2. `src/main/resources/data.sql`

## 3) 其他 SQL 脚本

当前不保留迁移/修复/回滚脚本。
如需处理异常数据，直接执行重建流程：

1. 重建 schema 并重建所有表（执行 `database.sql`）。
2. 重新导入示例数据（执行 `data.sql`）。

## 建议执行顺序

1. 执行建表脚本：`database.sql`
2. 执行数据初始化脚本：`data.sql`
3. 如有异常，重复 1 和 2 即可（重建全量）。
