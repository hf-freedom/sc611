<template>
  <div id="app">
    <el-container style="height: 100vh">
      <el-header style="background: #001529; color: white; padding: 0 20px; display: flex; align-items: center; justify-content: space-between">
        <div style="display: flex; align-items: center">
          <h2 style="margin: 0; color: white">合同审批履约系统</h2>
          <el-menu mode="horizontal" :default-active="activeMenu" background="#001529" text-color="#fff" active-text-color="#ffd04b" style="margin-left: 30px" @select="handleSelect">
            <el-menu-item index="/">
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/contracts">
              <span>合同管理</span>
            </el-menu-item>
            <el-menu-item index="/performance">
              <span>履约管理</span>
            </el-menu-item>
            <el-menu-item index="/risk">
              <span>风险监控</span>
            </el-menu-item>
          </el-menu>
        </div>
        <div style="display: flex; align-items: center">
          <span style="margin-right: 10px">当前角色：</span>
          <el-select v-model="currentRole" @change="changeRole" style="width: 120px">
            <el-option label="业务人员" value="creator"></el-option>
            <el-option label="法务专员" value="legal"></el-option>
            <el-option label="财务专员" value="financial"></el-option>
            <el-option label="经理" value="manager"></el-option>
            <el-option label="总监" value="director"></el-option>
          </el-select>
        </div>
      </el-header>
      <el-main style="background: #f0f2f5; padding: 20px">
        <router-view/>
      </el-main>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      currentRole: 'creator'
    }
  },
  computed: {
    activeMenu() {
      return this.$route.path
    }
  },
  watch: {
    '$route.path'() {
    }
  },
  mounted() {
    const savedRole = localStorage.getItem('currentRole')
    if (savedRole) {
      this.currentRole = savedRole
    }
  },
  methods: {
    handleSelect(index) {
      this.$router.push(index)
    },
    changeRole(val) {
      localStorage.setItem('currentRole', val)
      this.$message.success(`已切换到${this.getRoleText(val)}角色`)
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
    }
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  margin: 0;
  padding: 0;
}
.el-menu-item a {
  color: inherit;
  text-decoration: none;
}
</style>
