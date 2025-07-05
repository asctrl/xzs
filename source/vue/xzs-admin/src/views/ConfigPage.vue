<template>
  <div>
    <el-form label-width="120px">
      <el-form-item label="显示解析">
        <el-switch v-model="form.show_analyze"></el-switch>
      </el-form-item>
      <el-form-item label="显示正确答案">
        <el-switch v-model="form.show_correct"></el-switch>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveConfig">保存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data() {
    return {
      form: {
        show_analyze: false,
        show_correct: false
      }
    }
  },
  created() {
    axios.get('/api/config/all').then(res => {
      this.form.show_analyze = res.data.show_analyze === '1'
      this.form.show_correct = res.data.show_correct === '1'
    })
  },
  methods: {
    saveConfig() {
      axios.post('/api/config/update', {
        show_analyze: this.form.show_analyze ? '1' : '0',
        show_correct: this.form.show_correct ? '1' : '0'
      }).then(() => {
        this.$message.success('保存成功')
      })
    }
  }
}
</script> 