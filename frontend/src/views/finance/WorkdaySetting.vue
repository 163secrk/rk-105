<template>
  <div class="workday-page">
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="section-title">
          <icon-calendar class="section-icon" />
          <span>法定工作日设置</span>
        </div>
        <div class="section-desc">按月设置法定工作日天数，生成对账单时将作为计算因子</div>
      </div>
      <a-button type="primary" @click="openGenerateModal">
        <template #icon><icon-calendar-clock /></template>
        自动生成
      </a-button>
      <a-button type="primary" @click="openAddModal">
        <template #icon><icon-plus /></template>
        新增设置
      </a-button>
    </div>

    <a-table
      :data="workdayList"
      :loading="loading"
      :pagination="false"
      row-key="id"
      bordered
    >
      <template #columns>
        <a-table-column title="月份" data-index="month" width="140">
          <template #cell="{ record }">
            <span class="month-tag">{{ formatMonth(record.month) }}</span>
          </template>
        </a-table-column>
        <a-table-column title="法定工作日天数" data-index="workdayCount" width="180">
          <template #cell="{ record }">
            <a-tag color="blue" size="large">{{ record.workdayCount }} 天</a-tag>
          </template>
        </a-table-column>
        <a-table-column title="备注" data-index="remark" width="260">
          <template #cell="{ record }">
            <span>{{ record.remark || '-' }}</span>
          </template>
        </a-table-column>
        <a-table-column title="更新时间" data-index="updateTime" width="200">
          <template #cell="{ record }">
            <span>{{ formatDateTime(record.updateTime) }}</span>
          </template>
        </a-table-column>
        <a-table-column title="操作" width="180" fixed="right">
          <template #cell="{ record }">
            <a-space>
              <a-button type="text" size="small" @click="openEditModal(record)">
                <template #icon><icon-edit /></template>
                编辑
              </a-button>
              <a-button type="text" status="danger" size="small" @click="handleDelete(record)">
                <template #icon><icon-delete /></template>
                删除
              </a-button>
            </a-space>
          </template>
        </a-table-column>
      </template>
      <template #empty>
        <a-empty description="暂无工作日设置，点击新增设置开始配置" />
      </template>
    </a-table>

    <a-modal
      v-model:visible="modalVisible"
      :title="editingId ? '编辑工作日设置' : '新增工作日设置'"
      :ok-text="editingId ? '保存' : '确认新增'"
      @before-ok="handleBeforeOk"
      @cancel="modalVisible = false"
    >
      <a-form ref="formRef" :model="formData" layout="vertical" :rules="formRules">
        <a-form-item field="month" label="月份">
          <a-month-picker
            v-model="formData.month"
            placeholder="请选择月份"
            style="width: 100%"
            value-format="YYYY-MM"
            :disabled="!!editingId"
          />
        </a-form-item>
        <a-form-item field="workdayCount" label="法定工作日天数">
          <a-input-number
            v-model="formData.workdayCount"
            :min="1"
            :max="31"
            placeholder="请输入工作日天数(1-31)"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item field="remark" label="备注">
          <a-textarea
            v-model="formData.remark"
            placeholder="可选，输入备注信息"
            :max-length="200"
            :auto-size="{ minRows: 2, maxRows: 4 }"
            show-word-limit
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:visible="generateModalVisible"
      title="自动生成年度工作日"
      ok-text="确认生成"
      @before-ok="handleGenerateOk"
      @cancel="generateModalVisible = false"
    >
      <div class="generate-tips">
        <icon-info-circle /> 选择年份后，系统将自动计算每月的法定工作日天数（去除周末），已有设置的月份不会被覆盖，生成后可根据法定节假日手动调整。
      </div>
      <a-form ref="generateFormRef" :model="generateForm" layout="vertical">
        <a-form-item field="year" label="选择年份" :rules="[{ required: true, message: '请选择年份' }]">
          <a-date-picker
            v-model="generateForm.year"
            mode="year"
            placeholder="请选择年份"
            style="width: 100%"
            value-format="YYYY"
            :disabled-date="disableBefore2020"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import {
  IconCalendar,
  IconPlus,
  IconEdit,
  IconDelete,
  IconCalendarClock,
  IconInfoCircle
} from '@arco-design/web-vue/es/icon'
import {
  getWorkdayListApi,
  saveWorkdayApi,
  deleteWorkdayApi,
  generateYearWorkdaysApi
} from '@/api/workday'

const loading = ref(false)
const workdayList = ref([])
const modalVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const formData = ref({
  month: '',
  workdayCount: null,
  remark: ''
})

const generateModalVisible = ref(false)
const generateFormRef = ref(null)
const generateForm = ref({
  year: ''
})

const formRules = {
  month: [{ required: true, message: '请选择月份' }],
  workdayCount: [
    { required: true, message: '请输入工作日天数' },
    {
      validator: (value, callback) => {
        const count = Number(value)
        if (!count || count < 1 || count > 31) {
          callback('工作日天数必须在1-31之间')
          return
        }
        callback()
      }
    }
  ]
}

function formatMonth(month) {
  if (!month) return '-'
  const parts = month.split('-')
  return `${parts[0]}年${parts[1]}月`
}

function formatDateTime(dt) {
  if (!dt) return '-'
  if (typeof dt === 'string') return dt.replace('T', ' ').substring(0, 19)
  const d = new Date(dt)
  if (isNaN(d.getTime())) return String(dt)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function disableBefore2020(current) {
  if (!current) return false
  const d = current instanceof Date ? current : new Date(current)
  return d.getFullYear() < 2020
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getWorkdayListApi()
    workdayList.value = res.data || []
  } catch (e) {
    workdayList.value = []
  } finally {
    loading.value = false
  }
}

function resetForm() {
  editingId.value = null
  formData.value = {
    month: '',
    workdayCount: null,
    remark: ''
  }
  formRef.value?.clearValidate()
}

function openAddModal() {
  resetForm()
  modalVisible.value = true
}

function openGenerateModal() {
  generateForm.value = { year: '' }
  generateFormRef.value?.clearValidate()
  generateModalVisible.value = true
}

async function handleGenerateOk(done) {
  try {
    await generateFormRef.value?.validate()
  } catch {
    done(false)
    return
  }
  try {
    await generateYearWorkdaysApi(Number(generateForm.value.year))
    Message.success(`${generateForm.value.year}年工作日生成成功`)
    done()
    fetchList()
  } catch {
    done(false)
  }
}

function openEditModal(record) {
  editingId.value = record.id
  formData.value = {
    month: record.month,
    workdayCount: record.workdayCount,
    remark: record.remark || ''
  }
  modalVisible.value = true
}

async function handleBeforeOk(done) {
  try {
    await formRef.value?.validate()
  } catch {
    done(false)
    return
  }
  try {
    await saveWorkdayApi({
      id: editingId.value,
      month: formData.value.month,
      workdayCount: Number(formData.value.workdayCount),
      remark: formData.value.remark
    })
    Message.success(editingId.value ? '更新成功' : '新增成功')
    done()
    fetchList()
  } catch {
    done(false)
  }
}

function handleDelete(record) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除 ${formatMonth(record.month)} 的工作日设置吗？`,
    okButtonProps: { status: 'danger' },
    onOk: async () => {
      try {
        await deleteWorkdayApi(record.id)
        Message.success('删除成功')
        fetchList()
      } catch (e) {}
    }
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.workday-page {
  width: 100%;
}

.toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

.section-icon {
  color: #165DFF;
  font-size: 18px;
}

.section-desc {
  font-size: 13px;
  color: #86909c;
}

.month-tag {
  display: inline-block;
  padding: 4px 12px;
  background: #e8f3ff;
  color: #165DFF;
  border-radius: 4px;
  font-weight: 500;
  font-size: 13px;
}

.generate-tips {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  background: #e8f3ff;
  border-radius: 6px;
  font-size: 13px;
  color: #4e5969;
  line-height: 1.6;
}
</style>
