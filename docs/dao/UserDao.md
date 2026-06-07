# UserDao — 用户数据访问接口

**实体**: `User` | **基类**: `JpaRepository<User, Long>`

## 查询方法

| 方法                                                                    | 说明                       |
| ----------------------------------------------------------------------- | -------------------------- |
| `findByIsDeletedFalseOrderByIdAsc()`                                    | 所有未删除用户，按 ID 升序 |
| `findByIsDeletedFalseAndUsernameContainingIgnoreCase(String, Pageable)` | 用户名模糊搜索分页         |
| `findByUsernameAndIsDeletedFalse(String)`                               | 按用户名精确查找           |
| `findByIdAndIsDeletedFalse(Long)`                                       | 按 ID 查找                 |

## 原子操作（@Modifying + @Query）

### `decreaseCoinIfEnough(Long userId, Long amount, Long updatedBy, OffsetDateTime updatedAt) → int`

- **JPQL**: `UPDATE User SET coin = coin - :amount WHERE id = :userId AND coin >= :amount`
- **返回**: 影响行数 (>0 成功, =0 余额不足)
- **用途**: 购买/解锁/扩地扣金币

### `increaseCoin(Long userId, Long amount, Long updatedBy, OffsetDateTime updatedAt) → int`

- **JPQL**: `UPDATE User SET coin = coin + :amount WHERE id = :userId`
- **返回**: 影响行数
- **用途**: 出售果实/杀虫奖励加金币
