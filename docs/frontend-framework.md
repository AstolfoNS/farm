# Frontend 基础框架说明

## 目标
在不改变现有业务逻辑的前提下，前端框架层提供统一能力：

1. 模块注册与生命周期管理
2. 事件总线（模块解耦通信）
3. 启动日志与状态快照
4. 新模块标准接入路径

## 入口文件

1. `src/main/resources/static/js/app/core/framework.js`
2. `src/main/resources/static/js/app/modules/home.js`

## 核心对象

全局对象：`window.FarmCore`

### 主要 API

1. `boot()`: 启动框架，记录启动时间并广播 `core:booted`
2. `registerModule(name, handlers)`: 注册模块生命周期处理器
3. `registerSetActiveModule(name, moduleApi, options)`: 基于 `setActive(flag)` 的快速注册
4. `activateModule(name) / deactivateModule(name) / refreshModule(name)`: 调度模块生命周期
5. `emit(eventName, payload)`: 发布事件
6. `on(eventName, handler)`: 订阅事件
7. `snapshot()`: 获取框架快照（版本、启动时间、已注册模块）

## 当前接入方式

采用“模块自注册 + 壳层补充注册”两层结构：

1. 业务模块 `farm/shop/store/plot-admin/user-admin/seed-admin` 在各自文件中调用 `registerSetActiveModule(...)` 自注册
2. `home.js` 仅注册壳层模块（`user-select`、`settings`）
3. 模块切换优先走 `FarmCore` 注册中心，降低 `home.js` 对具体模块实现的硬编码依赖

## 运行期事件

1. `core:booted`: 框架启动完成
2. `module:changed`: 页面模块切换完成（包含 `previous/current`）
3. `user:changed`: 当前用户刷新成功（包含 `user/userId`）
4. `core:error`: 模块生命周期 hook 异常

## 新模块接入规范

1. 提供 `setActive(flag)` 作为标准生命周期入口
2. 在模块文件末尾调用 `FarmCore.registerSetActiveModule("module-name", ModuleApi, options)`
3. 需要刷新行为时通过 `options.refreshMethod` 或 `options.refresh` 挂载
4. 跨模块通信优先使用 `FarmCore.emit/on`，避免直接互相调用
