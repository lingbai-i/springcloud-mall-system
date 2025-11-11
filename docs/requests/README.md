# Mall API 请求集合使用说明

> 生成时间: 2025-11-09T22:11:30+08:00 (Asia/Shanghai)

本目录提供 Postman/Thunder Client 集合以便快速验证接口。集合文件：`mall-api.postman_collection.json`。

## 目录结构

- `mall-api.postman_collection.json`：可直接导入 Postman 或 Thunder Client 的集合文件。

## 环境变量

- `gateway_url` 默认 `http://localhost:8080`（Gateway 服务）
- `sms_service_url` 默认 `http://localhost:8083`（SMS 服务）
- `frontend_dev_url` 默认 `http://localhost:5173`（前端开发服务器，用于代理到 SMS）

可在 Postman 的环境或 Thunder 的全局变量中覆盖以上变量。

## 端点说明

### SMS

- `POST {{gateway_url}}/api/sms/send`：通过网关发送验证码。
- `POST {{gateway_url}}/api/sms/verify`：通过网关验证验证码。
- `POST {{sms_service_url}}/sms/send`：直连 GatewaySmsController 发送验证码。
- `POST {{sms_service_url}}/send`：直连 SmsController 发送验证码。
- `POST {{sms_service_url}}/sms/verify`：直连 GatewaySmsController 验证验证码。
- `POST {{sms_service_url}}/verify`：直连 SmsController 验证验证码。
- `GET  {{sms_service_url}}/sms/health`：健康检查。
- `POST {{frontend_dev_url}}/api/sms/send`：通过前端开发代理发送验证码（需启动前端 dev server）。

### User（占位）

- `POST {{gateway_url}}/api/users/register`：用户注册占位接口；具体字段以后端实现为准。
- `POST {{gateway_url}}/api/users/login`：用户登录占位接口；返回值字段以后端实现为准。
- `GET  {{gateway_url}}/api/users/test`：网关测试端点。

## 使用步骤

1. 启动相关服务：`start-services.ps1` 或分别启动 `gateway-service`、`sms-service`、`frontend`。
2. 在 Postman/Thunder 中导入 `mall-api.postman_collection.json`。
3. 配置环境变量（如端口不同）。
4. 选择请求并发送，查看响应状态与体内容。

## DTO 字段参考

- 发送验证码请求体：
  - `phoneNumber`：字符串，必须满足正则 `^1[3-9]\d{9}$`。
  - `purpose`：字符串，使用业务枚举值，如 `REGISTER`。
- 验证验证码请求体：
  - `phoneNumber`：同上。
  - `code`：字符串，验证码。
  - `purpose`：同上。

## 变更日志

- V1.0 2025-11-09：初始提交，涵盖 SMS 及用户占位接口。
- V1.1 2025-11-09T22:11:30+08:00：修正前端开发端口为 5173，并同步代理说明。

## 注意事项

- 若实际用户服务控制器路径与占位不同，请更新集合并在本 README 同步说明。
- 建议统一通过网关进行外部访问以保证安全与限流策略生效。
