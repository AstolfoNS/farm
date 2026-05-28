# Phase 2 SQL 执行说明（UTF-8）

请按顺序手动执行以下脚本：

1. `sql/phase2-001-plot-management-schema.sql`
2. `sql/phase2-002-plot-management-migration.sql`
3. `sql/phase2-003-plot-management-fix.sql`

如需回滚：

1. `sql/phase2-004-plot-management-rollback.sql`

## 说明

1. 脚本基于 PostgreSQL。
2. 脚本尽量幂等，可重复执行（回滚脚本除外，回滚是恢复操作）。
3. 迁移脚本会创建备份表：
   - `farm.user_plots_bak_phase2`
   - `farm.user_plot_allocations_bak_phase2`

