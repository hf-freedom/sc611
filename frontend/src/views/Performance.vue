<template>
  <div class="performance">
    <el-card>
      <div slot="header" style="display: flex; justify-content: space-between; align-items: center">
        <span>履约任务管理</span>
        <div>
          <el-tag type="info" style="margin-right: 10px">待处理: {{ pendingCount }}</el-tag>
          <el-tag type="warning">即将到期: {{ upcomingCount }}</el-tag>
        </div>
      </div>
      <el-table :data="tasks" style="width: 100%" v-if="tasks.length > 0">
        <el-table-column prop="contractName" label="合同名称" width="150"></el-table-column>
        <el-table-column prop="taskName" label="任务名称" width="150"></el-table-column>
        <el-table-column prop="amount" label="金额" width="120">
          <template slot-scope="scope">¥{{ Number(scope.row.amount).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="dueDate" label="到期日期" width="120"></el-table-column>
        <el-table-column prop="owner" label="负责人" width="100"></el-table-column>
        <el-table-column label="进度" width="120">
          <template slot-scope="scope">
            <el-progress :percentage="getTaskProgress(scope.row)" :status="getProgressStatus(scope.row)" :show-text="false"></el-progress>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="getTaskStatusType(scope.row.status)" size="small">{{ getTaskStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button size="mini" type="primary" v-if="scope.row.status === 'PENDING' || scope.row.status === 'IN_PROGRESS'" @click="completeTask(scope.row.id)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-else style="text-align: center; padding: 60px; color: #909399">
        <el-icon name="document" style="font-size: 48px; margin-bottom: 20px"></el-icon>
        <p>暂无履约任务</p>
        <p style="font-size: 12px; margin-top: 10px">合同审批通过后会自动生成履约任务</p>
      </div>
    </el-card>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'Performance',
  data() {
    return {
      tasks: []
    }
  },
  computed: {
    pendingCount() {
      return this.tasks.filter(t => t.status === 'PENDING').length
    },
    upcomingCount() {
      return this.tasks.filter(t => this.isUpcoming(t.dueDate) && t.status !== 'COMPLETED').length
    }
  },
  mounted() {
    this.loadTasks()
    this.timer = setInterval(() => this.loadTasks(), 60000)
  },
  beforeDestroy() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    async loadTasks() {
      try {
        const res = await api.getTasks()
        this.tasks = res.data
      } catch (e) {
        this.$message.error('加载失败')
      }
    },
    async completeTask(taskId) {
      try {
        await api.completeTask(taskId)
        this.$message.success('任务已完成')
        this.loadTasks()
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    getTaskStatusText(status) {
      const map = {
        PENDING: '待处理',
        IN_PROGRESS: '进行中',
        COMPLETED: '已完成',
        EXPIRED: '已逾期'
      }
      return map[status] || status
    },
    getTaskStatusType(status) {
      if (status === 'COMPLETED') return 'success'
      if (status === 'EXPIRED') return 'danger'
      if (status === 'IN_PROGRESS') return 'warning'
      return 'info'
    },
    getTaskProgress(task) {
      if (task.status === 'COMPLETED') return 100
      if (task.status === 'EXPIRED') return 0
      return 50
    },
    getProgressStatus(task) {
      if (task.status === 'COMPLETED') return 'success'
      if (task.status === 'EXPIRED') return 'exception'
      if (this.isUpcoming(task.dueDate)) return 'warning'
      return ''
    },
    isUpcoming(dueDate) {
      if (!dueDate) return false
      const now = new Date()
      const due = new Date(dueDate)
      const diff = due - now
      const days = diff / (1000 * 60 * 60 * 24)
      return days > 0 && days <= 3
    }
  }
}
</script>
