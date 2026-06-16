<template>
  <div class="page-container">
    <a-card :bordered="false" class="page-card">
      <template #title>
        <div class="card-header">
          <div class="card-title">
            <icon-user class="title-icon" />
            <span>人才库</span>
          </div>
          <a-button type="primary" @click="handleAdd">
            <template #icon>
              <icon-plus />
            </template>
            新增人才
          </a-button>
        </div>
      </template>
      <a-table
        :data="talentList"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1400 }"
        bordered
        row-key="id"
      >
        <template #columns>
          <a-table-column title="姓名" data-index="name" :width="100" />
          <a-table-column title="性别" data-index="gender" :width="80" />
          <a-table-column title="手机" data-index="phone" :width="130" />
          <a-table-column title="邮箱" data-index="email" :width="180" />
          <a-table-column title="职级" :width="100">
            <template #cell="{ record }">
              <a-tag :color="levelColor(record.level)">{{ levelText(record.level) }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="月薪" :width="120">
            <template #cell="{ record }">
              {{ record.monthlySalary ? `¥${Number(record.monthlySalary).toLocaleString()}` : '-' }}
            </template>
          </a-table-column>
          <a-table-column title="技术栈">
            <template #cell="{ record }">
              <template v-if="record.techStack">
                <a-tag
                  v-for="(tag, idx) in parseTags(record.techStack)"
                  :key="idx"
                  color="arcoblue"
                  style="margin-bottom: 4px;"
                >
                  {{ tag }}
                </a-tag>
              </template>
              <span v-else>-</span>
            </template>
          </a-table-column>
          <a-table-column title="状态" :width="140">
            <template #cell="{ record }">
              <a-dropdown @select="(val) => handleStatusChange(record, val)">
                <a-tag
                  :color="statusColor(record.status)"
                  style="cursor: pointer;"
                >
                  {{ statusText(record.status) }}
                  <icon-down style="margin-left: 4px; font-size: 12px;" />
                </a-tag>
                <template #content>
                  <a-doption value="IDLE">待岗</a-doption>
                  <a-doption value="ON_PROJECT">在项</a-doption>
                  <a-doption value="LEFT">离职</a-doption>
                </template>
              </a-dropdown>
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="140" fixed="right">
            <template #cell="{ record }">
              <a-button type="text" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm
                content="确定要删除该人才档案吗？"
                @ok="handleDelete(record)"
              >
                <a-button type="text" size="small" status="danger">删除</a-button>
              </a-popconfirm>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:visible="modalVisible"
      :title="isEdit ? '编辑人才' : '新增人才'"
      @ok="handleSubmit"
      @cancel="modalVisible = false"
      :ok-loading="submitting"
      width="560px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        layout="vertical"
        :rules="formRules"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="name" label="姓名">
              <a-input v-model="formData.name" placeholder="请输入姓名" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="gender" label="性别">
              <a-select v-model="formData.gender" placeholder="请选择性别">
                <a-option value="男">男</a-option>
                <a-option value="女">女</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="phone" label="手机">
              <a-input v-model="formData.phone" placeholder="请输入手机号码" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="email" label="邮箱">
              <a-input v-model="formData.email" placeholder="请输入邮箱" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="level" label="职级">
              <a-select v-model="formData.level" placeholder="请选择职级">
                <a-option value="JUNIOR">初级</a-option>
                <a-option value="MIDDLE">中级</a-option>
                <a-option value="SENIOR">高级</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="monthlySalary" label="月薪（元）">
              <a-input-number
                v-model="formData.monthlySalary"
                :min="0"
                :precision="0"
                placeholder="请输入月薪"
                style="width: 100%;"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item field="techStack" label="技术栈（多个用逗号分隔）">
          <a-input v-model="formData.techStack" placeholder="如：Java,SpringBoot,Vue" />
        </a-form-item>
        <a-form-item field="status" label="状态">
          <a-select v-model="formData.status" placeholder="请选择状态">
            <a-option value="IDLE">待岗</a-option>
            <a-option value="ON_PROJECT">在项</a-option>
            <a-option value="LEFT">离职</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="remark" label="备注">
          <a-textarea v-model="formData.remark" :auto-size="{ minRows: 2, maxRows: 4 }" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import {
  IconUser,
  IconPlus,
  IconDown
} from '@arco-design/web-vue/es/icon'
import {
  getTalentListApi,
  addTalentApi,
  updateTalentApi,
  updateTalentStatusApi,
  deleteTalentApi
} from '@/api/talent'

const loading = ref(false)
const talentList = ref([])
const modalVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const defaultForm = () => ({
  name: '',
  gender: '',
  phone: '',
  email: '',
  level: '',
  monthlySalary: undefined,
  techStack: '',
  status: 'IDLE',
  remark: ''
})

const formData = reactive(defaultForm())

const formRules = {
  name: [{ required: true, message: '请输入姓名' }],
  level: [{ required: true, message: '请选择职级' }],
  status: [{ required: true, message: '请选择状态' }]
}

function levelText(level) {
  const map = { JUNIOR: '初级', MIDDLE: '中级', SENIOR: '高级' }
  return map[level] || level
}

function levelColor(level) {
  const map = { JUNIOR: 'green', MIDDLE: 'blue', SENIOR: 'orangered' }
  return map[level] || 'gray'
}

function statusText(status) {
  const map = { IDLE: '待岗', ON_PROJECT: '在项', LEFT: '离职' }
  return map[status] || status
}

function statusColor(status) {
  const map = { IDLE: 'green', ON_PROJECT: 'gray', LEFT: 'red' }
  return map[status] || 'gray'
}

function parseTags(str) {
  if (!str) return []
  return str.split(/[,，]/).map(s => s.trim()).filter(Boolean)
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getTalentListApi()
    talentList.value = res.data || []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(formData, defaultForm())
  formRef.value?.clearValidate()
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  resetForm()
  modalVisible.value = true
}

function handleEdit(record) {
  isEdit.value = true
  editId.value = record.id
  Object.assign(formData, {
    name: record.name,
    gender: record.gender || '',
    phone: record.phone || '',
    email: record.email || '',
    level: record.level,
    monthlySalary: record.monthlySalary,
    techStack: record.techStack || '',
    status: record.status,
    remark: record.remark || ''
  })
  modalVisible.value = true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    const payload = { ...formData }
    if (isEdit.value) {
      payload.id = editId.value
      await updateTalentApi(payload)
      Message.success('编辑成功')
    } else {
      await addTalentApi(payload)
      Message.success('新增成功')
    }
    modalVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(record, newStatus) {
  if (record.status === newStatus) return
  try {
    await updateTalentStatusApi(record.id, newStatus)
    Message.success('状态更新成功')
    fetchList()
  } catch (e) {}
}

async function handleDelete(record) {
  try {
    await deleteTalentApi(record.id)
    Message.success('删除成功')
    fetchList()
  } catch (e) {}
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.page-container {
  width: 100%;
}

.page-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.title-icon {
  color: #165DFF;
  font-size: 20px;
}
</style>
