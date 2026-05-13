# UserController API 说明

## 基本信息

- Controller 类：`cn.jxufe.farm.controller.UserController`
- 基础路径：`/user`
- 返回格式：统一使用 `Message` 或 `EasyUIData` JSON

## 接口清单（登录相关）

### 1) 获取登录下拉用户列表

- 路径：`GET /user/loginOptions`
- 用途：前端登录弹窗下拉框展示用户头像、昵称、经验、金币、积分
- 请求参数：无
- 返回：`List<Map<String,Object>>`

返回字段示例：

- `id`：用户主键
- `username`：用户名
- `nickname`：昵称
- `experience`：经验值
- `coin`：金币
- `score`：积分
- `head`：头像访问 URL

### 2) 设置当前登录用户（写入 Session）

- 路径：`POST /user/setCurUser`
- `Content-Type`：`application/json`
- 请求体示例：

```json
{
  "id": 6
}
```

- 用途：根据用户 ID 设定当前用户，并将用户实体写入服务器 `HttpSession`
- 返回：`Message`
    - `code=0`：成功
    - `code=1`：失败（用户不存在/参数无效）

### 3) 获取当前登录用户（从 Session 读取）

- 路径：`GET /user/getCurUser`
- 用途：顶部栏读取当前用户信息；未登录时返回“未知用户”默认数据
- 返回：`Message`
    - `data` 字段包含：
        - `id`
        - `username`
        - `nickname`
        - `experience`
        - `coin`
        - `score`
        - `head`

## 接口清单（玩家管理扩展）

### 4) 分页查询用户

- 路径：`GET|POST /user/gridDataFilterSortPage`
- 参数：
    - `name`：用户名模糊匹配
    - `page/rows/sort/order`：EasyUI 分页与排序参数
- 返回：`EasyUIData`

### 5) 新增或更新用户

- 路径：`POST /user/addOrUpdate`
- 参数：`id/username/nickname/experience/score/coin/avatarPath`
- 返回：`Message`

### 6) 删除用户（逻辑删除）

- 路径：`POST /user/delete`
- 参数：`id`
- 返回：`Message`

### 7) 更新头像路径

- 路径：`POST /user/updateAvatar`
- 参数：`id/avatarPath`
- 返回：`Message`
