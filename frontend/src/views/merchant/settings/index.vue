<!--
  商家设置中心占位页
  @author lingbai
  修改日志：
  V1.1 2025-11-09T20:53:01+08:00：新增占位组件以修复缺失文件导致的开发服务器启动失败；仅用于占位，后续可替换为真实设置页面。
-->
<template>
  <div class="merchant-settings">
    <el-card>
      <template #header>
        <div class="card-header">设置中心</div>
      </template>
      <p>该页面为占位实现，用于保证项目可启动。</p>
      <p>后续请替换为真实的商家设置功能。</p>
    </el-card>
  </div>
  
  <!--
    设计说明：
    - 为了不干扰当前任务（用户中心头像与资料更新），这里保持最小可运行实现。
    - 路由依赖该文件存在，否则 Vite 依赖扫描会报错并中断 dev server。
  -->
</template>

<script setup>
// 目前无需逻辑，仅占位以保障路由声明不报错
</script>

<style scoped>
.merchant-settings {
  padding: 16px;
}
.card-header {
  font-weight: 600;
}
</style>
