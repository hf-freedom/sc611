<template>
  <div class="home">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.totalCount || 0 }}</div>
            <div class="stat-label">合同总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">¥{{ formatAmount(statistics.totalAmount) }}</div>
            <div class="stat-label">合同总金额</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.performanceRate || 0 }}%</div>
            <div class="stat-label">履约完成率</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value" style="color: #f56c6c">{{ statistics.expiredCount || 0 }}</div>
            <div class="stat-label">逾期任务</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>待审批合同</span>
          </div>
          <el-table :data="pendingContracts" style="width: 100%" size="small">
            <el-table-column prop="name" label="合同名称"></el-table-column>
            <el-table-column prop="code" label="合同编号"></el-table-column>
            <el-table-column prop="currentApprover" label="当前审批人"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header">
            <span>系统提醒</span>
          </div>
          <div>
            <div v-for="(reminder, index) in reminders" :key="index" style="padding: 8px 0; border-bottom: 1px solid #eee">
              {{ reminder }}
            </div>
            <div v-if="reminders.length === 0" style="color: #909399; padding: 8px 0">
              暂无提醒
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'Home',
  data() {
    return {
      statistics: {},
      pendingContracts: [],
      reminders: []
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const [statRes, contractRes, reminderRes] = await Promise.all([
          api.getStatistics(),
          api.getContracts(),
          api.getReminders()
        ])
        this.statistics = statRes.data
        this.pendingContracts = contractRes.data.filter(c => 
          c.status === 'PENDING_APPROVAL' || 
          c.status === 'LEGAL_REVIEW' || 
          c.status === 'FINANCIAL_REVIEW'
        )
        this.reminders = reminderRes.data
      } catch (e) {
        console.error('加载数据失败', e)
      }
    },
    formatAmount(amount) {
      if (!amount) return '0'
      return Number(amount).toLocaleString()
    }
  }
}
</script>

<style scoped>
.stat-card {
  text-align: center;
}
.stat-content {
  padding: 10px 0;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}
.stat-label {
  font-size: 14px;
  color: #909399;
}
</style>
