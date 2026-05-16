<template>
  <div class="contract-list">
    <el-card>
      <div slot="header" style="display: flex; justify-content: space-between; align-items: center">
        <span>合同列表</span>
        <el-button type="primary" @click="showCreateDialog">新建合同</el-button>
      </div>

      <el-table :data="contracts" style="width: 100%" v-if="contracts.length > 0">
        <el-table-column prop="name" label="合同名称" width="150"></el-table-column>
        <el-table-column prop="type" label="合同类型" width="100">
          <template slot-scope="scope">
            <el-tag size="small">{{ getTypeText(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="合同金额" width="120" sortable>
          <template slot-scope="scope">
            ¥{{ Number(scope.row.amount).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="创建人" width="100"></el-table-column>
        <el-table-column label="审批进度" width="150">
          <template slot-scope="scope">
            <el-progress :percentage="getApprovalProgress(scope.row)" :show-text="false" style="width: 80px"></el-progress>
            <span style="margin-left: 8px; font-size: 12px">{{ getApprovalProgress(scope.row) }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="当前审批人" width="120">
          <template slot-scope="scope">
            <span v-if="scope.row.currentApprover">{{ scope.row.currentApprover }}</span>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220">
          <template slot-scope="scope">
            <el-button size="mini" @click="viewDetail(scope.row.id)">查看详情</el-button>
            <el-button size="mini" type="primary" v-if="scope.row.status === 'DRAFT'" @click="submitApproval(scope.row.id)">提交审批</el-button>
            <el-button size="mini" type="success" v-if="canApprove(scope.row)" @click="viewDetail(scope.row.id)">去审批</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-else style="text-align: center; padding: 60px; color: #909399">
        <el-icon name="document" style="font-size: 48px; margin-bottom: 20px"></el-icon>
        <p>暂无合同数据</p>
        <el-button type="primary" @click="showCreateDialog" style="margin-top: 20px">新建合同</el-button>
      </div>
    </el-card>

    <el-dialog title="新建合同" :visible.sync="dialogVisible" width="700px">
      <el-form :model="form" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="合同名称" required>
              <el-input v-model="form.name"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同类型" required>
              <el-select v-model="form.type" style="width: 100%">
                <el-option label="采购合同" value="PURCHASE"></el-option>
                <el-option label="销售合同" value="SALES"></el-option>
                <el-option label="服务合同" value="SERVICE"></el-option>
                <el-option label="合作合同" value="COOPERATION"></el-option>
                <el-option label="其他合同" value="OTHER"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="合同金额" required>
              <el-input-number v-model="form.amount" :min="0" style="width: 100%"></el-input-number>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="创建人" required>
              <el-input v-model="form.creator"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="甲方">
              <el-input v-model="form.partyA"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="乙方">
              <el-input v-model="form.partyB"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="合同内容">
          <el-input type="textarea" v-model="form.content" rows="3"></el-input>
        </el-form-item>
        <el-form-item label="必备条款">
          <el-checkbox-group v-model="form.requiredClauses">
            <el-checkbox label="违约责任"></el-checkbox>
            <el-checkbox label="争议解决"></el-checkbox>
            <el-checkbox label="保密条款"></el-checkbox>
            <el-checkbox label="生效条件"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="付款节点">
          <div v-for="(node, index) in form.paymentNodes" :key="index" style="margin-bottom: 10px">
            <el-input v-model="node.name" placeholder="节点名称" style="width: 150px; margin-right: 10px"></el-input>
            <el-input-number v-model="node.amount" :min="0" placeholder="金额" style="margin-right: 10px"></el-input-number>
            <el-date-picker v-model="node.dueDate" type="date" placeholder="付款日期" style="margin-right: 10px"></el-date-picker>
            <el-button size="mini" type="danger" icon="el-icon-delete" @click="removePaymentNode(index)"></el-button>
          </div>
          <el-button size="mini" type="primary" icon="el-icon-plus" @click="addPaymentNode">添加节点</el-button>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createContract">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import api from '../api'

export default {
  name: 'ContractList',
  data() {
    return {
      contracts: [],
      dialogVisible: false,
      form: {
        name: '',
        type: '',
        amount: 0,
        creator: '',
        partyA: '',
        partyB: '',
        content: '',
        requiredClauses: [],
        paymentNodes: []
      }
    }
  },
  computed: {
    currentRole() {
      return localStorage.getItem('currentRole') || 'creator'
    }
  },
  mounted() {
    this.loadContracts()
  },
  methods: {
    canApprove(contract) {
      const status = contract.status
      const currentNodeIndex = contract.currentNodeIndex || 0
      const approvalFlow = contract.approvalFlow || []
      const currentNode = approvalFlow[currentNodeIndex]
      const roleNodeMap = {
        'legal': 'LEGAL',
        'financial': 'FINANCIAL',
        'manager': 'MANAGER',
        'director': 'DIRECTOR'
      }
      if (status === 'LEGAL_REVIEW' && this.currentRole === 'legal') {
        return true
      }
      if (status === 'FINANCIAL_REVIEW' && this.currentRole === 'financial') {
        return true
      }
      if (['PENDING_APPROVAL', 'LEGAL_REVIEW', 'FINANCIAL_REVIEW'].includes(status) && currentNode) {
        return roleNodeMap[this.currentRole] === currentNode
      }
      return false
    },
    async loadContracts() {
      try {
        const res = await api.getContracts()
        this.contracts = res.data
      } catch (e) {
        this.$message.error('加载合同列表失败')
      }
    },
    showCreateDialog() {
      console.log('打开新建合同对话框')
      this.form = {
        name: '',
        type: '',
        amount: 0,
        creator: '',
        partyA: '',
        partyB: '',
        content: '',
        requiredClauses: [],
        paymentNodes: []
      }
      this.dialogVisible = true
    },
    addPaymentNode() {
      this.form.paymentNodes.push({
        name: '',
        amount: 0,
        dueDate: '',
        description: ''
      })
    },
    removePaymentNode(index) {
      this.form.paymentNodes.splice(index, 1)
    },
    async createContract() {
      try {
        await api.createContract(this.form)
        this.$message.success('创建成功')
        this.dialogVisible = false
        this.loadContracts()
      } catch (e) {
        this.$message.error('创建失败')
      }
    },
    async submitApproval(id) {
      try {
        const res = await api.submitApproval(id)
        if (res.data.success) {
          this.$message.success('提交成功')
          this.loadContracts()
        } else {
          this.$message.error(res.data.message)
        }
      } catch (e) {
        this.$message.error('提交失败')
      }
    },
    viewDetail(id) {
      this.$router.push(`/contracts/${id}`)
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
    getApprovalProgress(contract) {
      if (contract.status === 'EFFECTIVE' || contract.status === 'RISK') {
        return 100
      }
      if (contract.status === 'DRAFT') {
        return 0
      }
      const flow = contract.approvalFlow || []
      const current = contract.currentNodeIndex || 0
      if (flow.length === 0) return 0
      return Math.round((current / flow.length) * 100)
    }
  }
}
</script>
