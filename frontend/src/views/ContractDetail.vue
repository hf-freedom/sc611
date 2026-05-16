<template>
  <div class="contract-detail">
    <el-card v-loading="loading">
      <div slot="header" style="display: flex; justify-content: space-between; align-items: center">
        <span>合同详情 - {{ contract.name }}</span>
        <el-button @click="$router.back()">返回</el-button>
      </div>

      <el-descriptions :column="2" border>
        <el-descriptions-item label="合同名称">{{ contract.name }}</el-descriptions-item>
        <el-descriptions-item label="合同编号">{{ contract.code }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ getTypeText(contract.type) }}</el-descriptions-item>
        <el-descriptions-item label="合同金额">¥{{ Number(contract.amount).toLocaleString() }}</el-descriptions-item>
        <el-descriptions-item label="甲方">{{ contract.partyA }}</el-descriptions-item>
        <el-descriptions-item label="乙方">{{ contract.partyB }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ contract.creator }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(contract.status)">{{ getStatusText(contract.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前审批人">{{ contract.currentApprover || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>必备条款</el-divider>
      <el-tag v-for="clause in contract.requiredClauses" :key="clause" style="margin: 5px">{{ clause }}</el-tag>
      <span v-if="!contract.requiredClauses || contract.requiredClauses.length === 0" style="color: #909399">无</span>

      <el-divider>付款节点</el-divider>
      <el-table :data="contract.paymentNodes || []" size="small">
        <el-table-column prop="name" label="节点名称"></el-table-column>
        <el-table-column prop="amount" label="金额">
          <template slot-scope="scope">¥{{ Number(scope.row.amount).toLocaleString() }}</template>
        </el-table-column>
        <el-table-column prop="dueDate" label="付款日期"></el-table-column>
      </el-table>

      <el-divider>审批流程</el-divider>
      <el-steps :active="getCurrentStep()" finish-status="success" align-center>
        <el-step title="创建草稿"></el-step>
        <el-step title="法务审核" v-if="contract.approvalFlow && contract.approvalFlow.includes('LEGAL')"></el-step>
        <el-step title="财务审核" v-if="contract.approvalFlow && contract.approvalFlow.includes('FINANCIAL')"></el-step>
        <el-step title="经理审批" v-if="contract.approvalFlow && contract.approvalFlow.includes('MANAGER')"></el-step>
        <el-step title="总监审批" v-if="contract.approvalFlow && contract.approvalFlow.includes('DIRECTOR')"></el-step>
        <el-step title="合同生效"></el-step>
      </el-steps>

      <el-divider>审批记录</el-divider>
      <el-table :data="contract.approvalRecords || []" size="small" v-if="contract.approvalRecords && contract.approvalRecords.length > 0">
        <el-table-column prop="node" label="审批节点" width="120">
          <template slot-scope="scope">{{ getNodeText(scope.row.node) }}</template>
        </el-table-column>
        <el-table-column prop="approver" label="审批人" width="120"></el-table-column>
        <el-table-column label="审批结果" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.approved ? 'success' : 'danger'" size="small">
              {{ scope.row.approved ? '通过' : '驳回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="opinion" label="审批意见"></el-table-column>
      </el-table>
      <div v-else style="text-align: center; padding: 20px; color: #909399">
        暂无审批记录
      </div>

      <div style="margin-top: 20px" v-if="showApprovalButtons()">
        <el-card v-if="currentRole === 'legal'">
          <div slot="header">法务条款校验</div>
          <div v-if="legalValidation.length > 0">
            <el-alert title="以下必备条款缺失，请修改合同后再审批" type="error" :closable="false">
              <ul>
                <li v-for="item in legalValidation" :key="item">{{ item }}</li>
              </ul>
            </el-alert>
          </div>
          <el-alert v-else title="所有必备条款已完整" type="success" :closable="false"></el-alert>
        </el-card>

        <el-card v-if="currentRole === 'financial'" style="margin-top: 15px">
          <div slot="header">付款节点校验</div>
          <div v-if="paymentValidation.length > 0">
            <el-alert title="付款节点校验不通过" type="error" :closable="false">
              <ul>
                <li v-for="item in paymentValidation" :key="item">{{ item }}</li>
              </ul>
            </el-alert>
          </div>
          <el-alert v-else title="付款节点校验通过" type="success" :closable="false"></el-alert>
        </el-card>

        <div style="margin-top: 20px; text-align: center">
          <el-button type="primary" @click="showApproveDialog" :disabled="!canApproveNow">
            {{ getApproveButtonText() }}
          </el-button>
          <el-button type="danger" @click="showRejectDialog">驳回</el-button>
        </div>
      </div>
    </el-card>

    <el-dialog :title="getDialogTitle()" :visible.sync="approvalDialogVisible" width="500px">
      <el-form label-width="80px">
        <el-form-item label="审批人">
          <el-input v-model="approvalForm.approver"></el-input>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input type="textarea" v-model="approvalForm.opinion" placeholder="请输入审批意见"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="approve">确认通过</el-button>
      </div>
    </el-dialog>

    <el-dialog title="驳回合同" :visible.sync="rejectDialogVisible" width="500px">
      <el-form label-width="80px">
        <el-form-item label="审批人">
          <el-input v-model="approvalForm.approver"></el-input>
        </el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input type="textarea" v-model="approvalForm.opinion" placeholder="请输入驳回原因（必填）"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="reject">确认驳回</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'ContractDetail',
  data() {
    return {
      loading: false,
      contract: {},
      approvalDialogVisible: false,
      rejectDialogVisible: false,
      approvalForm: {
        approver: '',
        opinion: ''
      }
    }
  },
  computed: {
    currentRole() {
      return localStorage.getItem('currentRole') || 'creator'
    },
    legalValidation() {
      const requiredClauses = ['违约责任', '争议解决', '保密条款', '生效条件']
      const contractClauses = this.contract.requiredClauses || []
      const missing = requiredClauses.filter(clause => !contractClauses.includes(clause))
      return missing
    },
    paymentValidation() {
      const issues = []
      const paymentNodes = this.contract.paymentNodes || []
      if (paymentNodes.length === 0) {
        issues.push('付款节点不能为空')
        return issues
      }
      let totalAmount = 0
      paymentNodes.forEach((node, index) => {
        if (!node.name) {
          issues.push(`第${index + 1}个节点名称不能为空`)
        }
        if (!node.amount || node.amount <= 0) {
          issues.push(`第${index + 1}个节点金额必须大于0`)
        } else {
          totalAmount += Number(node.amount)
        }
        if (!node.dueDate) {
          issues.push(`第${index + 1}个节点付款日期不能为空`)
        }
      })
      if (totalAmount > Number(this.contract.amount || 0)) {
        issues.push('付款节点总金额不能超过合同总金额')
      }
      return issues
    },
    canApproveNow() {
      if (this.currentRole === 'legal') {
        return this.legalValidation.length === 0
      }
      if (this.currentRole === 'financial') {
        return this.paymentValidation.length === 0
      }
      return true
    }
  },
  mounted() {
    this.loadContract()
  },
  methods: {
    async loadContract() {
      this.loading = true
      try {
        const res = await api.getContract(this.$route.params.id)
        this.contract = res.data
      } catch (e) {
        this.$message.error('加载失败')
      } finally {
        this.loading = false
      }
    },
    showApproveDialog() {
      this.approvalForm = { 
        approver: this.getRoleText(this.currentRole), 
        opinion: '' 
      }
      this.approvalDialogVisible = true
    },
    showRejectDialog() {
      this.approvalForm = { 
        approver: this.getRoleText(this.currentRole), 
        opinion: '' 
      }
      this.rejectDialogVisible = true
    },
    async approve() {
      try {
        const res = await api.approveContract({
          contractId: this.contract.id,
          approver: this.approvalForm.approver,
          opinion: this.approvalForm.opinion
        })
        if (res.data.success) {
          this.$message.success('审批成功')
          this.approvalDialogVisible = false
          this.loadContract()
        } else {
          this.$message.error(res.data.message)
        }
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    async reject() {
      if (!this.approvalForm.opinion || !this.approvalForm.opinion.trim()) {
        this.$message.error('驳回原因不能为空')
        return
      }
      try {
        const res = await api.rejectContract({
          contractId: this.contract.id,
          approver: this.approvalForm.approver,
          opinion: this.approvalForm.opinion
        })
        if (res.data.success) {
          this.$message.success('驳回成功')
          this.rejectDialogVisible = false
          this.loadContract()
        } else {
          this.$message.error(res.data.message)
        }
      } catch (e) {
        this.$message.error('操作失败')
      }
    },
    showApprovalButtons() {
      const status = this.contract.status
      const currentNodeIndex = this.contract.currentNodeIndex || 0
      const approvalFlow = this.contract.approvalFlow || []
      const currentNode = approvalFlow[currentNodeIndex]
      const roleNodeMap = {
        'legal': 'LEGAL',
        'financial': 'FINANCIAL',
        'manager': 'MANAGER',
        'director': 'DIRECTOR'
      }
      if (!currentNode) {
        return false
      }
      if (status === 'APPROVED' || status === 'EFFECTIVE' || status === 'REJECTED' || status === 'RISK') {
        return false
      }
      return roleNodeMap[this.currentRole] === currentNode
    },
    getCurrentStep() {
      if (this.contract.status === 'EFFECTIVE' || this.contract.status === 'RISK') {
        return 6
      }
      if (!this.contract.approvalFlow) return 1
      return (this.contract.currentNodeIndex || 0) + 1
    },
    getTypeText(type) {
      const map = {
        PURCHASE: '采购合同',
        SALES: '销售合同',
        SERVICE: '服务合同',
        COOPERATION: '合作合同',
        OTHER: '其他合同'
      }
      return map[type] || type
    },
    getStatusText(status) {
      const map = {
        DRAFT: '草稿',
        PENDING_APPROVAL: '待审批',
        LEGAL_REVIEW: '法务审核',
        FINANCIAL_REVIEW: '财务审核',
        APPROVED: '已审批',
        EFFECTIVE: '已生效',
        REJECTED: '已驳回',
        RISK: '风险'
      }
      return map[status] || status
    },
    getStatusType(status) {
      if (status === 'DRAFT' || status === 'REJECTED') return 'info'
      if (status === 'EFFECTIVE') return 'success'
      if (status === 'RISK') return 'danger'
      return 'warning'
    },
    getNodeText(node) {
      const map = {
        LEGAL: '法务审核',
        FINANCIAL: '财务审核',
        MANAGER: '经理审批',
        DIRECTOR: '总监审批'
      }
      return map[node] || node
    },
    getRoleText(role) {
      const map = {
        creator: '业务人员',
        legal: '法务专员',
        financial: '财务专员',
        manager: '经理',
        director: '总监'
      }
      return map[role] || role
    },
    getDialogTitle() {
      if (this.currentRole === 'legal') {
        return '法务审核'
      } else if (this.currentRole === 'financial') {
        return '财务审核'
      } else {
        return '审批'
      }
    },
    getApproveButtonText() {
      if (this.currentRole === 'legal') {
        return this.legalValidation.length > 0 ? '请先完善必备条款' : '通过法务审核'
      } else if (this.currentRole === 'financial') {
        return this.paymentValidation.length > 0 ? '请先完善付款节点' : '通过财务审核'
      } else {
        return '通过审批'
      }
    }
  }
}
</script>
