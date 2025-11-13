# 退款管理API

<cite>
**本文档引用文件**   
- [RefundController.java](file://backend/payment-service/src/main/java/com/mall/payment/controller/RefundController.java)
- [RefundCreateRequest.java](file://backend/payment-service/src/main/java/com/mall/payment/dto/request/RefundCreateRequest.java)
- [RefundOrderResponse.java](file://backend/payment-service/src/main/java/com/mall/payment/dto/response/RefundOrderResponse.java)
- [RefundStatus.java](file://backend/payment-service/src/main/java/com/mall/payment/enums/RefundStatus.java)
- [RefundServiceImpl.java](file://backend/payment-service/src/main/java/com/mall/payment/service/impl/RefundServiceImpl.java)
- [RefundService.java](file://backend/payment-service/src/main/java/com/mall/payment/service/RefundService.java)
- [RefundOrder.java](file://backend/payment-service/src/main/java/com/mall/payment/entity/RefundOrder.java)
- [RefundRecordResponse.java](file://backend/payment-service/src/main/java/com/mall/payment/dto/response/RefundRecordResponse.java)
- [RefundOrderRepository.java](file://backend/payment-service/src/main/java/com/mall/payment/repository/RefundOrderRepository.java)
</cite>

## 目录
1. [简介](#简介)
2. [核心API接口](#核心api接口)
3. [退款状态管理](#退款状态管理)
4. [退款审核流程](#退款审核流程)
5. [批量处理机制](#批量处理机制)
6. [错误处理与重试](#错误处理与重试)
7. [数据模型](#数据模型)

## 简介
退款管理API提供了一套完整的退款订单生命周期管理功能，支持用户申请、商家审核、系统自动处理等场景。该API基于支付服务模块实现，通过`RefundController`暴露RESTful接口，涵盖退款订单创建、处理、审核、状态查询等核心功能。系统支持多种退款类型，包括用户申请、系统自动和客服处理，并实现了完善的审核流程和状态流转机制。

**Section sources**
- [RefundController.java](file://backend/payment-service/src/main/java/com/mall/payment/controller/RefundController.java#L25-L468)
- [RefundService.java](file://backend/payment-service/src/main/java/com/mall/payment/service/RefundService.java#L53-L277)

## 核心API接口

### 创建退款订单
`createRefundOrder`接口用于创建新的退款订单，根据支付订单发起退款申请。

```mermaid
sequenceDiagram
participant 用户 as "用户"
participant 前端 as "前端应用"
participant RefundController as "RefundController"
participant RefundService as "RefundService"
participant 支付订单 as "PaymentOrder"
用户->>前端 : 提交退款申请
前端->>RefundController : POST /api/refund/orders
RefundController->>RefundService : createRefundOrder(request)
RefundService->>支付订单 : 查询支付订单
支付订单-->>RefundService : 返回支付订单
RefundService->>RefundService : 验证退款金额
RefundService->>RefundService : 创建退款订单
RefundService->>RefundController : 返回退款订单响应
RefundController->>前端 : 返回创建结果
前端->>用户 : 显示创建结果
```

**Diagram sources**
- [RefundController.java](file://backend/payment-service/src/main/java/com/mall/payment/controller/RefundController.java#L50-L78)
- [RefundServiceImpl.java](file://backend/payment-service/src/main/java/com/mall/payment/service/impl/RefundServiceImpl.java#L96-L117)

### 处理退款
`processRefund`接口用于发起实际的退款流程，调用第三方支付平台的退款接口。

```mermaid
sequenceDiagram
participant 管理员 as "管理员"
participant 前端 as "前端应用"
participant RefundController as "RefundController"
participant RefundService as "RefundService"
participant RefundChannelService as "RefundChannelService"
participant 第三方支付 as "第三方支付平台"
管理员->>前端 : 点击处理退款
前端->>RefundController : POST /api/refund/orders/{refundOrderId}/process
RefundController->>RefundService : processRefund(refundOrderId)
RefundService->>RefundService : 验证退款订单状态
RefundService->>RefundChannelService : 调用退款渠道服务
RefundChannelService->>第三方支付 : 发起退款请求
第三方支付-->>RefundChannelService : 返回处理结果
RefundChannelService-->>RefundService : 返回退款结果
RefundService->>RefundService : 更新退款订单状态
RefundService->>RefundController : 返回处理结果
RefundController->>前端 : 返回处理结果
前端->>管理员 : 显示处理结果
```

**Diagram sources**
- [RefundController.java](file://backend/payment-service/src/main/java/com/mall/payment/controller/RefundController.java#L218-L246)
- [RefundServiceImpl.java](file://backend/payment-service/src/main/java/com/mall/payment/service/impl/RefundServiceImpl.java#L184-L235)

### 审核退款
`auditRefund`接口用于对退款申请进行人工审核，决定是否同意退款。

```mermaid
sequenceDiagram
participant 审核员 as "审核员"
participant 前端 as "前端应用"
participant RefundController as "RefundController"
participant RefundService as "RefundService"
审核员->>前端 : 提交审核结果
前端->>RefundController : POST /api/refund/orders/{refundOrderId}/audit
RefundController->>RefundService : auditRefund(refundOrderId, approved, auditReason, auditorId)
RefundService->>RefundService : 验证退款订单状态
RefundService->>RefundService : 更新审核信息
RefundService->>RefundService : 保存审核记录
RefundService->>RefundController : 返回审核结果
RefundController->>前端 : 返回审核结果
前端->>审核员 : 显示审核结果
```

**Diagram sources**
- [RefundController.java](file://backend/payment-service/src/main/java/com/mall/payment/controller/RefundController.java#L258-L285)
- [RefundServiceImpl.java](file://backend/payment-service/src/main/java/com/mall/payment/service/impl/RefundServiceImpl.java#L255-L286)

### 状态查询
`queryRefundStatus`接口用于主动查询第三方支付平台的退款状态，实现状态同步。

```mermaid
sequenceDiagram
participant 系统 as "系统"
participant RefundController as "RefundController"
participant RefundService as "RefundService"
participant RefundChannelService as "RefundChannelService"
participant 第三方支付 as "第三方支付平台"
系统->>RefundController : GET /api/refund/orders/{refundOrderId}/status
RefundController->>RefundService : queryRefundStatus(refundOrderId)
RefundService->>RefundService : 查询本地退款订单
RefundService->>RefundChannelService : 查询第三方退款状态
RefundChannelService->>第三方支付 : 发起状态查询
第三方支付-->>RefundChannelService : 返回状态结果
RefundChannelService-->>RefundService : 返回查询结果
RefundService->>RefundService : 更新本地状态
RefundService->>RefundController : 返回最新状态
RefundController->>系统 : 返回状态信息
```

**Diagram sources**
- [RefundController.java](file://backend/payment-service/src/main/java/com/mall/payment/controller/RefundController.java#L333-L355)
- [RefundServiceImpl.java](file://backend/payment-service/src/main/java/com/mall/payment/service/impl/RefundServiceImpl.java#L379-L422)

## 退款状态管理

### RefundStatus枚举值
`RefundStatus`枚举定义了退款订单的各种状态，用于跟踪退款流程的进展。

| 状态代码 | 状态描述 | 说明 |
|---------|---------|------|
| PENDING | 待审核 | 退款申请已提交，等待审核 |
| REVIEWING | 审核中 | 退款申请正在审核中 |
| PENDING_REVIEW | 待人工审核 | 需要人工介入审核的退款申请 |
| APPROVED | 审核通过 | 退款申请审核通过，准备处理 |
| REJECTED | 审核拒绝 | 退款申请被拒绝 |
| PROCESSING | 处理中 | 退款正在处理中，已向第三方发起退款 |
| SUCCESS | 退款成功 | 退款已成功，资金已退回 |
| FAILED | 退款失败 | 退款处理失败 |
| CANCELLED | 已取消 | 退款申请被取消 |
| EXCEPTION | 异常 | 退款过程中出现异常，需要人工处理 |

**Section sources**
- [RefundStatus.java](file://backend/payment-service/src/main/java/com/mall/payment/enums/RefundStatus.java#L38-L207)

### 状态流转规则
退款订单的状态遵循特定的流转规则，确保业务流程的正确性。

```mermaid
graph TD
PENDING[待审核] --> REVIEWING[审核中]
REVIEWING --> APPROVED[审核通过]
REVIEWING --> REJECTED[审核拒绝]
REVIEWING --> PENDING_REVIEW[待人工审核]
APPROVED --> PROCESSING[处理中]
PROCESSING --> SUCCESS[退款成功]
PROCESSING --> FAILED[退款失败]
PROCESSING --> EXCEPTION[异常]
FAILED --> PROCESSING[重试]
EXCEPTION --> PROCESSING[人工处理后]
PENDING --> CANCELLED[已取消]
REVIEWING --> CANCELLED[已取消]
APPROVED --> CANCELLED[已取消]
EXCEPTION --> CANCELLED[已取消]
```

**Diagram sources**
- [RefundStatus.java](file://backend/payment-service/src/main/java/com/mall/payment/enums/RefundStatus.java#L189-L205)
- [RefundOrder.java](file://backend/payment-service/src/main/java/com/mall/payment/entity/RefundOrder.java#L290-L322)

## 退款审核流程

### 审核流程说明
退款审核流程支持自动审核和人工审核两种模式，确保退款申请的合规性。

```mermaid
flowchart TD
A[用户提交退款申请] --> B{是否符合自动审核条件?}
B --> |是| C[系统自动审核通过]
B --> |否| D[人工审核]
D --> E{审核结果}
E --> |通过| F[审核通过]
E --> |拒绝| G[审核拒绝]
C --> H[进入处理流程]
F --> H
G --> I[通知用户审核结果]
H --> J[调用第三方退款接口]
J --> K{退款结果}
K --> |成功| L[更新为退款成功]
K --> |失败| M[更新为退款失败]
M --> N{是否可重试?}
N --> |是| O[标记为可重试]
N --> |否| P[标记为终态]
```

**Diagram sources**
- [RefundServiceImpl.java](file://backend/payment-service/src/main/java/com/mall/payment/service/impl/RefundServiceImpl.java#L486-L509)
- [RefundController.java](file://backend/payment-service/src/main/java/com/mall/payment/controller/RefundController.java#L258-L285)

### 审核权限控制
系统通过`@RequirePermission`注解实现审核权限控制，确保只有授权用户才能执行审核操作。

```mermaid
classDiagram
class RefundController {
+auditRefund(refundOrderId, approved, auditReason, auditorId)
}
class RequirePermission {
+value : String[]
+allowOwner : boolean
+ownerField : String
}
class PermissionAspect {
+checkPermission(joinPoint, requirePermission)
}
RefundController --> RequirePermission : "使用"
PermissionAspect --> RequirePermission : "处理"
```

**Diagram sources**
- [RefundController.java](file://backend/payment-service/src/main/java/com/mall/payment/controller/RefundController.java#L258-L285)
- [RequirePermission.java](file://backend/payment-service/src/main/java/com/mall/payment/annotation/RequirePermission.java)
- [PermissionAspect.java](file://backend/payment-service/src/main/java/com/mall/payment/aspect/PermissionAspect.java)

## 批量处理机制

### 批量处理流程
系统提供批量处理待审核退款申请的功能，通过定时任务自动处理符合条件的退款申请。

```mermaid
sequenceDiagram
participant 定时任务 as "定时任务"
participant RefundController as "RefundController"
participant RefundService as "RefundService"
定时任务->>RefundController : POST /api/refund/batch-process
RefundController->>RefundService : batchProcessPendingRefunds()
RefundService->>RefundService : 查询待审核退款订单
loop 遍历每个待审核订单
RefundService->>RefundService : 判断是否自动通过
RefundService->>RefundService : 执行自动审核
end
RefundService->>RefundController : 返回处理数量
RefundController->>定时任务 : 返回批量处理结果
```

**Diagram sources**
- [RefundController.java](file://backend/payment-service/src/main/java/com/mall/payment/controller/RefundController.java#L435-L455)
- [RefundServiceImpl.java](file://backend/payment-service/src/main/java/com/mall/payment/service/impl/RefundServiceImpl.java#L486-L509)

### 批量处理规则
批量处理遵循预设的业务规则，确保自动审核的准确性和安全性。

```mermaid
flowchart TD
A[开始批量处理] --> B[查询待审核退款订单]
B --> C{订单是否存在?}
C --> |否| D[返回处理数量0]
C --> |是| E[遍历每个订单]
E --> F{是否符合自动通过条件?}
F --> |是| G[执行自动审核通过]
F --> |否| H[跳过处理]
G --> I[更新处理计数]
H --> I
I --> J{是否还有更多订单?}
J --> |是| E
J --> |否| K[返回处理数量]
```

**Diagram sources**
- [RefundServiceImpl.java](file://backend/payment-service/src/main/java/com/mall/payment/service/impl/RefundServiceImpl.java#L486-L509)

## 错误处理与重试

### 错误处理机制
系统实现了完善的错误处理机制，确保退款流程的稳定性和可靠性。

```mermaid
flowchart TD
A[处理退款] --> B{处理成功?}
B --> |是| C[更新为成功状态]
B --> |否| D[记录失败原因]
D --> E{是否可重试?}
E --> |是| F[标记为可重试]
E --> |否| G[标记为终态]
F --> H[等待重试]
H --> I[重试处理]
I --> B
```

**Diagram sources**
- [RefundServiceImpl.java](file://backend/payment-service/src/main/java/com/mall/payment/service/impl/RefundServiceImpl.java#L184-L248)
- [RefundOrder.java](file://backend/payment-service/src/main/java/com/mall/payment/entity/RefundOrder.java#L379-L382)

### 重试机制
系统支持失败退款订单的重试功能，提高退款成功率。

```mermaid
sequenceDiagram
participant 用户 as "用户"
participant 前端 as "前端应用"
participant RefundController as "RefundController"
participant RefundService as "RefundService"
用户->>前端 : 请求重试退款
前端->>RefundController : POST /api/refund/orders/{refundOrderId}/retry
RefundController->>RefundService : retryRefund(refundOrderId)
RefundService->>RefundService : 验证退款订单状态
RefundService->>RefundService : 验证重试次数
RefundService->>RefundService : 重新处理退款
RefundService->>RefundController : 返回重试结果
RefundController->>前端 : 返回重试结果
前端->>用户 : 显示重试结果
```

**Diagram sources**
- [RefundController.java](file://backend/payment-service/src/main/java/com/mall/payment/controller/RefundController.java#L364-L384)
- [RefundServiceImpl.java](file://backend/payment-service/src/main/java/com/mall/payment/service/impl/RefundServiceImpl.java#L427-L445)

## 数据模型

### 退款订单实体
`RefundOrder`实体类存储退款申请的基本信息，包括退款金额、退款原因、审核状态等。

```mermaid
classDiagram
class RefundOrder {
-id : String
-refundNo : String
-paymentOrderId : String
-userId : String
-refundAmount : BigDecimal
-refundReason : String
-refundType : Integer
-status : RefundStatus
-reviewerId : String
-reviewTime : LocalDateTime
-reviewRemark : String
-processorId : String
-processTime : LocalDateTime
-refundTime : LocalDateTime
-actualRefundAmount : BigDecimal
-refundFee : BigDecimal
-thirdPartyRefundNo : String
-failureReason : String
-retryCount : Integer
-expectedArrivalTime : LocalDateTime
-refundVoucher : String
-remark : String
-createdAt : LocalDateTime
-updatedAt : LocalDateTime
+isSuccess() : boolean
+isFailed() : boolean
+canCancel() : boolean
+needsManualProcess() : boolean
+isFinalStatus() : boolean
+updateStatus(newStatus) : void
+approve(reviewerId, reviewRemark) : void
+reject(reviewerId, reviewRemark) : void
+startProcess(processorId) : void
+success(actualAmount, thirdPartyRefundNo) : void
+fail(failureReason) : void
+incrementRetryCount() : void
+getProcessDurationHours() : Long
}
class RefundStatus {
+PENDING
+REVIEWING
+PENDING_REVIEW
+APPROVED
+REJECTED
+PROCESSING
+SUCCESS
+FAILED
+CANCELLED
+EXCEPTION
}
RefundOrder --> RefundStatus : "使用"
```

**Diagram sources**
- [RefundOrder.java](file://backend/payment-service/src/main/java/com/mall/payment/entity/RefundOrder.java#L77-L572)
- [RefundStatus.java](file://backend/payment-service/src/main/java/com/mall/payment/enums/RefundStatus.java#L38-L207)

### 请求响应模型
系统定义了标准化的请求和响应DTO，确保API接口的规范性和一致性。

```mermaid
classDiagram
class RefundCreateRequest {
-paymentOrderId : String
-userId : String
-refundAmount : BigDecimal
-refundReason : String
-refundType : Integer
-contactPhone : String
-contactEmail : String
-refundVoucher : String
-remark : String
-urgent : Boolean
-clientIp : String
+isValidRefundAmount() : boolean
+isValidRefundType() : boolean
+isUserApplied() : boolean
+isSystemAuto() : boolean
+isCustomerService() : boolean
+getFormattedRefundAmount() : String
+getRefundTypeDescription() : String
+hasValidContact() : boolean
}
class RefundOrderResponse {
-id : String
-refundNo : String
-paymentOrderId : String
-userId : String
-refundAmount : BigDecimal
-refundReason : String
-refundType : Integer
-status : RefundStatus
-reviewerId : String
-reviewTime : LocalDateTime
-reviewRemark : String
-processorId : String
-processTime : LocalDateTime
-refundTime : LocalDateTime
-actualRefundAmount : BigDecimal
-refundFee : BigDecimal
-thirdPartyRefundNo : String
-failureReason : String
-retryCount : Integer
-expectedArrivalTime : LocalDateTime
-refundVoucher : String
-remark : String
-createdAt : LocalDateTime
-updatedAt : LocalDateTime
-refundRecords : List<RefundRecordResponse>
-canCancel : Boolean
-needsManualProcess : Boolean
-finalStatus : Boolean
-processDurationHours : Long
+getFormattedRefundAmount() : String
+getFormattedActualRefundAmount() : String
+getFormattedRefundFee() : String
+getRefundTypeDescription() : String
+getFormattedProcessDuration() : String
+isSuccess() : boolean
+isFailed() : boolean
+isProcessing() : boolean
+isPendingReview() : boolean
+isApproved() : boolean
+isRejected() : boolean
+isUserApplied() : boolean
+isSystemAuto() : boolean
+isCustomerService() : boolean
+getProgressPercentage() : int
+getNextActionHint() : String
+getExpectedArrivalDescription() : String
}
class RefundRecordResponse {
-id : String
-refundOrderId : String
-refundAmount : BigDecimal
-status : RefundStatus
-statusDesc : String
-thirdPartyRefundNo : String
-refundChannel : String
-refundTime : LocalDateTime
-actualRefundAmount : BigDecimal
-refundFee : BigDecimal
-errorCode : String
-errorMessage : String
-retryCount : Integer
-operatorId : String
-operatorType : Integer
-operatorTypeDesc : String
-expectedArrivalTime : LocalDateTime
-refundVoucher : String
-remark : String
-createdAt : LocalDateTime
-updatedAt : LocalDateTime
-refundDuration : Long
+getFormattedRefundAmount() : String
+getFormattedActualRefundAmount() : String
+getFormattedRefundFee() : String
+getFormattedRefundDuration() : String
+getOperatorTypeDescription() : String
+isSuccess() : boolean
+isFailed() : boolean
+isProcessing() : boolean
+hasError() : boolean
+getFullErrorMessage() : String
+canRetry(maxRetryCount) : boolean
+isSystemAutoRefund() : boolean
+isManualRefund() : boolean
+isUserAppliedRefund() : boolean
+getRefundChannelDisplayName() : String
+getExpectedArrivalDescription() : String
+getStatusColor() : String
}
RefundOrderResponse --> RefundRecordResponse : "包含"
RefundCreateRequest --> RefundStatus : "使用"
RefundOrderResponse --> RefundStatus : "使用"
```

**Diagram sources**
- [RefundCreateRequest.java](file://backend/payment-service/src/main/java/com/mall/payment/dto/request/RefundCreateRequest.java#L16-L226)
- [RefundOrderResponse.java](file://backend/payment-service/src/main/java/com/mall/payment/dto/response/RefundOrderResponse.java#L18-L566)
- [RefundRecordResponse.java](file://backend/payment-service/src/main/java/com/mall/payment/dto/response/RefundRecordResponse.java#L17-L368)
- [RefundStatus.java](file://backend/payment-service/src/main/java/com/mall/payment/enums/RefundStatus.java#L38-L207)