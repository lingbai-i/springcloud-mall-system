# 令牌管理API

<cite>
**Referenced Files in This Document**  
- [AuthController.java](file://backend/auth-service/src/main/java/com/mall/auth/controller/AuthController.java)
- [JwtUtils.java](file://backend/auth-service/src/main/java/com/mall/auth/utils/JwtUtils.java)
- [RefreshTokenRequest.java](file://backend/auth-service/src/main/java/com/mall/auth/dto/RefreshTokenRequest.java)
- [LoginResponse.java](file://backend/auth-service/src/main/java/com/mall/auth/dto/LoginResponse.java)
- [application.yml](file://backend/auth-service/src/main/resources/application.yml)
</cite>

## 目录
1. [简介](#简介)
2. [令牌刷新API](#令牌刷新api)
3. [令牌登出API](#令牌登出api)
4. [JWT令牌机制](#jwt令牌机制)
5. [安全性考虑](#安全性考虑)
6. [客户端处理策略](#客户端处理策略)
7. [故障排除](#故障排除)

## 简介

令牌管理API是在线商城系统的核心安全组件，负责统一的身份认证、授权和令牌生命周期管理。本API文档详细描述了访问令牌和刷新令牌的管理机制，重点介绍基于`AuthController`中的`/refresh`和`/logout`端点的令牌刷新和登出功能。

认证服务采用JWT（JSON Web Token）技术实现无状态认证，通过访问令牌进行API请求认证，通过刷新令牌获取新的访问令牌。系统实现了完整的令牌生命周期管理，包括令牌生成、验证、刷新和失效机制，确保系统的安全性和用户体验。

**Section sources**
- [AuthController.java](file://backend/auth-service/src/main/java/com/mall/auth/controller/AuthController.java#L1-L31)
- [README.md](file://backend/auth-service/README.md#L1-L30)

## 令牌刷新API

### HTTP方法和URL路径

令牌刷新API使用POST方法，通过`/auth/refresh`端点实现。客户端需要向此端点发送包含刷新令牌的请求，以获取新的访问令牌。

```mermaid
sequenceDiagram
participant Client as "客户端"
participant AuthController as "AuthController"
participant AuthService as "AuthService"
participant JwtUtils as "JwtUtils"
participant Redis as "Redis"
Client->>AuthController : POST /auth/refresh
AuthController->>AuthService : refreshToken(refreshToken)
AuthService->>JwtUtils : validateToken(refreshToken)
JwtUtils-->>AuthService : 验证结果
AuthService->>Redis : 获取存储的刷新令牌
Redis-->>AuthService : 刷新令牌
AuthService->>JwtUtils : generateAccessToken(userId, username)
JwtUtils-->>AuthService : 新的访问令牌
AuthService-->>AuthController : LoginResponse
AuthController-->>Client : {accessToken, refreshToken, ...}
```

**Diagram sources**
- [AuthController.java](file://backend/auth-service/src/main/java/com/mall/auth/controller/AuthController.java#L78-L92)
- [AuthService.java](file://backend/auth-service/src/main/java/com/mall/auth/service/AuthService.java#L102-L138)

### 请求参数

令牌刷新请求需要在请求体中包含刷新令牌，采用JSON格式：

```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

对应的DTO定义如下：

```java
@Data
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest {
    @NotBlank(message = "刷新令牌不能为空")
    @Schema(description = "刷新令牌", required = true)
    private String refreshToken;
}
```

**Section sources**
- [RefreshTokenRequest.java](file://backend/auth-service/src/main/java/com/mall/auth/dto/RefreshTokenRequest.java#L1-L24)
- [AuthController.java](file://backend/auth-service/src/main/java/com/mall/auth/controller/AuthController.java#L80-L81)

### 响应格式

成功刷新令牌后，服务器返回包含新访问令牌的响应：

```json
{
  "success": true,
  "message": "令牌刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "userId": 1,
    "username": "admin"
  }
}
```

响应数据结构由`LoginResponse`类定义：

```java
@Data
@Builder
@Schema(description = "登录响应")
public class LoginResponse {
    @Schema(description = "访问令牌（用于API调用）")
    private String accessToken;
    
    @Schema(description = "刷新令牌（用于获取新的访问令牌）")
    private String refreshToken;
    
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";
    
    @Schema(description = "访问令牌有效期（秒）", example = "900")
    private Long expiresIn;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;
}
```

**Section sources**
- [LoginResponse.java](file://backend/auth-service/src/main/java/com/mall/auth/dto/LoginResponse.java#L1-L58)
- [AuthController.java](file://backend/auth-service/src/main/java/com/mall/auth/controller/AuthController.java#L84-L89)

### 刷新流程

令牌刷新流程如下：
1. 客户端发送包含刷新令牌的POST请求到`/auth/refresh`端点
2. 服务端验证刷新令牌的签名和有效期
3. 从令牌中提取用户ID和用户名
4. 在Redis中验证存储的刷新令牌是否匹配
5. 生成新的访问令牌
6. 返回包含新访问令牌的响应，刷新令牌保持不变

```mermaid
flowchart TD
Start([开始]) --> ValidateToken["验证刷新令牌签名和有效期"]
ValidateToken --> TokenValid{"令牌有效?"}
TokenValid --> |否| ReturnError["返回错误: 无效的刷新令牌"]
TokenValid --> |是| ExtractUserInfo["从令牌提取用户信息"]
ExtractUserInfo --> ValidateRedis["验证Redis中的刷新令牌"]
ValidateRedis --> RedisValid{"Redis令牌匹配?"}
RedisValid --> |否| ReturnError
RedisValid --> |是| GenerateNewToken["生成新的访问令牌"]
GenerateNewToken --> BuildResponse["构建响应"]
BuildResponse --> ReturnSuccess["返回成功响应"]
ReturnError --> End([结束])
ReturnSuccess --> End
```

**Diagram sources**
- [AuthService.java](file://backend/auth-service/src/main/java/com/mall/auth/service/AuthService.java#L102-L138)
- [JwtUtils.java](file://backend/auth-service/src/main/java/com/mall/auth/utils/JwtUtils.java#L153-L161)

## 令牌登出API

### HTTP方法和URL路径

令牌登出API使用POST方法，通过`/auth/logout`端点实现。客户端需要在请求头中包含访问令牌，以使当前会话失效。

```mermaid
sequenceDiagram
participant Client as "客户端"
participant AuthController as "AuthController"
participant AuthService as "AuthService"
participant JwtUtils as "JwtUtils"
participant Redis as "Redis"
Client->>AuthController : POST /auth/logout
AuthController->>AuthService : logout(token)
AuthService->>JwtUtils : getUserIdFromToken(token)
JwtUtils-->>AuthService : 用户ID
AuthService->>Redis : 将访问令牌加入黑名单
Redis-->>AuthService : 成功
AuthService->>Redis : 删除刷新令牌
Redis-->>AuthService : 成功
AuthService-->>AuthController : 登出成功
AuthController-->>Client : {success : true, message : "登出成功"}
```

**Diagram sources**
- [AuthController.java](file://backend/auth-service/src/main/java/com/mall/auth/controller/AuthController.java#L129-L147)
- [AuthService.java](file://backend/auth-service/src/main/java/com/mall/auth/service/AuthService.java#L156-L176)

### 请求参数

令牌登出请求需要在请求头中包含访问令牌：

```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

服务端从Authorization头中提取Bearer后的令牌字符串进行处理。

**Section sources**
- [AuthController.java](file://backend/auth-service/src/main/java/com/mall/auth/controller/AuthController.java#L132-L140)

### 响应格式

成功登出后，服务器返回成功响应：

```json
{
  "success": true,
  "message": "登出成功"
}
```

如果请求头中缺少有效的访问令牌，则返回错误响应：

```json
{
  "success": false,
  "message": "缺少有效的访问令牌"
}
```

**Section sources**
- [AuthController.java](file://backend/auth-service/src/main/java/com/mall/auth/controller/AuthController.java#L143-L145)

### 失效机制

登出时的令牌失效机制包括：
1. 将访问令牌加入Redis黑名单，有效期等于令牌剩余有效期
2. 从Redis中删除对应的刷新令牌
3. 后续使用已加入黑名单的访问令牌的请求将被拒绝

```mermaid
flowchart TD
Start([开始]) --> ExtractUserId["从令牌提取用户ID"]
ExtractUserId --> AddToBlacklist["将访问令牌加入Redis黑名单"]
AddToBlacklist --> SetBlacklistExpiry["设置黑名单有效期=令牌剩余有效期"]
SetBlacklistExpiry --> DeleteRefreshToken["删除Redis中的刷新令牌"]
DeleteRefreshToken --> LogSuccess["记录登出成功日志"]
LogSuccess --> End([结束])
```

**Diagram sources**
- [AuthService.java](file://backend/auth-service/src/main/java/com/mall/auth/service/AuthService.java#L156-L176)
- [JwtUtils.java](file://backend/auth-service/src/main/java/com/mall/auth/utils/JwtUtils.java#L170-L178)

## JWT令牌机制

### 令牌类型和有效期

系统使用两种类型的JWT令牌，具有不同的有效期和用途：

| 令牌类型 | 有效期 | 用途 | 存储位置 |
|---------|-------|------|---------|
| 访问令牌 | 15分钟 | API请求认证 | 内存/客户端 |
| 刷新令牌 | 7天 | 获取新的访问令牌 | Redis |

这些配置在`application.yml`文件中定义：

```yaml
# JWT配置
jwt:
  secret: mallSecretKeyForJWTTokenGenerationAndValidation2025ThisIsAVeryLongSecretKeyThatMeetsTheMinimumRequirementOf256BitsAndEvenMoreToEnsureSecurityCompliance
  access-token-expiration: 900000  # 15分钟
  refresh-token-expiration: 604800000  # 7天
  issuer: mall-auth-service
```

**Section sources**
- [application.yml](file://backend/auth-service/src/main/resources/application.yml#L33-L42)
- [README.md](file://backend/auth-service/README.md#L159-L167)

### 令牌生成和验证

`JwtUtils`类负责JWT令牌的生成和验证，主要方法包括：

- `generateAccessToken(userId, username)`: 生成访问令牌
- `generateRefreshToken(userId, username)`: 生成刷新令牌
- `validateToken(token)`: 验证令牌有效性
- `getUserIdFromToken(token)`: 从令牌提取用户ID
- `getUsernameFromToken(token)`: 从令牌提取用户名
- `getRemainingValidity(token)`: 获取令牌剩余有效期

```mermaid
classDiagram
class JwtUtils {
+String secret
+Long accessTokenExpiration
+Long refreshTokenExpiration
+String issuer
+String generateAccessToken(Long userId, String username)
+String generateRefreshToken(Long userId, String username)
+String generateToken(Map~String, Object~ claims, Long expiration)
+Claims parseToken(String token)
+Long getUserIdFromToken(String token)
+String getUsernameFromToken(String token)
+boolean validateToken(String token)
+long getRemainingValidity(String token)
+SecretKey getSigningKey()
}
class AuthService {
+JwtUtils jwtUtils
+RedisTemplate redisTemplate
+LoginResponse login(LoginRequest request)
+LoginResponse refreshToken(String refreshToken)
+boolean validateToken(String token)
+void logout(String token)
}
class AuthController {
+AuthService authService
+ResponseEntity~Map~ login(LoginRequest request)
+ResponseEntity~Map~ refreshToken(RefreshTokenRequest request)
+ResponseEntity~Map~ logout(String authorization)
}
AuthService --> JwtUtils : "使用"
AuthController --> AuthService : "调用"
```

**Diagram sources**
- [JwtUtils.java](file://backend/auth-service/src/main/java/com/mall/auth/utils/JwtUtils.java#L28-L189)
- [AuthService.java](file://backend/auth-service/src/main/java/com/mall/auth/service/AuthService.java#L46-L192)

## 安全性考虑

### 防止令牌滥用

系统通过多种机制防止令牌滥用：

1. **短期访问令牌**: 访问令牌有效期仅为15分钟，减少令牌泄露的风险
2. **刷新令牌存储**: 刷新令牌存储在Redis中，可以主动失效
3. **令牌黑名单**: 登出时将访问令牌加入黑名单，阻止后续使用
4. **签名验证**: 所有令牌都使用HS512算法签名，防止篡改

```mermaid
flowchart TD
A[令牌安全机制] --> B[短期访问令牌]
A --> C[刷新令牌存储]
A --> D[令牌黑名单]
A --> E[签名验证]
B --> F["访问令牌15分钟过期"]
C --> G["刷新令牌存储在Redis"]
D --> H["登出时加入黑名单"]
E --> I["HS512签名算法"]
```

**Section sources**
- [application.yml](file://backend/auth-service/src/main/resources/application.yml#L37-L40)
- [AuthService.java](file://backend/auth-service/src/main/java/com/mall/auth/service/AuthService.java#L160-L169)

### 配置安全

生产环境必须注意以下安全配置：

1. **密钥管理**: JWT密钥必须足够长且随机，建议使用256位以上的密钥
2. **Redis安全**: Redis应配置密码保护，避免未授权访问
3. **HTTPS**: 生产环境必须使用HTTPS传输令牌
4. **日志安全**: 避免在日志中记录完整的令牌

```bash
# 生成256位随机密钥
openssl rand -base64 64
```

**Section sources**
- [application.yml](file://backend/auth-service/src/main/resources/application.yml#L36)
- [README.md](file://backend/auth-service/README.md#L233-L235)

## 客户端处理策略

### 成功和失败场景

#### 令牌刷新成功场景
1. 客户端在访问令牌即将过期前（如剩余5分钟）发起刷新请求
2. 服务端验证刷新令牌并返回新的访问令牌
3. 客户端更新本地存储的访问令牌
4. 使用新令牌继续API调用

#### 令牌刷新失败场景
1. 刷新令牌已过期（超过7天未使用）
2. 刷新令牌已被使用过（防重放攻击）
3. 用户已登出，刷新令牌被删除
4. 服务端返回错误，客户端需要重新登录

### 处理令牌过期

客户端应实现以下策略处理令牌过期：

1. **预刷新机制**: 在访问令牌过期前主动刷新
2. **自动重试**: 遇到401错误时尝试刷新令牌并重试请求
3. **错误处理**: 刷新失败时引导用户重新登录
4. **状态管理**: 维护令牌状态，避免并发刷新

```mermaid
flowchart TD
A[API请求] --> B{响应状态}
B --> |200| C[成功处理]
B --> |401| D[令牌过期]
D --> E[尝试刷新令牌]
E --> F{刷新成功?}
F --> |是| G[重试原请求]
F --> |否| H[跳转登录页]
G --> I{重试成功?}
I --> |是| C
I --> |否| H
```

**Section sources**
- [AuthController.java](file://backend/auth-service/src/main/java/com/mall/auth/controller/AuthController.java#L80-L92)
- [AuthService.java](file://backend/auth-service/src/main/java/com/mall/auth/service/AuthService.java#L102-L138)

## 故障排除

### 常见问题

#### 令牌验证失败
- **可能原因**: JWT密钥配置不一致、令牌过期、Redis连接失败
- **解决方案**: 检查`application.yml`中的密钥配置，确认令牌未过期，验证Redis服务状态

#### Redis连接失败
- **可能原因**: Redis服务未运行、端口配置错误、密码不正确
- **解决方案**: 确认Redis服务已启动，检查`application.yml`中的host、port和password配置

#### 用户服务不可用
- **可能原因**: 用户服务未启动、网络问题、服务URL配置错误
- **解决方案**: 确认用户服务已启动，检查`application.yml`中的`user-service.url`配置

**Section sources**
- [application.yml](file://backend/auth-service/src/main/resources/application.yml#L11-L23)
- [README.md](file://backend/auth-service/README.md#L284-L296)