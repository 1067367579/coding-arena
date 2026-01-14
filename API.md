# Coding Arena - API接口文档

> 本文档涵盖所有C端用户接口和B端管理后台接口的详细说明

---

## 一、接口规范

### 1.1 统一响应格式

所有接口均返回 `Result<T>` 包装结构：

```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": { ... }
}
```

**分页响应格式** (`PageResult`)：

```json
{
  "total": 100,
  "rows": [ ... ],
  "code": 1000,
  "msg": "操作成功"
}
```

### 1.2 状态码说明

| 状态码 | 说明 |
|-------|------|
| 1000 | 操作成功 |
| 2000 | 服务繁忙，请稍后重试 |
| 3001 | 未授权访问 |
| 3002 | 令牌过期 |
| 3101 | 用户不存在 |
| 3102 | 用户状态异常 |
| 3103 | 提交过于频繁 |
| 3104 | 竞赛不在进行中 |
| 3105 | 题目不存在 |

### 1.3 认证方式

- **Header参数**：`Authorization: Bearer {token}`
- **白名单接口**：无需token即可访问
- **带@CheckRateLimiter接口**：需用户登录+限流检查
- **带@CheckUserStatus接口**：需用户登录+状态正常

---

## 二、用户认证模块 (/user)

### 2.1 发送验证码

**接口地址**：`POST /user/code`

**接口说明**：向指定邮箱发送6位数字验证码，用于注册或登录

**是否需要token**：否（白名单）

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| email | String | 是 | 邮箱地址（正则验证格式） |

**请求示例**：
```json
{
  "email": "user@example.com"
}
```

**响应示例**：
```json
{
  "code": 1000,
  "msg": "验证码发送成功",
  "data": null
}
```

**业务链路**：用户注册 → 发送验证码 → 验证并创建账户

**下游操作**：
- Redis存储验证码（5分钟过期）
- 调用邮件服务发送验证码

---

### 2.2 用户登录

**接口地址**：`POST /user/login`

**接口说明**：使用邮箱+验证码登录，登录成功返回JWT令牌

**是否需要token**：否（白名单）

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| email | String | 是 | 注册邮箱 |
| code | String | 是 | 6位验证码 |

**请求示例**：
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**响应示例**：
```json
{
  "code": 1000,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ...",
    "userInfo": {
      "userId": 123456,
      "username": "testuser",
      "nickname": "测试用户",
      "avatar": "https://xxx.com/avatar.png",
      "identity": 1
    }
  }
}
```

**业务链路**：验证验证码 → 查询用户 → 生成JWT → Redis存储令牌

**下游操作**：
- Redis存储用户令牌（24小时TTL）
- 记录登录日志

---

### 2.3 退出登录

**接口地址**：`DELETE /user/logout`

**接口说明**：用户退出登录，使当前令牌失效

**是否需要token**：是

**请求Header**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| Authorization | String | 是 | Bearer Token |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "退出成功",
  "data": null
}
```

**业务链路**：解析Token → Redis删除令牌 → 清理ThreadLocal

---

### 2.4 获取用户信息

**接口地址**：`GET /user/info`

**接口说明**：根据Token获取当前登录用户的简要信息

**是否需要token**：是

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "userId": 123456,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://xxx.com/avatar.png",
    "identity": 1
  }
}
```

---

### 2.5 获取用户详情

**接口地址**：`GET /user/detail`

**接口说明**：获取当前登录用户的详细信息

**是否需要token**：是

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "userId": 123456,
    "username": "testuser",
    "email": "user@example.com",
    "nickname": "测试用户",
    "avatar": "https://xxx.com/avatar.png",
    "identity": 1,
    "status": 1,
    "createTime": "2025-01-01 12:00:00"
  }
}
```

---

### 2.6 修改用户信息

**接口地址**：`PUT /user/edit`

**接口说明**：修改当前登录用户的昵称等信息

**是否需要token**：是

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| nickname | String | 否 | 新昵称（2-20字符） |

**请求示例**：
```json
{
  "nickname": "新昵称"
}
```

**响应示例**：
```json
{
  "code": 1000,
  "msg": "修改成功",
  "data": null
}
```

---

### 2.7 修改用户头像

**接口地址**：`POST /user/avatar/update`

**接口说明**：更新用户头像URL

**是否需要token**：是

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| avatar | String | 是 | 头像URL地址 |

**请求示例**：
```json
{
  "avatar": "https://xxx.com/new-avatar.png"
}
```

**响应示例**：
```json
{
  "code": 1000,
  "msg": "头像修改成功",
  "data": null
}
```

---

## 三、题目模块 (/question)

### 3.1 题目列表查询

**接口地址**：`GET /question/semiLogin/list`

**接口说明**：分页查询题目列表，支持关键词搜索和难度筛选

**是否需要token**：否（白名单）

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |
| keyword | String | 否 | 标题/内容关键字 |
| difficulty | Integer | 否 | 难度等级（1-5） |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "total": 100,
    "rows": [
      {
        "questionId": 1,
        "title": "两数之和",
        "difficulty": 1,
        "tags": ["简单", "数组"],
        "submitCount": 1520,
        "passRate": 0.65
      }
    ]
  }
}
```

**缓存策略**：
- Redis缓存题目ID列表（5分钟TTL）
- 首次请求从MySQL加载，后续从Redis读取

---

### 3.2 题目详情查询

**接口地址**：`GET /question/detail`

**接口说明**：根据题目ID获取题目详细信息

**是否需要token**：否

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| questionId | Long | 是 | 题目ID |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "questionId": 1,
    "title": "两数之和",
    "content": "给定一个整数数组 nums 和一个整数目标值 target...",
    "inputFormat": "第一行一个整数n，第二行n个整数",
    "outputFormat": "输出目标和的下标",
    "difficulty": 1,
    "score": 100,
    "timeLimit": 1000,
    "spaceLimit": 128,
    "sampleInput": "3\\n2 7 11 15\\n9",
    "sampleOutput": "0 1",
    "tags": ["简单", "数组", "哈希表"],
    "testCases": [
      {"input": "3\\n2 7 11 15\\n9", "output": "0 1"},
      {"input": "4\\n3 2 4 6\\n6", "output": "1 2"}
    ]
  }
}
```

---

### 3.3 上一题

**接口地址**：`GET /question/pre`

**接口说明**：获取当前题目的上一题ID

**是否需要token**：否

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| questionId | Long | 是 | 当前题目ID |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": "123"
}
```

**业务说明**：若当前题为首题，返回错误码3101

---

### 3.4 下一题

**接口地址**：`GET /question/next`

**接口说明**：获取当前题目的下一题ID

**是否需要token**：否

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| questionId | Long | 是 | 当前题目ID |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": "125"
}
```

---

## 四、代码提交模块 (/user/question)

### 4.1 同步提交代码

**接口地址**：`POST /user/question/submit`

**接口说明**：提交代码进行同步评测，实时返回评测结果

**是否需要token**：是（需登录+限流+状态检查）

**请求头**：
| 参数 | 说明 |
|-----|------|
| Authorization | Bearer Token |

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| questionId | Long | 是 | 题目ID |
| examId | Long | 否 | 竞赛ID（竞赛模式下必填） |
| programType | Integer | 是 | 编程语言（1=Java） |
| userCode | String | 是 | 用户代码 |

**请求示例**：
```json
{
  "questionId": 1,
  "examId": 100,
  "programType": 1,
  "userCode": "public class Solution { public int[] twoSum(int[] nums, int target) { ... } }"
}
```

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "pass": 1,
    "score": 100,
    "exeMessage": "通过",
    "userExeResultList": [
      {
        "input": "3\\n2 7 11 15\\n9",
        "output": "0 1",
        "exeOutput": "0 1",
        "status": "AC"
      },
      {
        "input": "4\\n3 2 4 6\\n6",
        "output": "1 2",
        "exeOutput": "1 2",
        "status": "AC"
      }
    ],
    "useMemory": 25600,
    "useTime": 15
  }
}
```

**限流规则**：
- 10次/分钟/用户
- 超过返回429错误

**业务链路**：
1. 限流检查 → 2. 用户状态检查 → 3. 获取Docker容器 → 4. 编译执行 → 5. 结果判定 → 6. 保存记录 → 7. 更新热榜

**下游操作**：
- 从容器池获取Docker容器
- 编译Java代码
- 执行测试用例
- 更新Redis缓存（结果+排名）
- 更新热榜（首次提交）

---

### 4.2 异步提交代码（MQ）

**接口地址**：`POST /user/question/rabbit/submit`

**接口说明**：提交代码到消息队列异步评测，立即返回提交ID

**是否需要token**：是（需登录+限流+状态检查）

**请求参数**：同4.1

**响应示例**：
```json
{
  "code": 1000,
  "msg": "提交成功",
  "data": {
    "submitId": 123456,
    "message": "评测中，请稍后查询结果"
  }
}
```

**业务链路**：
1. 限流检查 → 2. 用户状态检查 → 3. 发送MQ消息 → 4. 返回提交ID

**下游操作**：
- RabbitMQ发送评测任务
- oj-judge服务异步消费消息

---

### 4.3 获取评测结果

**接口地址**：`GET /user/question/exe/result`

**接口说明**：查询代码提交结果（用于异步提交后的结果查询）

**是否需要token**：是

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| questionId | Long | 是 | 题目ID |
| examId | Long | 否 | 竞赛ID |
| currentTime | String | 是 | 提交时间（格式：yyyy-MM-dd HH:mm:ss） |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "pass": 1,
    "score": 100,
    "exeMessage": "通过",
    "useTime": 15,
    "useMemory": 25600
  }
}
```

**缓存策略**：
- Redis缓存评测结果（1小时TTL）
- key: `submit:{userId}:{questionId}`

---

### 4.4 热榜查询

**接口地址**：`GET /user/question/hot`

**接口说明**：获取热门题目排行榜

**是否需要token**：否

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| top | Integer | 否 | 返回数量，默认10 |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": [
    {
      "questionId": 1,
      "title": "两数之和",
      "submitCount": 1520
    },
    {
      "questionId": 2,
      "title": "三数之和",
      "submitCount": 1200
    }
  ]
}
```

**数据来源**：Redis ZSet（score为提交次数）

---

## 五、竞赛模块 (/exam)

### 5.1 竞赛列表查询

**接口地址**：`GET /exam/semiLogin/list`

**接口说明**：分页查询竞赛列表，支持按时间筛选

**是否需要token**：否（白名单）

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |
| type | Integer | 否 | 类型：0=全部，1=进行中，2=已结束 |
| startTime | DateTime | 否 | 开始时间筛选 |
| endTime | DateTime | 否 | 结束时间筛选 |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "total": 20,
    "rows": [
      {
        "examId": 100,
        "title": "2025春季算法竞赛",
        "description": "春季编程大赛",
        "startTime": "2025-01-15 09:00:00",
        "endTime": "2025-01-15 12:00:00",
        "duration": 180,
        "status": 2,
        "participantCount": 500
      }
    ]
  }
}
```

---

### 5.2 竞赛排名查询

**接口地址**：`GET /exam/semiLogin/rank/list`

**接口说明**：分页查询竞赛排行榜

**是否需要token**：否

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| examId | Long | 是 | 竞赛ID |
| pageNum | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "total": 200,
    "rows": [
      {
        "rank": 1,
        "userId": 123,
        "nickname": "算法大师",
        "avatar": "https://xxx.com/1.png",
        "totalScore": 500,
        "submitCount": 5,
        "passCount": 5
      },
      {
        "rank": 2,
        "userId": 456,
        "nickname": "编程新手",
        "avatar": "https://xxx.com/2.png",
        "totalScore": 480,
        "submitCount": 6,
        "passCount": 4
      }
    ]
  }
}
```

**缓存策略**：
- Redis List缓存排名（实时刷新）
- 分页查询O(1)时间复杂度

---

### 5.3 获取竞赛首题

**接口地址**：`GET /exam/getFirstQuestion`

**接口说明**：获取竞赛的第一道题目ID

**是否需要token**：是

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| examId | Long | 是 | 竞赛ID |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": "1"
}
```

---

### 5.4 竞赛切换题目

**接口地址**：
- `GET /exam/pre` - 上一题
- `GET /exam/next` - 下一题

**是否需要token**：是

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| examId | Long | 是 | 竞赛ID |
| questionId | Long | 是 | 当前题目ID |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": "2"
}
```

---

## 六、用户参赛模块 (/user/exam)

### 6.1 竞赛报名

**接口地址**：`POST /user/exam/enter`

**接口说明**：用户报名参加竞赛

**是否需要token**：是（需登录+状态正常）

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| examId | Long | 是 | 竞赛ID |

**请求示例**：
```json
{
  "examId": 100
}
```

**响应示例**：
```json
{
  "code": 1000,
  "msg": "报名成功",
  "data": {
    "examId": 100,
    "joinTime": "2025-01-14 20:00:00",
    "examTitle": "2025春季算法竞赛"
  }
}
```

**业务校验**：
- 竞赛必须处于已发布状态
- 用户不能重复报名
- 竞赛必须在报名时间内

---

### 6.2 我的竞赛列表

**接口地址**：`GET /user/exam/list`

**接口说明**：查询当前用户报名的竞赛列表

**是否需要token**：是

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |
| type | Integer | 否 | 类型：1=进行中，2=已结束 |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "total": 5,
    "rows": [
      {
        "examId": 100,
        "title": "2025春季算法竞赛",
        "startTime": "2025-01-15 09:00:00",
        "endTime": "2025-01-15 12:00:00",
        "status": 1,
        "totalScore": 300,
        "rank": 15
      }
    ]
  }
}
```

---

## 七、消息通知模块 (/user/message)

### 7.1 我的消息列表

**接口地址**：`GET /user/message/list`

**接口说明**：分页查询当前用户的消息通知

**是否需要token**：是

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "total": 10,
    "rows": [
      {
        "messageId": 1,
        "type": 1,
        "title": "竞赛结果通知",
        "content": "恭喜您在2025春季算法竞赛中排名第15位",
        "isRead": 0,
        "createTime": "2025-01-15 13:00:00"
      }
    ]
  }
}
```

**消息类型**：
- 1: 系统通知
- 2: 竞赛结果
- 3: 题目讨论回复

---

## 八、管理后台接口 (/sys)

> 以下接口需要管理员身份访问，路径前缀 `/sys`

### 8.1 题目管理

#### 8.1.1 题目列表

**接口地址**：`GET /sys/question/list`

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |
| difficulty | Integer | 否 | 难度筛选 |
| keyword | String | 否 | 关键字搜索 |

**响应示例**：
```json
{
  "code": 1000,
  "msg": "操作成功",
  "data": {
    "total": 100,
    "rows": [
      {
        "questionId": 1,
        "title": "两数之和",
        "difficulty": 1,
        "status": 1,
        "createTime": "2025-01-01 12:00:00"
      }
    ]
  }
}
```

#### 8.1.2 新增题目

**接口地址**：`POST /sys/question/add`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| title | String | 是 | 标题 |
| content | String | 是 | 题目描述 |
| inputFormat | String | 是 | 输入格式 |
| outputFormat | String | 是 | 输出格式 |
| difficulty | Integer | 是 | 难度（1-5） |
| score | Integer | 否 | 分值，默认100 |
| timeLimit | Integer | 否 | 时间限制(ms)，默认1000 |
| spaceLimit | Integer | 否 | 空间限制(MB)，默认128 |
| sampleInput | String | 是 | 样例输入 |
| sampleOutput | String | 是 | 样例输出 |
| testCases | JSON | 是 | 测试用例数组 |
| tags | JSON | 是 | 标签数组 |

**请求示例**：
```json
{
  "title": "两数之和",
  "content": "给定一个整数数组...",
  "inputFormat": "第一行n，第二行数组",
  "outputFormat": "输出下标",
  "difficulty": 1,
  "score": 100,
  "timeLimit": 1000,
  "spaceLimit": 128,
  "sampleInput": "3\\n2 7 11 15\\n9",
  "sampleOutput": "0 1",
  "testCases": [
    {"input": "3\\n2 7 11 15\\n9", "output": "0 1"},
    {"input": "4\\n3 2 4 6\\n6", "output": "1 2"}
  ],
  "tags": ["简单", "数组"]
}
```

#### 8.1.3 编辑题目

**接口地址**：`PUT /sys/question/edit`

**请求参数**：同新增，需额外传入questionId

#### 8.1.4 删除题目

**接口地址**：`DELETE /sys/question/delete`

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| questionId | Long | 是 | 题目ID |

---

### 8.2 竞赛管理

#### 8.2.1 竞赛列表

**接口地址**：`GET /sys/exam/list`

#### 8.2.2 新增竞赛

**接口地址**：`POST /sys/exam/add`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| title | String | 是 | 竞赛标题 |
| description | String | 否 | 竞赛描述 |
| startTime | DateTime | 是 | 开始时间 |
| endTime | DateTime | 是 | 结束时间 |
| duration | Integer | 是 | 持续时间（分钟） |

#### 8.2.3 竞赛题目管理

**接口地址**：`POST /sys/exam/question/add`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| examId | Long | 是 | 竞赛ID |
| questionId | Long | 是 | 题目ID |
| questionOrder | Integer | 是 | 题目顺序 |
| questionScore | Integer | 是 | 题目分值 |

#### 8.2.4 发布竞赛

**接口地址**：`PUT /sys/exam/publish`

**请求参数**（Query）：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| examId | Long | 是 | 竞赛ID |

#### 8.2.5 撤销发布

**接口地址**：`PUT /sys/exam/publish/cancel`

---

### 8.3 用户管理

#### 8.3.1 用户列表

**接口地址**：`GET /sys/cuser/list`

#### 8.3.2 修改用户状态

**接口地址**：`PUT /sys/cuser/status/update`

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| userId | Long | 是 | 用户ID |
| status | Integer | 是 | 状态：1正常，2禁用 |

---

## 九、接口索引

| 模块 | 接口地址 | 方法 | 说明 | 是否需要Token |
|-----|---------|------|------|--------------|
| 用户 | `/user/code` | POST | 发送验证码 | 否 |
| 用户 | `/user/login` | POST | 用户登录 | 否 |
| 用户 | `/user/logout` | DELETE | 退出登录 | 是 |
| 用户 | `/user/info` | GET | 获取用户信息 | 是 |
| 用户 | `/user/detail` | GET | 获取用户详情 | 是 |
| 用户 | `/user/edit` | PUT | 修改用户信息 | 是 |
| 题目 | `/question/semiLogin/list` | GET | 题目列表 | 否 |
| 题目 | `/question/detail` | GET | 题目详情 | 否 |
| 提交 | `/user/question/submit` | POST | 同步提交代码 | 是 |
| 提交 | `/user/question/rabbit/submit` | POST | 异步提交代码 | 是 |
| 提交 | `/user/question/exe/result` | GET | 获取评测结果 | 是 |
| 提交 | `/user/question/hot` | GET | 热榜查询 | 否 |
| 竞赛 | `/exam/semiLogin/list` | GET | 竞赛列表 | 否 |
| 竞赛 | `/exam/semiLogin/rank/list` | GET | 竞赛排名 | 否 |
| 参赛 | `/user/exam/enter` | POST | 竞赛报名 | 是 |
| 参赛 | `/user/exam/list` | GET | 我的竞赛 | 是 |
| 消息 | `/user/message/list` | GET | 我的消息 | 是 |
| 管理 | `/sys/question/list` | GET | 题目列表 | 是(管理员) |
| 管理 | `/sys/question/add` | POST | 新增题目 | 是(管理员) |
| 管理 | `/sys/exam/list` | GET | 竞赛列表 | 是(管理员) |
| 管理 | `/sys/exam/publish` | PUT | 发布竞赛 | 是(管理员) |
| 管理 | `/sys/cuser/list` | GET | 用户列表 | 是(管理员) |

---

*文档更新时间：2026-01-14*
