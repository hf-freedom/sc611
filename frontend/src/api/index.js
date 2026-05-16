import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8004/api',
  timeout: 10000
})

export default {
  getContracts() {
    return api.get('/contracts')
  },
  getContract(id) {
    return api.get(`/contracts/${id}`)
  },
  createContract(data) {
    return api.post('/contracts', data)
  },
  updateContract(id, data) {
    return api.put(`/contracts/${id}`, data)
  },
  submitApproval(id) {
    return api.post(`/contracts/${id}/submit`)
  },
  approveContract(data) {
    return api.post('/contracts/approve', data)
  },
  rejectContract(data) {
    return api.post('/contracts/reject', data)
  },
  getStatistics() {
    return api.get('/contracts/statistics')
  },
  validateLegal(id) {
    return api.get(`/contracts/${id}/validate-legal`)
  },
  validatePayment(id) {
    return api.get(`/contracts/${id}/validate-payment`)
  },
  getTasks() {
    return api.get('/performance/tasks')
  },
  getTasksByContract(contractId) {
    return api.get(`/performance/tasks/contract/${contractId}`)
  },
  completeTask(taskId) {
    return api.post(`/performance/tasks/${taskId}/complete`)
  },
  getReminders() {
    return api.get('/performance/reminders')
  }
}
