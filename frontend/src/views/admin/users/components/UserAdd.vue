<template>
  <div class="user-add">
    <UserEdit
      :user="null"
      @success="handleSuccess"
      @cancel="handleCancel"
    />
  </div>
</template>

<script setup>
import UserEdit from './UserEdit.vue'

// Emits
const emit = defineEmits(['success', 'cancel'])

// 处理成功
const handleSuccess = () => {
  emit('success')
}

// 处理取消
const handleCancel = () => {
  emit('cancel')
}
</script>

<style scoped>
.user-add {
  padding: 0;
}
</style>