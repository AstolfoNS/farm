# UserService — 用户服务接口

**文件**: `service/UserService.java` | **实现**: `UserServiceImp`

## 方法列表

### `PageResult<UserInfoVO> list()`
- **说明**: 获取所有未删除用户，包装为单页结果
- **返回**: 用户信息列表分页

### `PageResult<UserInfoVO> gridDataFilterSortPage(String name, PageQueryDTO pageRequest)`
- **说明**: 按用户名模糊搜索，支持分页和排序
- **参数**: `name` 搜索关键词 | `pageRequest` 分页排序参数
- **返回**: 分页用户列表

### `UserInfoVO addOrUpdate(UserAddOrUpdateDTO params)`
- **说明**: 新增或更新用户。id 为空时新增并初始化默认地块；id>0 时更新
- **事务**: @Transactional
- **流程**: 新增时 → `initDefaultPlotsForNewUser()` 根据 PlotPolicy 创建地块
- **返回**: 用户信息

### `void delete(IdDTO params)`
- **说明**: 按 ID 软删除用户
- **事务**: @Transactional

### `UserAvatarVO updateAvatar(UserAvatarUpdateDTO params)`
- **说明**: 更新用户头像路径
- **事务**: @Transactional

### `List<UserInfoVO> loginUserOptions()`
- **说明**: 返回所有未删除用户，供前端登录下拉选择

### `CurUserVO setCurUser(HttpSession session, SetCurUserDTO user)`
- **说明**: 设置当前会话用户（简化登录），写入 Session
- **返回**: 含资产信息的当前用户 VO

### `CurUserVO getCurUser(HttpSession session)`
- **说明**: 从 Session 读取当前用户资产信息
- **依赖**: `AssetDefaultProvider` 获取默认资源路径

### `UserSettingsVO getCurUserSettings(HttpSession session)`
- **说明**: 读取当前用户的偏好设置（音效/背景音乐）

### `UserSettingsVO saveCurUserSettings(HttpSession session, UserSettingsUpdateDTO params)`
- **说明**: 保存当前用户的偏好设置到 `users.preferences_json`
- **事务**: @Transactional
