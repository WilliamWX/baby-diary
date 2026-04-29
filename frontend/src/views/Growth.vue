<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getGrowthChart, addGrowthRecord, getGrowthRecords, addFeedingRecord, getFeedingList, addSleepRecord, getSleepList, addMilestone, getMilestoneList } from '../api/growth'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'

use([CanvasRenderer, LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const route = useRoute()
const babyId = route.params.babyId

const activeTab = ref('growth')
const chartOption = ref({})
const growthRecords = ref([])
const feedingRecords = ref([])
const sleepRecords = ref([])
const milestones = ref([])

// 新增表单
const growthForm = ref({ babyId, recordDate: '', height: null, weight: null, headCircumference: null, note: '' })
const feedingForm = ref({ babyId, feedTime: '', type: 0, amount: '', foodName: '', note: '' })
const sleepForm = ref({ babyId, startTime: '', endTime: '', quality: 3, note: '' })
const milestoneForm = ref({ babyId, title: '', content: '', eventDate: '', photos: '' })

const showGrowthForm = ref(false)
const showFeedingForm = ref(false)
const showSleepForm = ref(false)
const showMilestoneForm = ref(false)

async function loadChart() {
  try {
    const res = await getGrowthChart(babyId)
    const data = res.data
    chartOption.value = {
      title: { text: '成长曲线', left: 'center' },
      tooltip: { trigger: 'axis' },
      legend: { data: ['身高(cm)', '体重(kg)', '头围(cm)'], bottom: 0 },
      grid: { left: 50, right: 20, top: 50, bottom: 40 },
      xAxis: { type: 'category', data: data.dates },
      yAxis: [
        { type: 'value', name: 'cm' },
        { type: 'value', name: 'kg' }
      ],
      series: [
        { name: '身高(cm)', type: 'line', data: data.heights, smooth: true },
        { name: '体重(kg)', type: 'line', yAxisIndex: 1, data: data.weights, smooth: true },
        { name: '头围(cm)', type: 'line', data: data.headCircumferences, smooth: true }
      ]
    }
  } catch (e) {
    chartOption.value = { title: { text: '暂无数据，快去记录吧' } }
  }
}

async function loadRecords() {
  const res = await getGrowthRecords(babyId)
  growthRecords.value = res.data || []
}

async function loadFeeding() {
  const res = await getFeedingList(babyId, '')
  feedingRecords.value = res.data || []
}

async function loadSleep() {
  const res = await getSleepList(babyId, '')
  sleepRecords.value = res.data || []
}

async function loadMilestones() {
  const res = await getMilestoneList(babyId)
  milestones.value = res.data || []
}

async function submitGrowth() {
  await addGrowthRecord(growthForm.value)
  ElMessage.success('记录成功')
  showGrowthForm.value = false
  loadChart()
  loadRecords()
}

async function submitFeeding() {
  await addFeedingRecord(feedingForm.value)
  ElMessage.success('记录成功')
  showFeedingForm.value = false
  loadFeeding()
}

async function submitSleep() {
  await addSleepRecord(sleepForm.value)
  ElMessage.success('记录成功')
  showSleepForm.value = false
  loadSleep()
}

async function submitMilestone() {
  await addMilestone(milestoneForm.value)
  ElMessage.success('里程碑记录成功')
  showMilestoneForm.value = false
  loadMilestones()
}

function typeLabel(type) {
  return ['母乳', '奶粉', '辅食'][type]
}

onMounted(() => {
  loadChart()
  loadRecords()
  loadFeeding()
  loadSleep()
  loadMilestones()
})
</script>

<template>
  <div class="growth-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="成长曲线" name="growth">
        <el-button type="primary" size="small" style="margin-bottom:16px" @click="showGrowthForm = true">+ 添加记录</el-button>
        <v-chart :option="chartOption" style="height:400px" autoresize />
        <el-table :data="growthRecords" style="margin-top:20px" stripe>
          <el-table-column prop="recordDate" label="日期" width="120" />
          <el-table-column prop="height" label="身高(cm)" />
          <el-table-column prop="weight" label="体重(kg)" />
          <el-table-column prop="headCircumference" label="头围(cm)" />
          <el-table-column prop="note" label="备注" />
        </el-table>

        <el-dialog v-model="showGrowthForm" title="添加成长记录" width="400px">
          <el-form :model="growthForm" label-width="100px">
            <el-form-item label="日期"><el-input v-model="growthForm.recordDate" type="date" /></el-form-item>
            <el-form-item label="身高(cm)"><el-input-number v-model="growthForm.height" :precision="1" /></el-form-item>
            <el-form-item label="体重(kg)"><el-input-number v-model="growthForm.weight" :precision="2" /></el-form-item>
            <el-form-item label="头围(cm)"><el-input-number v-model="growthForm.headCircumference" :precision="1" /></el-form-item>
            <el-form-item label="备注"><el-input v-model="growthForm.note" /></el-form-item>
          </el-form>
          <template #footer><el-button @click="showGrowthForm = false">取消</el-button><el-button type="primary" @click="submitGrowth">保存</el-button></template>
        </el-dialog>
      </el-tab-pane>

      <el-tab-pane label="喂养记录" name="feeding">
        <el-button type="primary" size="small" style="margin-bottom:16px" @click="showFeedingForm = true">+ 添加喂养</el-button>
        <el-table :data="feedingRecords" stripe>
          <el-table-column prop="feedTime" label="时间" width="160" />
          <el-table-column label="类型" width="80">
            <template #default="{row}">{{ typeLabel(row.type) }}</template>
          </el-table-column>
          <el-table-column prop="amount" label="量" width="80" />
          <el-table-column prop="foodName" label="食物" />
          <el-table-column prop="note" label="备注" />
        </el-table>

        <el-dialog v-model="showFeedingForm" title="添加喂养记录" width="400px">
          <el-form :model="feedingForm" label-width="80px">
            <el-form-item label="时间"><el-input v-model="feedingForm.feedTime" type="datetime-local" /></el-form-item>
            <el-form-item label="类型"><el-select v-model="feedingForm.type"><el-option :value="0" label="母乳"/><el-option :value="1" label="奶粉"/><el-option :value="2" label="辅食"/></el-select></el-form-item>
            <el-form-item label="量"><el-input v-model="feedingForm.amount" placeholder="如：200ml" /></el-form-item>
            <el-form-item label="食物"><el-input v-model="feedingForm.foodName" placeholder="如：米粉" /></el-form-item>
            <el-form-item label="备注"><el-input v-model="feedingForm.note" /></el-form-item>
          </el-form>
          <template #footer><el-button @click="showFeedingForm = false">取消</el-button><el-button type="primary" @click="submitFeeding">保存</el-button></template>
        </el-dialog>
      </el-tab-pane>

      <el-tab-pane label="睡眠记录" name="sleep">
        <el-button type="primary" size="small" style="margin-bottom:16px" @click="showSleepForm = true">+ 添加睡眠</el-button>
        <el-table :data="sleepRecords" stripe>
          <el-table-column prop="startTime" label="入睡" width="160" />
          <el-table-column prop="endTime" label="醒来" width="160" />
          <el-table-column label="质量" width="80">
            <template #default="{row}"><el-rate :model-value="row.quality" disabled show-score text-color="#ff9900" /></template>
          </el-table-column>
          <el-table-column prop="note" label="备注" />
        </el-table>

        <el-dialog v-model="showSleepForm" title="添加睡眠记录" width="400px">
          <el-form :model="sleepForm" label-width="80px">
            <el-form-item label="入睡"><el-input v-model="sleepForm.startTime" type="datetime-local" /></el-form-item>
            <el-form-item label="醒来"><el-input v-model="sleepForm.endTime" type="datetime-local" /></el-form-item>
            <el-form-item label="质量"><el-rate v-model="sleepForm.quality" /></el-form-item>
            <el-form-item label="备注"><el-input v-model="sleepForm.note" /></el-form-item>
          </el-form>
          <template #footer><el-button @click="showSleepForm = false">取消</el-button><el-button type="primary" @click="submitSleep">保存</el-button></template>
        </el-dialog>
      </el-tab-pane>

      <el-tab-pane label="里程碑" name="milestone">
        <el-button type="primary" size="small" style="margin-bottom:16px" @click="showMilestoneForm = true">+ 添加里程碑</el-button>
        <el-timeline>
          <el-timeline-item
            v-for="m in milestones"
            :key="m.id"
            :timestamp="m.eventDate"
            placement="top">
            <el-card>
              <h3>{{ m.title }}</h3>
              <p>{{ m.content }}</p>
            </el-card>
          </el-timeline-item>
        </el-timeline>

        <el-dialog v-model="showMilestoneForm" title="添加里程碑" width="400px">
          <el-form :model="milestoneForm" label-width="80px">
            <el-form-item label="标题"><el-input v-model="milestoneForm.title" placeholder="如：第一次翻身" /></el-form-item>
            <el-form-item label="日期"><el-input v-model="milestoneForm.eventDate" type="date" /></el-form-item>
            <el-form-item label="描述"><el-input v-model="milestoneForm.content" type="textarea" /></el-form-item>
          </el-form>
          <template #footer><el-button @click="showMilestoneForm = false">取消</el-button><el-button type="primary" @click="submitMilestone">保存</el-button></template>
        </el-dialog>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.growth-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 0;
}
</style>
