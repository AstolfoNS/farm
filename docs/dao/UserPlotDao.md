# UserPlotDao — 用户地块数据访问接口

**实体**: `UserPlot` | **基类**: `JpaRepository<UserPlot, Long>`

## 查询方法

| 方法                                                         | 说明                                 |
| ------------------------------------------------------------ | ------------------------------------ |
| `findByUserIdAndIsDeletedFalseOrderByPlotIndexAsc(Long)`     | 用户所有地块，按索引升序             |
| `findByUserIdAndPlotIndexAndIsDeletedFalse(Long, Short)`     | 用户指定索引地块                     |
| `findByIdAndIsDeletedFalse(Long)`                            | 按 ID 查地块                         |
| `findByIdAndUserIdAndIsDeletedFalse(Long, Long)`             | 按 ID+用户ID 查                      |
| `findTopByUserIdAndIsDeletedFalseOrderByPlotIndexDesc(Long)` | 用户最后一个地块（用于确定下一索引） |
| `findByIdInAndIsDeletedFalse(List<Long>)`                    | 按 ID 列表批量查                     |
| `existsBySoilTypeIdAndIsDeletedFalse(Long)`                  | 检查土壤是否被引用                   |
| `countByUserIdAndIsDeletedFalse(Long)`                       | 用户地块总数                         |
| `countByUserIdAndIsLockedFalseAndIsDeletedFalse(Long)`       | 用户已解锁地块数                     |
