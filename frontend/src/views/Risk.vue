<template>
  <div class="risk">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <div style="text-align: center">
            <div style="font-size: 36px; font-weight: bold; color: #F56C6C">{{ riskContracts.length }}</div>
            <div style="color: #909399; margin-top: 10px">风险合同数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center">
            <div style="font-size: 36px; font-weight: bold; color: #E6A23C">{{ expiredTasksCount }}</div>
            <div style="color: #909399; margin-top: 10px">逾期任务数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center">
            <div style="font-size: 36px; font-weight: bold; color: #409EFF">{{ totalAmount }}</div>
            <div style="color: #909399; margin-top: 10px">风险金额(万)</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card>
          <div style="text-align: center">
            <div style="font-size: 36px; font-weight: bold; color: #67C23A">{{ processedCount }}</div>
            <div style="color: #909399; margin-top: 10px">已处理风险</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <div slot="header">
        <span>风险合同列表</span>
      </div>
      <el-table :data="riskContracts" style="width: 100%" v-if="riskContracts.length > 0">
        <el-table-column prop="name" label="合同名称" width="150"></el-table-column>
        <el-table-column prop="code" label="合同编号" width="150"></el-table-column>
        <el-table-column prop="amount" label="合同金额" width="120">
          <template slot-scope="scope">¥{{ Number(scope.row.amount).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="partyB" label="合作方" width="120"></el-table-column>
        <el-table-column label="风险类型" width="120">
          <template slot-scope="scope">
            <el-tag type="danger" size="small">履约逾期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="100">
          <template slot-scope="scope">
            <el-tag type="danger" size="small">高风险</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发生时间" width="120" prop="updatedAt"></el-table-column>
        <el-table-column label="处理状态" width="100">
          <template slot-scope="scope">
            <el-tag type="warning" size="small">待处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" @click="viewDetail(scope.row.id)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-else style="text-align: center; padding: 60px; color: #909399">
        <el-icon name="success" style="font-size: 48px; margin-bottom: 20px; color: #67C23A"></el-icon>
        <p>暂无风险合同</p>
        <p style="font-size: 12px; margin-top: 10px">系统运行良好，继续保持！</p>
      </div>
    </el-card>

    <el-card style="margin-top: 20px">
      <div slot="header">
        <span>风险预警记录</span>
      </div>
      <el-table :data="warnings" style="width: 100%" v-if="warnings.length > 0">
        <el-table-column prop="type" label="预警类型" width="120">
          <template slot-scope="scope">
            <el-tag :type="scope.row.type === 'TASK' ? 'warning' : 'danger'" size="small">{{ scope.row.type === 'TASK' ? '任务即将到期' : '履约逾期' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contractName" label="合同名称" width="150"></el-table-column>
        <el-table-column prop="taskName" label="相关任务" width="150"></el-table-column>
        <el-table-column prop="message" label="预警内容"></el-table-column>
        <el-table-column prop="owner" label="负责人" width="100"></el-table-column>
        <el-table-column prop="createdAt" label="预警时间" width="160"></el-table-column>
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'READ' ? 'info' : 'danger'" size="small">{{ scope.row.status === 'READ' ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-else style="text-align: center; padding: 40px; color: #909399">
        <el-icon name="bell" style="font-size: 36px; margin-bottom: 15px; color: #67C23A"></el-icon>
        <p>暂无预警记录</p>
      </div>
    </el-card>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'Risk',
  data() {
    return {
      riskContracts: [],
      warnings: []
    }
  },
  computed: {
    expiredTasksCount() {
      return this.warnings.filter(w => w.type === 'EXPIRED').length
    },
    totalAmount() {
      const total = this.riskContracts.reduce((sum, c) => sum + Number(c.amount || 0), 0)
      return Math.round(total / 10000)
    },
    processedCount() {
      return this.warnings.filter(w => w.status === 'READ').length
    }
  },
  mounted() {
    this.loadData()
    this.timer = setInterval(() => this.loadData(), 30000)
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    async loadData() {
      try {
        const res = await api.getContracts()
        this.riskContracts = res.data.filter(c => c.status === 'RISK')
        this.generateWarnings(res.data)
      } catch (e) {
        this.$message.error('加载失败')
      }
    },
    generateWarnings(contracts) {
      const warnings = []
      contracts.forEach(contract => {
        if (contract.paymentNodes) {
          contract.paymentNodes.forEach(node => {
            if (node.dueDate) {
              const due = new Date(node.dueDate)
              const now = new Date()
              const diff = due - now
              const days = diff / (1000 * 60 * 60 * 24)
              if (days > 0 && days <= 3 && !node.completed) {
                warnings.push({
                  type: 'TASK',
                  contractName: contract.name,
                  taskName: node.name,
                  message: `付款任务"${node.name}"将在${Math.ceil(days)}天后到期，请及时处理`,
                  owner: contract.creator,
                  createdAt: new Date().toLocaleString(),
                  status: 'UNREAD'
                })
              }
              if (days < 0 && !node.completed) {
                warnings.push({
                  type: 'EXPIRED',
                  contractName: contract.name,
                  taskName: node.name,
                  message: `付款任务"${node.name}"已逾期${Math.abs(Math.floor(days))}天，请立即处理`,
                  owner: contract.creator,
                  createdAt: new Date().toLocaleString(),
                  status: 'UNREAD'
                })
              }
            }
          })
        }
      })
      this.warnings = warnings
    },
    viewDetail(contractId) {
      this.$router.push(`/contract/${contractId}`)
    }
  }
}
</script>
