<template>
  <div class="page-container">
    <a-card :bordered="false" class="page-card">
      <template #title>
        <div class="card-header">
          <div class="card-title">
          <icon-send class="title-icon" />
          <span>指派中心</span>
        </div>
        <a-button type="primary" @click="openWizard">
          <template #icon>
            <icon-plus />
          </template>
          新增指派
        </a-button>
      </div>
      </template>

      <a-table
        :data="assignmentList"
        :loading="listLoading"
        :pagination="false"
        :scroll="{ x: 1400 }"
        bordered
        row-key="id"
      >
        <template #columns>
          <a-table-column title="人才姓名" data-index="talentName" :width="120" />
          <a-table-column title="职级" :width="100">
            <template #cell="{ record }">
              <a-tag :color="levelColor(record.talentLevel)">{{ levelText(record.talentLevel) }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="项目名称" data-index="projectName" :width="180" />
          <a-table-column title="开始日期" :width="120">
            <template #cell="{ record }">
              {{ formatDate(record.startDate) }}
            </template>
          </a-table-column>
          <a-table-column title="结束日期" :width="120">
            <template #cell="{ record }">
              {{ formatDate(record.endDate) }}
            </template>
          </a-table-column>
          <a-table-column title="结算单价（元/人月）" :width="160">
            <template #cell="{ record }">
              <a-tag color="arcoblue">
                {{ record.unitPrice ? `¥${Number(record.unitPrice).toLocaleString()}` : '-' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="状态" :width="100">
            <template #cell="{ record }">
              <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="100" fixed="right">
            <template #cell="{ record }">
              <a-popconfirm
                content="确定要删除该指派记录吗？"
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
      v-model:visible="wizardVisible"
      title="新增外派指派"
      :footer="false"
      :mask-closable="false"
      width="820px"
      @cancel="wizardVisible = false"
    >
      <a-steps :current="currentStep" line-style="solid">
        <a-step title="选择人才和项目" />
        <a-step title="填写日期和单价" />
        <a-step title="确认提交" />
      </a-steps>

      <div class="wizard-content">
        <div v-show="currentStep === 1" class="step-content">
          <a-form
            ref="step1FormRef"
            :model="wizardData"
            layout="vertical"
            :rules="step1Rules"
          >
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item field="talentId" label="选择人才">
                  <a-select
                    v-model="wizardData.talentId"
                    placeholder="请选择人才"
                    :disabled="!!wizardData.talentId"
                    allow-clear
                    @clear="handleTalentClear"
                    @change="handleTalentChange"
                  >
                      <a-option
                        v-for="t in talentList" :key="t.id" :value="t.id">
                        <div class="select-option">
                          <span>{{ t.name }}</span>
                          <a-tag :color="levelColor(t.level)" size="small">{{ levelText(t.level) }}</a-tag>
                          <span class="option-sub">{{ t.phone || t.email }}</span>
                        </div>
                      </a-option>
                    </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item field="projectId" label="选择项目">
                  <a-select
                    v-model="wizardData.projectId"
                    placeholder="请选择项目"
                    :disabled="!!wizardData.projectId"
                    allow-clear
                    @clear="handleProjectClear"
                    @change="handleProjectChange"
                  >
                    <a-option
                      v-for="p in projectList" :key="p.id" :value="p.id">
                      <div class="select-option">
                        <span>{{ p.projectName }}</span>
                        <span class="option-sub">{{ p.clientName }}</span>
                      </div>
                    </a-option>
                  </a-select>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>

          <div v-if="wizardData.talentId || wizardData.projectId" class="selected-info">
            <a-alert type="info">
              <template #icon>
                <icon-user />
              </template>
              <div class="info-content">
                <div v-if="selectedTalent" class="info-row">
                  <span class="info-label">已选人才：</span>
                  <span class="info-value">{{ selectedTalent.name }}</span>
                  <a-tag :color="levelColor(selectedTalent.level)" size="small">{{ levelText(selectedTalent.level) }}</a-tag>
                </div>
                <div v-if="selectedProject" class="info-row">
                  <span class="info-label">已选项目：</span>
                  <span class="info-value">{{ selectedProject.projectName }}</span>
                  <span class="option-sub">{{ selectedProject.clientName }}</span>
                </div>
              </div>
            </a-alert>
          </div>
        </div>

        <div v-show="currentStep === 2" class="step-content">
          <a-form
            ref="step2FormRef"
            :model="wizardData"
            layout="vertical"
            :rules="step2Rules"
          >
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item field="startDate" label="开始日期">
                  <a-date-picker
                    v-model="wizardData.startDate"
                    style="width: 100%;"
                    placeholder="请选择开始日期"
                    format="YYYY-MM-DD"
                    :allow-clear
                    @change="handleDateChange"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item field="endDate" label="结束日期">
                  <a-date-picker
                    v-model="wizardData.endDate"
                    style="width: 100%;"
                    placeholder="请选择结束日期"
                    format="YYYY-MM-DD"
                    :allow-clear
                    @change="handleDateChange"
                  />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item field="unitPrice" label="结算单价（元/人月）">
              <a-input-number
                v-model="wizardData.unitPrice"
                :min="0"
                :precision="0"
                placeholder="请输入结算单价"
                style="width: 100%;"
              />
              <div class="price-hint" v-if="suggestedPrice !== null">
                <icon-file />
                <span>根据项目配置和人才职级，建议单价为</span>
                <a-tag color="green">¥{{ Number(suggestedPrice).toLocaleString() }}</a-tag>
                <span>，已自动填入，可手动修改</span>
              </div>
            </a-form-item>

            <a-form-item field="remark" label="备注">
              <a-textarea v-model="wizardData.remark" :auto-size="{ minRows: 2, maxRows: 4 }" placeholder="请输入备注（选填）" />
            </a-form-item>
          </a-form>

          <div v-if="conflictError" class="conflict-alert">
            <a-alert type="error">
              <template #icon>
                <icon-folder />
              </template>
              <div class="conflict-content">
                <div class="conflict-title">时间段冲突！</div>
                <div class="conflict-desc">{{ conflictError }}</div>
              </div>
            </a-alert>
          </div>
        </div>

        <div v-show="currentStep === 3" class="step-content">
          <a-descriptions bordered size="default" :column="2">
            <a-descriptions-item label="人才姓名">
              {{ selectedTalent?.name || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="人才职级">
              <a-tag :color="levelColor(selectedTalent?.level)">{{ levelText(selectedTalent?.level) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="项目名称">
              {{ selectedProject?.projectName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="甲方公司">
              {{ selectedProject?.clientName || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="开始日期">
              {{ formatDate(wizardData.startDate) }}
            </a-descriptions-item>
            <a-descriptions-item label="结束日期">
              {{ formatDate(wizardData.endDate) }}
            </a-descriptions-item>
            <a-descriptions-item label="结算单价">
              <a-tag color="arcoblue">¥{{ Number(wizardData.unitPrice).toLocaleString() }}/人月</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="备注">
              {{ wizardData.remark || '-' }}
            </a-descriptions-item>
          </a-descriptions>

          <div v-if="conflictError" class="conflict-alert" style="margin-top: 20px;">
            <a-alert type="error">
              <template #icon>
                <icon-folder />
              </template>
              <div class="conflict-content">
                <div class="conflict-title">时间段冲突！</div>
                <div class="conflict-desc">{{ conflictError }}</div>
              </div>
            </a-alert>
          </div>
        </div>
      </div>

      <div class="wizard-footer">
        <a-space>
          <a-button v-if="currentStep > 1" @click="prevStep">上一步</a-button>
          <a-button
            v-if="currentStep < 3"
            type="primary"
            @click="nextStep"
          >
            下一步
          </a-button>
          <a-button
            v-if="currentStep === 3"
            type="primary"
            :loading="submitting"
            @click="handleSubmit"
            :disabled="!!conflictError"
          >
            确认提交
          </a-button>
        </a-space>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import {
  IconSend,
  IconPlus,
  IconUser,
  IconFolder,
  IconFile
} from '@arco-design/web-vue/es/icon'
import {
  getTalentListApi
} from '@/api/talent'
import {
  getProjectListApi
} from '@/api/project'
import {
  getAssignmentListApi,
  addAssignmentApi,
  deleteAssignmentApi,
  checkConflictApi,
  suggestPriceApi
} from '@/api/assignment'

const listLoading = ref(false)
const assignmentList = ref([])
const talentList = ref([])
const projectList = ref([])

const wizardVisible = ref(false)
const currentStep = ref(1)
const submitting = ref(false)
const suggestedPrice = ref(null)
const conflictError = ref('')
const step1FormRef = ref(null)
const step2FormRef = ref(null)

const defaultWizardData = () => ({
  talentId: undefined,
  projectId: undefined,
  startDate: '',
  endDate: '',
  unitPrice: undefined,
  remark: ''
})

const wizardData = reactive(defaultWizardData())

const step1Rules = {
  talentId: [{ required: true, message: '请选择人才' }],
  projectId: [{ required: true, message: '请选择项目' }]
}

const step2Rules = {
  startDate: [{ required: true, message: '请选择开始日期' }],
  endDate: [{ required: true, message: '请选择结束日期' }],
  unitPrice: [{ required: true, message: '请输入结算单价' }]
}

const selectedTalent = computed(() => {
  if (!wizardData.talentId) return null
  return talentList.value.find(t => t.id === wizardData.talentId)
})

const selectedProject = computed(() => {
  if (!wizardData.projectId) return null
  return projectList.value.find(p => p.id === wizardData.projectId)
})

function levelText(level) {
  const map = { JUNIOR: '初级', MIDDLE: '中级', SENIOR: '高级' }
  return map[level] || level || '-'
}

function levelColor(level) {
  const map = { JUNIOR: 'green', MIDDLE: 'blue', SENIOR: 'orangered' }
  return map[level] || 'gray'
}

function statusText(status) {
  const map = { ACTIVE: '进行中', FINISHED: '已结束', CANCELLED: '已取消' }
  return map[status] || status || '-'
}

function statusColor(status) {
  const map = { ACTIVE: 'green', FINISHED: 'gray', CANCELLED: 'red' }
  return map[status] || 'gray'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

async function fetchTalentList() {
  try {
    const res = await getTalentListApi()
    talentList.value = (res.data || []).filter(t => t.status !== 'LEFT')
  } catch (e) {}
}

async function fetchProjectList() {
  try {
    const res = await getProjectListApi()
    projectList.value = res.data || []
  } catch (e) {}
}

async function fetchAssignmentList() {
  listLoading.value = true
  try {
    const res = await getAssignmentListApi()
    assignmentList.value = res.data || []
  } finally {
    listLoading.value = false
  }
}

function openWizard() {
  Object.assign(wizardData, defaultWizardData())
  suggestedPrice.value = null
  conflictError.value = ''
  currentStep.value = 1
  wizardVisible.value = true
  step1FormRef.value?.clearValidate()
  step2FormRef.value?.clearValidate()
}

function handleTalentChange() {
  suggestedPrice.value = null
  wizardData.unitPrice = undefined
  wizardData.startDate = ''
  wizardData.endDate = ''
  conflictError.value = ''
  tryFetchSuggestedPrice()
  tryCheckConflict()
}

function handleTalentClear() {
  wizardData.talentId = undefined
  suggestedPrice.value = null
  wizardData.unitPrice = undefined
  conflictError.value = ''
}

function handleProjectChange() {
  suggestedPrice.value = null
  wizardData.unitPrice = undefined
  conflictError.value = ''
  tryFetchSuggestedPrice()
  tryCheckConflict()
}

function handleProjectClear() {
  wizardData.projectId = undefined
  suggestedPrice.value = null
  wizardData.unitPrice = undefined
  conflictError.value = ''
}

function handleDateChange() {
  conflictError.value = ''
  tryCheckConflict()
}

async function tryFetchSuggestedPrice() {
  if (!wizardData.talentId || !wizardData.projectId) {
    return
  }
  try {
    const res = await suggestPriceApi({
      projectId: wizardData.projectId,
      talentId: wizardData.talentId
    })
    if (res.data !== null && res.data !== undefined) {
      suggestedPrice.value = res.data
      if (wizardData.unitPrice === undefined || wizardData.unitPrice === null || wizardData.unitPrice === '') {
        wizardData.unitPrice = res.data
      }
    }
  } catch (e) {}
}

async function tryCheckConflict() {
  if (!wizardData.talentId || !wizardData.startDate || !wizardData.endDate) {
    conflictError.value = ''
    return
  }
  try {
    const res = await checkConflictApi({
      talentId: wizardData.talentId,
      startDate: wizardData.startDate,
      endDate: wizardData.endDate
    })
    if (res.data && res.data.conflict) {
      const list = res.data.conflictingAssignments || []
      if (list.length > 0) {
        const first = list[0]
        conflictError.value = `${selectedTalent.value?.name || '该人才'} 已被指派到「${first.projectName}」（${formatDate(first.startDate)} ~ ${formatDate(first.endDate)}），与当前时间段存在重叠。`
      } else {
        conflictError.value = '该人才在指定时间段内已有其他指派记录。'
      }
    } else {
        conflictError.value = ''
      }
  } catch (e) {
    conflictError.value = ''
  }
}

async function nextStep() {
  if (currentStep.value === 1) {
    try {
      await step1FormRef.value?.validate()
    } catch {
      return
    }
    currentStep.value = 2
  } else if (currentStep.value === 2) {
    try {
      await step2FormRef.value?.validate()
    } catch {
      return
    }
    const start = new Date(wizardData.startDate)
    const end = new Date(wizardData.endDate)
    if (end < start) {
      Message.error('结束日期不能早于开始日期')
      return
    }
    await tryCheckConflict()
    if (conflictError.value) {
      return
    }
    currentStep.value = 3
  }
}

function prevStep() {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

async function handleSubmit() {
  if (conflictError.value) {
    Message.error('存在时间段冲突，无法提交')
    return
  }
  submitting.value = true
  try {
    await addAssignmentApi({
      talentId: wizardData.talentId,
      projectId: wizardData.projectId,
      startDate: wizardData.startDate,
      endDate: wizardData.endDate,
      unitPrice: wizardData.unitPrice,
      remark: wizardData.remark
    })
    Message.success('指派成功')
    wizardVisible.value = false
    fetchAssignmentList()
  } catch (e) {
    if (e?.response?.data?.message) {
      Message.error(e.response.data.message)
    }
  } finally {
    submitting.value = false
  }
}

async function handleDelete(record) {
  try {
    await deleteAssignmentApi(record.id)
    Message.success('删除成功')
    fetchAssignmentList()
  } catch (e) {}
}

onMounted(() => {
  fetchTalentList()
  fetchProjectList()
  fetchAssignmentList()
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

.select-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.option-sub {
  color: #86909c;
  font-size: 12px;
}

.selected-info {
  margin-top: 16px;
}

.info-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.info-label {
  color: #4e5969;
}

.info-value {
  font-weight: 500;
  color: #1d2129;
}

.wizard-content {
  margin-top: 32px;
  min-height: 320px;
}

.step-content {
  padding: 8px 0;
}

.price-hint {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #86909c;
}

.conflict-alert {
  margin-top: 16px;
}

.conflict-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.conflict-title {
  font-weight: 600;
  color: #f53f3f;
}

.conflict-desc {
  font-size: 13px;
}

.wizard-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e6eb;
  display: flex;
  justify-content: flex-end;
}
</style>
