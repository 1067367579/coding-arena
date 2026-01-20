# Coding Arena - 全量接口与业务链路文档（基于代码静态整理）

> 生成时间：2026-01-19  
> 说明：本文档基于仓库源码静态扫描与阅读整理；`oj-gateway` 的路由与白名单来自 Nacos 配置（仓库内不包含全量配置），因此“网关URL/是否白名单”部分按代码可推断的默认约定给出，并标注需要以 Nacos 实际配置为准。

## 1. 服务模块与职责

| 服务/模块 | 作用 |
|---|---|
| `oj-gateway` | 网关：统一鉴权、按路径前缀区分用户端/管理端 |
| `oj-modules/oj-friend` | 用户端服务：用户、题目、提交/判题触发、竞赛、消息、文件上传 |
| `oj-modules/oj-system` | 管理端服务：管理员登录、题库管理、竞赛管理、C端用户管理 |
| `oj-modules/oj-judge` | 判题服务：沙箱执行、判题、结果落库/缓存；支持 HTTP/ RabbitMQ 两种触发 |
| `oj-modules/oj-job` | 任务服务：XXL-Job 负责竞赛缓存刷新、竞赛结果统计与消息生成 |
| `oj-api` | 跨服务 DTO/VO、Feign 接口（`RemoteJudgeService`） |
| `oj-common` | 通用组件：统一响应、Redis/安全/文件/消息/RabbitMQ 等基础能力 |

## 2. 网关鉴权、身份与调用约定

### 2.1 网关鉴权（`oj-gateway`）

- 实现位置：`oj-gateway/src/main/java/com/example/gateway/filter/AuthFilter.java`
- Token Header：`authentication: Bearer <jwt>`
- 鉴权流程（核心）：
  - 白名单匹配（来自 Nacos `security.ignore.whiteList`）命中则放行
  - 否则校验 JWT
  - 校验 Redis 登录态：`login:token:<userId>` 必须存在
  - 身份与路径匹配（字符串包含判断）：
    - URL 包含 `system` ⇒ 必须为管理员（`LoginUser.identity = 2`）
    - URL 包含 `friend` ⇒ 必须为普通用户（`LoginUser.identity = 1`）

### 2.2 服务侧 ThreadLocal（`TokenInterceptor`）

- 实现位置：`oj-common/oj-common-security/src/main/java/com/example/common/security/intercptor/TokenInterceptor.java`
- 说明：
  - 微服务侧会从 Header 解析 token，并把 `userId` 写入 `ThreadLocalUtil`（业务代码无需在接口参数中显式传 `userId`）
  - 同时对 Redis 登录态 TTL 进行续签（延长会话）
- 拦截器排除路径（见 `WebMvcConfig`）：`/**/login`、`/**/code`

### 2.3 网关 URL 前缀约定（需结合 Nacos 路由核对）

仓库内未包含网关路由配置，但鉴权逻辑依赖 URL 含 `friend/system` 进行身份匹配。常见约定如下（若 Nacos 路由不同，请以实际为准）：

- 用户端（`oj-friend`）：网关前缀通常为 `/friend`，并配置 `StripPrefix=1` ⇒ 外部URL≈`/friend` + 服务内部 Controller 路径
- 管理端（`oj-system`）：网关前缀通常为 `/system`，并配置 `StripPrefix=1` ⇒ 外部URL≈`/system` + 服务内部 Controller 路径
- 判题（`oj-judge`）：通常为内部服务直连（Feign/RabbitMQ），不建议经网关对外暴露

## 3. 统一响应结构

### 3.1 `Result<T>`（统一返回）

```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `code` | `int` | 业务状态码（见 `ResultCode`） |
| `msg` | `String` | 状态说明 |
| `data` | `T` | 响应数据 |

### 3.2 `PageResult`（分页返回）

```json
{
  "total": 0,
  "rows": [],
  "code": 1000,
  "msg": "操作成功"
}
```

| 字段 | 类型 | 说明 |
|---|---|---|
| `total` | `long` | 总数 |
| `rows` | `List<?>` | 当前页数据 |
| `code` | `int` | 业务状态码 |
| `msg` | `String` | 状态说明 |

## 4. 数据结构（请求/响应 DTO/VO）

### 4.1 鉴权与登录态

#### 4.1.1 `LoginUser`

| 字段 | 类型 | 说明 |
|---|---|---|
| `identity` | `Integer` | 身份：`1=普通用户`，`2=管理员` |
| `nickName` | `String` | 昵称 |
| `avatar` | `String` | 头像URL（用户端） |

### 4.2 用户端（`oj-friend`）

#### 4.2.1 `SendCodeDTO`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `email` | `String` | 是 | 邮箱（正则校验） |

#### 4.2.2 `UserLoginDTO`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `email` | `String` | 是 | 邮箱（正则校验） |
| `code` | `String` | 是 | 验证码（长度=6） |

#### 4.2.3 `UserEditDTO`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `avatar` | `String` | 否 | 头像URL |
| `nickName` | `String` | 否 | 昵称 |
| `gender` | `Integer` | 否 | 性别（具体枚举以业务为准） |
| `school` | `String` | 否 | 学校 |
| `major` | `String` | 否 | 专业 |
| `phone` | `String` | 否 | 手机号 |
| `wechat` | `String` | 否 | 微信号 |
| `introduce` | `String` | 否 | 个人简介 |

#### 4.2.4 `UserVO`

| 字段 | 类型 | 说明 |
|---|---|---|
| `userId` | `Long` | 用户ID（序列化为字符串避免前端精度丢失） |
| `nickName` | `String` | 昵称 |
| `avatar` | `String` | 头像 |
| `gender` | `Integer` | 性别 |
| `phone` | `String` | 电话 |
| `email` | `String` | 邮箱 |
| `wechat` | `String` | 微信 |
| `school` | `String` | 学校 |
| `major` | `String` | 专业 |
| `introduce` | `String` | 简介 |
| `status` | `Integer` | 状态（`1正常/0冻结`） |

#### 4.2.5 `QuestionQueryDTO`（继承 `PageQueryDTO`）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNum` | `Integer` | 否 | 页码（默认 1） |
| `pageSize` | `Integer` | 否 | 每页大小（默认 10） |
| `keyword` | `String` | 否 | 关键字（title/content 命中其一即可） |
| `difficulty` | `Integer` | 否 | 难度 |

#### 4.2.6 `QuestionQueryVO` / `QuestionVO`

`QuestionQueryVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `questionId` | `Long` | 题目ID（序列化为字符串） |
| `title` | `String` | 标题 |
| `difficulty` | `Integer` | 难度 |

`QuestionVO`（额外字段）：

| 字段 | 类型 | 说明 |
|---|---|---|
| `timeLimit` | `Integer` | 时间限制 |
| `spaceLimit` | `Integer` | 空间限制 |
| `content` | `String` | 题目内容 |
| `defaultCode` | `String` | 默认代码 |

#### 4.2.7 `UserSubmitDTO`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `examId` | `Long` | 否 | 竞赛ID（非竞赛练习可不传） |
| `questionId` | `Long` | 是 | 题目ID |
| `programType` | `Integer` | 是 | 语言类型（`0=Java`，`1=CPP`；当前仅支持 Java） |
| `userCode` | `String` | 是 | 用户代码（不含主函数，服务端会拼接 `mainFunc`） |

#### 4.2.8 `JudgeDTO`（跨服务）

| 字段 | 类型 | 说明 |
|---|---|---|
| `userId` | `Long` | 用户ID |
| `examId` | `Long` | 竞赛ID（可为空） |
| `questionId` | `Long` | 题目ID |
| `programType` | `Integer` | 语言 |
| `difficulty` | `Integer` | 难度 |
| `timeLimit` | `Long` | 时间限制 |
| `spaceLimit` | `Long` | 空间限制 |
| `userCode` | `String` | 待执行代码（已拼接 main 函数） |
| `inputList` | `List<String>` | 输入用例列表 |
| `outputList` | `List<String>` | 标准输出列表 |

#### 4.2.9 `UserQuestionResultVO` / `UserExeResult`（跨服务）

`UserQuestionResultVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `pass` | `Integer` | 通过状态：`0未通过/1通过/2请先提交/3判题中` |
| `exeMessage` | `String` | 执行信息（编译错误/超时/通过等） |
| `userExeResultList` | `List<UserExeResult>` | 用例对比详情 |
| `score` | `Integer` | 得分 |

`UserExeResult`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `input` | `String` | 输入 |
| `output` | `String` | 期望输出 |
| `exeOutput` | `String` | 实际输出 |

#### 4.2.10 `ExamQueryDTO` / `ExamRankDTO`

`ExamQueryDTO`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNum` | `Integer` | 否 | 页码 |
| `pageSize` | `Integer` | 否 | 每页大小 |
| `startTime` | `LocalDateTime` | 否 | 开始时间筛选（`yyyy-MM-dd HH:mm:ss`） |
| `endTime` | `LocalDateTime` | 否 | 结束时间筛选（`yyyy-MM-dd HH:mm:ss`） |
| `type` | `Integer` | 否 | 类型：`0未完赛/1历史/2我的` |

`ExamRankDTO`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNum` | `Integer` | 否 | 页码 |
| `pageSize` | `Integer` | 否 | 每页大小 |
| `examId` | `Long` | 是 | 竞赛ID |

#### 4.2.11 `ExamQueryVO` / `ExamRankVO`

`ExamQueryVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `examId` | `Long` | 竞赛ID（序列化为字符串） |
| `title` | `String` | 标题 |
| `startTime` | `LocalDateTime` | 开始时间 |
| `endTime` | `LocalDateTime` | 结束时间 |
| `enter` | `boolean` | 是否已报名 |

`ExamRankVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `userId` | `Long` | 用户ID |
| `score` | `Integer` | 得分 |
| `examRank` | `Integer` | 排名 |
| `nickName` | `String` | 昵称（通过用户缓存补齐） |

#### 4.2.12 `UserExamDTO`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `examId` | `Long` | 是 | 竞赛ID |

#### 4.2.13 `MessageTextVO`

| 字段 | 类型 | 说明 |
|---|---|---|
| `messageTextId` | `Long` | 消息文本ID |
| `title` | `String` | 标题 |
| `content` | `String` | 内容 |

#### 4.2.14 `OSSResult`

| 字段 | 类型 | 说明 |
|---|---|---|
| `name` | `String` | 文件URL |
| `success` | `boolean` | 是否上传成功 |

### 4.3 管理端（`oj-system`）

#### 4.3.1 `LoginDTO` / `SysUserDTO`

`LoginDTO`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `userAccount` | `String` | 是 | 管理员账号 |
| `password` | `String` | 是 | 管理员密码（长度 5~20） |

`SysUserDTO`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `userAccount` | `String` | 是 | 管理员账号 |
| `password` | `String` | 是 | 管理员密码（长度 5~20） |

#### 4.3.2 `UserQueryDTO` / `UserStatusDTO` / `UserQueryVO`

`UserQueryDTO`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNum` | `Integer` | 否 | 页码 |
| `pageSize` | `Integer` | 否 | 每页大小 |
| `userId` | `Long` | 否 | 用户ID筛选 |
| `nickName` | `String` | 否 | 昵称筛选 |

`UserStatusDTO`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `userId` | `Long` | 是 | 用户ID |
| `status` | `Integer` | 是 | 用户状态 |

`UserQueryVO`：

| 字段 | 类型 | 说明 |
|---|---|---|
| `userId` | `Long` | 用户ID（序列化为字符串） |
| `nickName` | `String` | 昵称 |
| `gender` | `Integer` | 性别 |
| `phone` | `String` | 电话 |
| `email` | `String` | 邮箱 |
| `wechat` | `String` | 微信 |
| `school` | `String` | 学校 |
| `major` | `String` | 专业 |
| `introduce` | `String` | 简介 |
| `status` | `Integer` | 状态 |

#### 4.3.3 `QuestionQueryDTO` / `QuestionAddDTO` / `QuestionEditDTO`

`QuestionQueryDTO`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNum` | `Integer` | 否 | 页码 |
| `pageSize` | `Integer` | 否 | 每页大小 |
| `title` | `String` | 否 | 标题筛选 |
| `difficulty` | `Integer` | 否 | 难度筛选 |
| `excludeIdSetStr` | `String` | 否 | 已选题目ID集合字符串（分隔符 `;`） |

`QuestionAddDTO`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `title` | `String` | 是 | 标题 |
| `content` | `String` | 是 | 内容 |
| `difficulty` | `Integer` | 是 | 难度（1~3） |
| `spaceLimit` | `Integer` | 是 | 空间限制（>=1） |
| `timeLimit` | `Integer` | 是 | 时间限制（>=1） |
| `questionCase` | `String` | 是 | 用例JSON字符串 |
| `defaultCode` | `String` | 是 | 默认代码 |
| `mainFunc` | `String` | 是 | 主函数代码 |

`QuestionEditDTO`（额外含 `questionId`）：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `questionId` | `Long` | 是 | 题目ID |
| `title` | `String` | 是 | 标题 |
| `content` | `String` | 是 | 内容 |
| `difficulty` | `Integer` | 是 | 难度 |
| `spaceLimit` | `Integer` | 是 | 空间限制 |
| `timeLimit` | `Integer` | 是 | 时间限制 |
| `questionCase` | `String` | 是 | 用例 |
| `defaultCode` | `String` | 是 | 默认代码 |
| `mainFunc` | `String` | 是 | 主函数 |

#### 4.3.4 `ExamQueryDTO` / `ExamAddDTO` / `ExamEditDTO` / `ExamQuestionDTO`

`ExamQueryDTO`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `pageNum` | `Integer` | 否 | 页码 |
| `pageSize` | `Integer` | 否 | 每页大小 |
| `startTime` | `LocalDateTime` | 否 | 开始时间筛选 |
| `endTime` | `LocalDateTime` | 否 | 结束时间筛选 |
| `title` | `String` | 否 | 标题筛选 |
| `status` | `Integer` | 否 | 状态筛选（`0未发布/1已发布`） |

`ExamAddDTO`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `title` | `String` | 是 | 标题 |
| `startTime` | `LocalDateTime` | 是 | 开始时间 |
| `endTime` | `LocalDateTime` | 是 | 结束时间 |

`ExamEditDTO`（新增基础上 + `examId`）：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `examId` | `Long` | 是 | 竞赛ID |

`ExamQuestionDTO`：

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `examId` | `Long` | 是 | 竞赛ID |
| `questionIds` | `LinkedHashSet<Long>` | 否 | 题目ID集合（保持插入顺序） |

## 5. HTTP 接口文档（按服务划分）

> 每个接口同时给出“服务直连URL”与“网关常见URL”。若你实际部署的网关路由不同，请以 Nacos 配置为准。

## 5.1 用户端接口（`oj-friend`）

### 5.1.1 用户认证与用户信息（`/user`）

#### 5.1.1.1 发送邮箱验证码

- 接口名称：发送验证码
- 接口类型：HTTP `POST`
- 所属服务：`oj-friend`
- URL（服务直连）：`POST /user/code`
- URL（网关常见）：`POST /friend/user/code`
- 鉴权：通常白名单（需在网关 `security.ignore.whiteList` 放行）
- 请求：`application/json` Body：`SendCodeDTO`
- 示例：

```json
{ "email": "user@example.com" }
```

- 响应：`Result<Void>`

```json
{ "code": 1000, "msg": "操作成功", "data": null }
```

- 业务链路（上下游）：
  - Redis：`email:code:<email>`、`code:counter:<email>`
  - 邮件：`EmailService` 发送验证码

#### 5.1.1.2 用户登录（邮箱 + 验证码）

- 接口名称：用户登录
- 接口类型：HTTP `POST`
- 所属服务：`oj-friend`
- URL（服务直连）：`POST /user/login`
- URL（网关常见）：`POST /friend/user/login`
- 鉴权：通常白名单
- 请求：`application/json` Body：`UserLoginDTO`
- 示例：

```json
{ "email": "user@example.com", "code": "123456" }
```

- 响应：`Result<String>`（JWT）

```json
{ "code": 1000, "msg": "操作成功", "data": "eyJhbGciOi..." }
```

- 业务链路（上下游）：
  - Redis：校验验证码并删除验证码 key
  - MySQL：`tb_user`（新用户首次登录自动创建默认用户）
  - Redis：写入登录态 `login:token:<userId>`

#### 5.1.1.3 退出登录

- 接口名称：退出登录
- 接口类型：HTTP `DELETE`
- 所属服务：`oj-friend`
- URL（服务直连）：`DELETE /user/logout`
- URL（网关常见）：`DELETE /friend/user/logout`
- 鉴权：需要登录（普通用户）
- 请求：Header `authentication: Bearer <jwt>`
- 响应：`Result<Void>`
- 业务链路：Redis 删除登录态 `login:token:<userId>`

#### 5.1.1.4 获取当前登录用户简要信息（从 token/redis）

- 接口名称：获取用户简要信息
- 接口类型：HTTP `GET`
- 所属服务：`oj-friend`
- URL（服务直连）：`GET /user/info`
- URL（网关常见）：`GET /friend/user/info`
- 鉴权：需要登录（普通用户）
- 响应：`Result<LoginUser>`

#### 5.1.1.5 获取当前登录用户详细信息（用户档案）

- 接口名称：获取用户详细信息
- 接口类型：HTTP `GET`
- 所属服务：`oj-friend`
- URL（服务直连）：`GET /user/detail`
- URL（网关常见）：`GET /friend/user/detail`
- 鉴权：需要登录（普通用户）
- 响应：`Result<UserVO>`
- 示例（结构示例）：

```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "userId": "123456789",
    "nickName": "用户昵称",
    "avatar": "https://...",
    "gender": 1,
    "phone": "13800000000",
    "email": "user@example.com",
    "wechat": "wxid_xxx",
    "school": "XX大学",
    "major": "计算机科学",
    "introduce": "个人简介",
    "status": 1
  }
}
```

- 业务链路：
  - Redis：`user:detail:<userId>`（命中/失效则 MySQL 刷新）
  - MySQL：`tb_user`

#### 5.1.1.6 修改用户信息

- 接口名称：修改用户信息
- 接口类型：HTTP `PUT`
- 所属服务：`oj-friend`
- URL（服务直连）：`PUT /user/edit`
- URL（网关常见）：`PUT /friend/user/edit`
- 鉴权：需要登录（普通用户）
- 请求：`application/json` Body：`UserEditDTO`
- 响应：`Result<Void>`
- 示例：

请求：

```json
{
  "nickName": "新昵称",
  "avatar": "https://...",
  "school": "XX大学",
  "major": "软件工程",
  "phone": "13800000000",
  "wechat": "wxid_xxx",
  "introduce": "更新后的简介",
  "gender": 1
}
```

响应：

```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": null
}
```

- 业务链路：
  - MySQL：更新 `tb_user`
  - Redis：刷新 `user:detail:<userId>`；更新 `login:token:<userId>` 中 `LoginUser`（昵称/头像）

#### 5.1.1.7 修改用户头像（仅头像）

- 接口名称：修改头像
- 接口类型：HTTP `POST`
- 所属服务：`oj-friend`
- URL（服务直连）：`POST /user/avatar/update`
- URL（网关常见）：`POST /friend/user/avatar/update`
- 鉴权：需要登录（普通用户）
- 请求：`application/json` Body：`UserEditDTO`（仅 `avatar` 字段）
- 响应：`Result<Void>`
- 示例：

请求：

```json
{ "avatar": "https://.../avatar.png" }
```

响应：

```json
{ "code": 1000, "msg": "操作成功", "data": null }
```

### 5.1.2 文件上传（`/file`）

#### 5.1.2.1 上传文件（头像/资源）

- 接口名称：上传文件
- 接口类型：HTTP `POST`
- 所属服务：`oj-friend`
- URL（服务直连）：`POST /file/upload`
- URL（网关常见）：`POST /friend/file/upload`
- 鉴权：需要登录（普通用户）；服务侧会按 `userId` 做每日上传次数限制
- 请求：`multipart/form-data`
  - `file`: `MultipartFile`
- 响应：`Result<OSSResult>`

### 5.1.3 题目查询（`/question`）

#### 5.1.3.1 题目列表（半登录/可匿名）

- 接口名称：题目列表查询
- 接口类型：HTTP `GET`
- 所属服务：`oj-friend`
- URL（服务直连）：`GET /question/semiLogin/list`
- URL（网关常见）：`GET /friend/question/semiLogin/list`
- 鉴权：通常白名单
- 请求：Query：`QuestionQueryDTO`
- 响应：`PageResult`（`rows` 为 `QuestionQueryVO`）
- 示例（结构示例）：

```json
{
  "total": 2,
  "rows": [
    { "questionId": "1", "title": "两数之和", "difficulty": 1 },
    { "questionId": "2", "title": "三数之和", "difficulty": 2 }
  ],
  "code": 1000,
  "msg": "操作成功"
}
```

- 业务链路：ES 查询；ES 无数据时从 MySQL `tb_question` 刷新到 ES

#### 5.1.3.2 题目详情

- 接口名称：题目详情
- 接口类型：HTTP `GET`
- 所属服务：`oj-friend`
- URL（服务直连）：`GET /question/detail`
- URL（网关常见）：`GET /friend/question/detail`
- 请求：Query：`questionId: Long`
- 响应：`Result<QuestionVO>`
- 示例（结构示例）：

```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "questionId": "1",
    "title": "两数之和",
    "difficulty": 1,
    "timeLimit": 1000,
    "spaceLimit": 262144,
    "content": "题目描述...",
    "defaultCode": "public class Main { ... }"
  }
}
```

#### 5.1.3.3 上一题 / 下一题（题目顺序）

- 接口名称：题目切换（上一题/下一题）
- 接口类型：HTTP `GET`
- 所属服务：`oj-friend`
- URL（服务直连）：`GET /question/pre`、`GET /question/next`
- URL（网关常见）：`GET /friend/question/pre`、`GET /friend/question/next`
- 请求：Query：`questionId: Long`
- 响应：`Result<String>`
- 示例：
  - `GET /friend/question/pre?questionId=2`

```json
{ "code": 1000, "msg": "操作成功", "data": "1" }
```

### 5.1.4 代码提交与判题触发（`/user/question`）

#### 5.1.4.1 提交代码（同步判题 / Feign 调用）

- 接口名称：提交代码（同步判题）
- 接口类型：HTTP `POST`
- 所属服务：`oj-friend`（入口） + `oj-judge`（判题）
- URL（服务直连）：`POST /user/question/submit`
- URL（网关常见）：`POST /friend/user/question/submit`
- 鉴权：
  - 需要登录（普通用户）
  - 限流：默认 `3 次 / 5 秒`（Redisson 令牌桶）
  - 状态校验：用户需为正常状态
- 请求：`application/json` Body：`UserSubmitDTO`
- 响应：`Result<UserQuestionResultVO>`
- 业务链路（上下游）：
  - `oj-friend`：构造 `JudgeDTO` → Redis 写 15s 占位 submitKey → Feign 调用 `oj-judge`
  - `oj-judge`：沙箱执行 → 判题 → MySQL `tb_user_submit` → Redis 写入结果与热题统计

#### 5.1.4.2 提交代码（异步判题 / RabbitMQ）

- 接口名称：提交代码（异步判题）
- 接口类型：HTTP `POST`
- URL（服务直连）：`POST /user/question/rabbit/submit`
- URL（网关常见）：`POST /friend/user/question/rabbit/submit`
- 鉴权：同 5.1.4.1
- 请求：`application/json` Body：`UserSubmitDTO`
- 响应：`Result<Void>`
- 示例：

请求：

```json
{
  "examId": 100,
  "questionId": 1,
  "programType": 0,
  "userCode": "class Solution { ... }"
}
```

响应：

```json
{ "code": 1000, "msg": "操作成功", "data": null }
```

#### 5.1.4.3 查询判题结果（轮询）

- 接口名称：查询判题结果
- 接口类型：HTTP `GET`
- URL（服务直连）：`GET /user/question/exe/result`
- URL（网关常见）：`GET /friend/user/question/exe/result`
- 请求（Query）：`questionId`（必填）、`examId`（可选）、`currentTime`（可选，`yyyy/MM/dd HH:mm:ss`）
- 响应：`Result<UserQuestionResultVO>`
- 示例（判题中）：

```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": { "pass": 3, "exeMessage": null, "userExeResultList": null, "score": null }
}
```

#### 5.1.4.4 热题榜（按提交数）

- 接口名称：热题榜
- 接口类型：HTTP `GET`
- URL（服务直连）：`GET /user/question/hot`
- URL（网关常见）：`GET /friend/user/question/hot`
- 请求（Query）：`top: Integer`
- 响应：`Result<List<QuestionQueryVO>>`
- 示例（结构示例）：

```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": [
    { "questionId": "1", "title": "两数之和", "difficulty": 1 },
    { "questionId": "2", "title": "三数之和", "difficulty": 2 }
  ]
}
```

### 5.1.5 竞赛（`/exam` 与 `/user/exam`）

#### 5.1.5.1 竞赛列表（DB）

- 接口名称：竞赛列表（DB）
- 接口类型：HTTP `GET`
- URL（服务直连）：`GET /exam/semiLogin/list`
- URL（网关常见）：`GET /friend/exam/semiLogin/list`
- 请求：Query：`ExamQueryDTO`
- 响应：`PageResult`（`rows` 为 `ExamQueryVO`）
- 示例（结构示例）：

```json
{
  "total": 1,
  "rows": [
    {
      "examId": "100",
      "title": "2026 春季算法竞赛",
      "startTime": "2026-01-20 09:00:00",
      "endTime": "2026-01-20 12:00:00",
      "enter": false
    }
  ],
  "code": 1000,
  "msg": "操作成功"
}
```

#### 5.1.5.2 竞赛列表（Redis 两级缓存）

- 接口名称：竞赛列表（Redis）
- 接口类型：HTTP `GET`
- URL（服务直连）：`GET /exam/semiLogin/redis/list`
- URL（网关常见）：`GET /friend/exam/semiLogin/redis/list`
- 请求：Query：`ExamQueryDTO`
- 响应：`PageResult`（结构同 5.1.5.1）

#### 5.1.5.3 竞赛首题 / 切换题目（上一题/下一题）

- 接口名称：竞赛题目导航
- 接口类型：HTTP `GET`
- URL（服务直连）：`GET /exam/getFirstQuestion`、`GET /exam/pre`、`GET /exam/next`
- URL（网关常见）：`GET /friend/exam/getFirstQuestion`、`GET /friend/exam/pre`、`GET /friend/exam/next`
- 请求：`examId`（必填），`questionId`（切换必填）
- 响应：`Result<String>`

#### 5.1.5.4 竞赛排行榜

- 接口名称：竞赛排行榜
- 接口类型：HTTP `GET`
- URL（服务直连）：`GET /exam/semiLogin/rank/list`
- URL（网关常见）：`GET /friend/exam/semiLogin/rank/list`
- 请求：Query：`ExamRankDTO`
- 响应：`PageResult`（`rows` 为 `ExamRankVO`）

#### 5.1.5.5 竞赛报名

- 接口名称：竞赛报名
- 接口类型：HTTP `POST`
- URL（服务直连）：`POST /user/exam/enter`
- URL（网关常见）：`POST /friend/user/exam/enter`
- 请求：`application/json` Body：`UserExamDTO`
- 响应：`Result<Void>`
- 示例：

```json
{ "examId": 100 }
```

#### 5.1.5.6 我的竞赛列表

- 接口名称：我的竞赛列表
- 接口类型：HTTP `GET`
- URL（服务直连）：`GET /user/exam/list`
- URL（网关常见）：`GET /friend/user/exam/list`
- 请求：Query：`ExamQueryDTO`（建议 `type=2`）
- 响应：`PageResult`（`rows` 为 `ExamQueryVO`）

### 5.1.6 消息通知（`/user/message`）

#### 5.1.6.1 我的消息列表

- 接口名称：我的消息列表
- 接口类型：HTTP `GET`
- URL（服务直连）：`GET /user/message/list`
- URL（网关常见）：`GET /friend/user/message/list`
- 请求：Query：`pageNum/pageSize`
- 响应：`PageResult`（`rows` 为 `MessageTextVO`）

## 5.2 管理端接口（`oj-system`）

### 5.2.1 管理员登录与会话（`/sys/user`）

#### 5.2.1.1 管理员登录

- 接口名称：管理员登录
- 接口类型：HTTP `POST`
- URL（服务直连）：`POST /sys/user/login`
- URL（网关常见）：`POST /system/sys/user/login`
- 请求：`application/json` Body：`LoginDTO`
- 响应：`Result<String>`（JWT）

#### 5.2.1.2 新增管理员

- 接口名称：新增管理员
- 接口类型：HTTP `POST`
- URL（服务直连）：`POST /sys/user/add`
- URL（网关常见）：`POST /system/sys/user/add`
- 请求：`application/json` Body：`SysUserDTO`
- 响应：`Result<Void>`

#### 5.2.1.3 获取管理员简要信息 / 退出登录

- `GET /sys/user/info`：`Result<LoginUser>`
- `DELETE /sys/user/logout`：`Result<Void>`

#### 5.2.1.4 其他接口（未实现）

以下接口在当前代码中返回 `null`（尚未实现）：

- `DELETE /sys/user/delete/{userId}`
- `PUT /sys/user/update`
- `GET /sys/user/detail`

### 5.2.2 C端用户管理（`/sys/cuser`）

- `GET /sys/cuser/list`：`PageResult`（`rows` 为 `UserQueryVO`）
- `PUT /sys/cuser/status/update`：`Result<Void>`（更新用户状态，并删除 `user:detail:<userId>` 缓存）

### 5.2.3 题库管理（`/sys/question`）

- `GET /sys/question/list`：`PageResult`
- `POST /sys/question/add`：`Result<Void>`（DB+ES+Redis 刷新）
- `GET /sys/question/detail`：`Result<QuestionVO>`
- `PUT /sys/question/edit`：`Result<Void>`（DB+ES 同步）
- `DELETE /sys/question/delete`：`Result<Void>`（DB+ES 删除 + `question:list` 移除）

### 5.2.4 竞赛管理（`/sys/exam`）

- `GET /sys/exam/list`：`PageResult`
- `POST /sys/exam/add`：`Result<ExamAddVO>`
- `POST /sys/exam/question/add`：`Result<Void>`（写 `tb_exam_question`）
- `GET /sys/exam/detail`：`Result<ExamVO>`
- `PUT /sys/exam/edit`：`Result<Void>`
- `DELETE /sys/exam/question/delete`：`Result<Void>`
- `DELETE /sys/exam/delete`：`Result<Void>`
- `PUT /sys/exam/publish`：`Result<Void>`（发布并写入用户端竞赛缓存）
- `PUT /sys/exam/publish/cancel`：`Result<Void>`（撤销发布并删除相关缓存）

## 5.3 判题服务接口（`oj-judge`，内部调用）

### 5.3.1 执行判题（Java）

- 接口名称：判题执行
- 接口类型：HTTP `POST`
- URL（服务直连）：`POST /judge/doJudgeJavaCode`
- 请求：`application/json` Body：`JudgeDTO`
- 响应：`Result<UserQuestionResultVO>`

## 6. 内部接口（Feign / MQ / Job）

### 6.1 Feign

- `RemoteJudgeService`：`POST /judge/doJudgeJavaCode`（`oj-friend` → `oj-judge`）

### 6.2 RabbitMQ

| 队列 | 消息体 | 生产者 | 消费者 | 说明 |
|---|---|---|---|---|
| `oj-work-queue` | `JudgeDTO` | `oj-friend` | `oj-judge` | 异步判题 |
| `message-cache-refresh-queue` | `Long userId` | `oj-friend` | `oj-friend` | 消息缓存异步刷新 |
| `exam-rank-cache-refresh-queue` | `Long examId` | `oj-friend` | `oj-friend` | 排行榜缓存异步刷新 |
| `exam-cache-refresh-queue` | - | - | - | 仅声明，当前未发现生产/消费实现 |

### 6.3 XXL-Job（`oj-job`）

- `examListOrganizeHandler`：定时刷新 `exam:unfinished:list`、`exam:history:list`、`exam:detail:<examId>`
- `examResultHandler`：统计竞赛成绩与排名，写入消息表与 Redis，并刷新 `exam:rank:list:<examId>`

