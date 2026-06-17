<template>
  <div class="page-container">
    <a-card :bordered="false" class="page-card">
      <template #title>
        <div class="card-header">
          <div class="card-title">
            <icon-folder class="title-icon" />
            <span>项目池</span>
          </div>
          <a-button type="primary" @click="handleAdd">
            <template #icon>
              <icon-plus />
            </template>
            新增项目
          </a-button>
        </div>
      </template>
      <a-table
        :data="projectList"
        :loading="loading"
        :pagination="false"
        :scroll="{ x: 1300 }"
        bordered
        row-key="id"
      >
        <template #columns>
          <a-table-column title="项目名称" data-index="projectName" :width="180" />
          <a-table-column title="甲方公司" data-index="clientName" :width="160" />
          <a-table-column title="联系人" data-index="contactPerson" :width="100" />
          <a-table-column title="联系电话" data-index="contactPhone" :width="140" />
          <a-table-column title="初级单价（元/人月）" :width="160">
            <template #cell="{ record }">
              <a-tag color="green">
                {{ record.priceJunior ? `¥${Number(record.priceJunior).toLocaleString()}` : '-' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="中级单价（元/人月）" :width="160">
            <template #cell="{ record }">
              <a-tag color="blue">
                {{ record.priceMiddle ? `¥${Number(record.priceMiddle).toLocaleString()}` : '-' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="高级单价（元/人月）" :width="160">
            <template #cell="{ record }">
              <a-tag color="orangered">
                {{ record.priceSenior ? `¥${Number(record.priceSenior).toLocaleString()}` : '-' }}
              </a-tag>
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="140" fixed="right">
            <template #cell="{ record }">
              <a-button type="text" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-popconfirm
                content="确定要删除该项目吗？"
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
      :title="isEdit ? '编辑项目' : '新增项目'"
      @before-ok="handleBeforeOk"
      @cancel="modalVisible = false"
      :ok-loading="submitting"
      width="600px"
    >
      <a-form
        ref="formRef"
        :model="formData"
        layout="vertical"
        :rules="formRules"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="projectName" label="项目名称">
              <a-input v-model="formData.projectName" placeholder="请输入项目名称" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="clientName" label="甲方公司">
              <a-input v-model="formData.clientName" placeholder="请输入甲方公司名称" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item field="contactPerson" label="联系人">
              <a-input v-model="formData.contactPerson" placeholder="请输入联系人" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item field="contactPhone" label="联系电话">
              <a-input v-model="formData.contactPhone" placeholder="请输入联系电话" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-divider orientation="left">人月结算单价配置</a-divider>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item field="priceJunior" label="初级（元/人月）">
              <a-input-number
                v-model="formData.priceJunior"
                :min="0"
                :precision="0"
                placeholder="初级单价"
                style="width: 100%;"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="priceMiddle" label="中级（元/人月）">
              <a-input-number
                v-model="formData.priceMiddle"
                :min="0"
                :precision="0"
                placeholder="中级单价"
                style="width: 100%;"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item field="priceSenior" label="高级（元/人月）">
              <a-input-number
                v-model="formData.priceSenior"
                :min="0"
                :precision="0"
                placeholder="高级单价"
                style="width: 100%;"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item field="remark" label="备注">
          <a-textarea v-model="formData.remark" :auto-size="{ minRows: 2, maxRows: 4 }" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import {
  IconFolder,
  IconPlus
} from '@arco-design/web-vue/es/icon'
import {
  getProjectListApi,
  addProjectApi,
  updateProjectApi,
  deleteProjectApi
} from '@/api/project'

const loading = ref(false)
const projectList = ref([])
const modalVisible = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const defaultForm = () => ({
  projectName: '',
  clientName: '',
  contactPerson: '',
  contactPhone: '',
  priceJunior: undefined,
  priceMiddle: undefined,
  priceSenior: undefined,
  remark: ''
})

const formData = reactive(defaultForm())

const phoneRegex = /^1[3-9]\d{9}$/

function validatePriceOrder(value, callback) {
  const { priceJunior, priceMiddle, priceSenior } = formData
  if (priceJunior != null && priceMiddle != null && priceMiddle <= priceJunior) {
    callback('中级单价应高于初级单价')
    return
  }
  if (priceMiddle != null && priceSenior != null && priceSenior <= priceMiddle) {
    callback('高级单价应高于中级单价')
    return
  }
  if (priceJunior != null && priceSenior != null && priceSenior <= priceJunior) {
    callback('高级单价应高于初级单价')
    return
  }
  callback()
}

const formRules = {
  projectName: [{ required: true, message: '请输入项目名称' }],
  contactPhone: [
    { required: true, message: '请输入联系电话' },
    { match: phoneRegex, message: '请输入正确的手机号码（11位，以1开头）' }
  ],
  priceJunior: [{ validator: validatePriceOrder }],
  priceMiddle: [{ validator: validatePriceOrder }],
  priceSenior: [{ validator: validatePriceOrder }]
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getProjectListApi()
    projectList.value = res.data || []
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
    projectName: record.projectName,
    clientName: record.clientName || '',
    contactPerson: record.contactPerson || '',
    contactPhone: record.contactPhone || '',
    priceJunior: record.priceJunior,
    priceMiddle: record.priceMiddle,
    priceSenior: record.priceSenior,
    remark: record.remark || ''
  })
  modalVisible.value = true
}

async function handleBeforeOk(done) {
  try {
    await formRef.value?.validate()
  } catch {
    done(false)
    return
  }
  submitting.value = true
  try {
    const payload = { ...formData }
    if (isEdit.value) {
      payload.id = editId.value
      await updateProjectApi(payload)
      Message.success('编辑成功')
    } else {
      await addProjectApi(payload)
      Message.success('新增成功')
    }
    done()
    fetchList()
  } catch {
    done(false)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(record) {
  try {
    await deleteProjectApi(record.id)
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
