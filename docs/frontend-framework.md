# Frontend 基础框架说明

## 目标
在不改变现有业务逻辑的前提下，为前端提供统一的基础能力：

1. 模块注册与生命周期管理
2. 事件总线（模块解耦通信）
3. 启动阶段日志与状态快照
4. 后续模块的标准接入方式

## 入口文件

1. `src/main/resources/static/js/app/core/framework.js`
2. `src/main/resources/static/js/app/modules/home.js`

## 核心对象

全局对象：`window.FarmCore`

### 主要 API

1. `boot()`: 启动框架，记录启动时间并广播 `core:booted`
2. `registerModule(name, handlers)`: 注册模块生命周期处理器
3. `activateModule(name) / deactivateModule(name) / refreshModule(name)`: 调度模块生命周期
4. `emit(eventName, payload)`: 发布事件
5. `on(eventName, handler)`: 订阅事件
6. `snapshot()`: 获取框架快照（版本、启动时间、已注册模块）

## 当前接入情况

`home.js` 在页面 ready 时会：

1. 调用 `FarmCore.boot()`
2. 统一注册模块生命周期（`user-manage`、`user-select`、`farm`、`plot-admin`、`shop`、`store`、`seed-admin`、`settings`）
3. 在模块切换时广播 `module:changed`
4. 在当前用户刷新成功后广播 `user:changed`

## 新模块接入规范

新增模块时推荐：

1. 提供 `setActive(flag)` 作为标准生命周期入口
2. 在 `home.js` 的 `buildModuleLifecycleMap()` 中接入激活/停用映射
3. 如需跨模块通信，优先通过 `FarmCore.emit/on`，避免直接互调
