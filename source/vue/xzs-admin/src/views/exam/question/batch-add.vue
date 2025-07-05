<template>
  <div class="app-container" style="display: flex; flex-direction: column;">
    <el-form :model="form" ref="form" label-width="100px" v-loading="formLoading" :rules="rules" style="flex: 1; overflow-y: auto;">
      <el-form-item label="年级：" prop="gradeLevel" required>
        <el-select v-model="form.gradeLevel" placeholder="年级" @change="levelChange" clearable>
          <el-option v-for="item in levelEnum" :key="item.key" :value="item.key" :label="item.value"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="学科：" prop="subjectId" required>
        <el-select v-model="form.subjectId" placeholder="学科">
          <el-option v-for="item in subjectFilter" :key="item.id" :value="item.id" :label="item.name+' ( '+item.levelName+' )'"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="题目JSON：" prop="questionsJson" required>
        <el-input
          type="textarea"
          v-model="form.questionsJson"
          :rows="36"
          placeholder="请输入JSON格式的题目数组，每个题目包含title、items、analyze、correct、score、difficult等字段"
          @input="validateJson"
          style="min-height: calc(100vh - 400px);"
        ></el-input>
        <div style="margin-top: 10px;">
          <el-button size="mini" type="info" @click="showJsonExample">查看JSON格式说明</el-button>
          <el-button size="mini" type="primary" @click="insertExample(1)">单选题示例</el-button>
          <el-button size="mini" type="primary" @click="insertExample(2)">多选题示例</el-button>
          <el-button size="mini" type="primary" @click="insertExample(3)">判断题示例</el-button>
          <el-button size="mini" type="primary" @click="insertExample(4)">填空题示例</el-button>
          <el-button size="mini" type="primary" @click="insertExample(5)">简答题示例</el-button>
          <el-button size="mini" type="success" @click="formatJson">格式化JSON</el-button>
        </div>
        <div v-if="jsonError" style="color: red; margin-top: 5px;">{{ jsonError }}</div>
        <div v-else-if="form.questionsJson && !jsonError" style="color: green; margin-top: 5px;">JSON格式正确，共 {{ questionsCount }} 道题目</div>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm" :disabled="!isFormValid">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
        <el-button @click="$router.push('/exam/question/list')">返回</el-button>
      </el-form-item>
    </el-form>
    <el-dialog title="JSON格式说明文档" :visible.sync="jsonExampleVisible" width="70%">
      <div>
        <p style="margin-bottom: 10px; color: #666;">请仔细阅读以下格式说明，特别注意标记⚠️的地方：</p>
        <el-input
          type="textarea"
          :value="jsonExample"
          :rows="25"
          readonly
          style="font-family: 'Courier New', monospace; font-size: 12px;"
        ></el-input>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="jsonExampleVisible = false">关闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { mapGetters, mapState, mapActions } from 'vuex'
import questionApi from '@/api/question'

export default {
  data() {
    return {
      form: {
        gradeLevel: null,
        subjectId: null,
        questionsJson: ''
      },
      subjectFilter: null,
      formLoading: false,
      jsonError: '',
      questionsCount: 0,
      jsonExampleVisible: false,
      jsonExample: '',
      rules: {
        gradeLevel: [
          { required: true, message: '请选择年级', trigger: 'change' }
        ],
        subjectId: [
          { required: true, message: '请选择学科', trigger: 'change' }
        ],
        questionsJson: [
          { required: true, message: '请输入题目JSON', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    let _this = this
    this.initSubject(function () {
      _this.subjectFilter = _this.subjects
    })
  },
  methods: {
    levelChange() {
      this.form.subjectId = null
      this.subjectFilter = this.subjects.filter(data => data.level === this.form.gradeLevel)
    },
    validateJson() {
      if (!this.form.questionsJson.trim()) {
        this.jsonError = ''
        this.questionsCount = 0
        return
      }
      
      try {
        const questions = JSON.parse(this.form.questionsJson)
        if (!Array.isArray(questions)) {
          this.jsonError = 'JSON必须是数组格式'
          this.questionsCount = 0
          return
        }
        
        // 验证每个题目的格式
        for (let i = 0; i < questions.length; i++) {
          const question = questions[i]
          
          // 基础字段验证
          if (!question.title || !question.analyze || !question.score || question.difficult === undefined) {
            this.jsonError = `第${i + 1}道题目格式不正确，请检查是否包含title、analyze、score、difficult字段`
            this.questionsCount = 0
            return
          }
          
          // 验证questionType字段
          if (!question.questionType || ![1, 2, 3, 4, 5].includes(question.questionType)) {
            this.jsonError = `第${i + 1}道题目的questionType字段不正确，应为1-5的数字`
            this.questionsCount = 0
            return
          }
          
          // 根据题型验证特定字段
          switch (question.questionType) {
            case 1: // 单选题
            case 3: // 判断题
              if (!question.items || !Array.isArray(question.items) || question.items.length === 0) {
                this.jsonError = `第${i + 1}道题目缺少items字段或格式不正确`
                this.questionsCount = 0
                return
              }
              if (!question.correct) {
                this.jsonError = `第${i + 1}道题目缺少correct字段`
                this.questionsCount = 0
                return
              }
              // 验证items格式
              for (let j = 0; j < question.items.length; j++) {
                const item = question.items[j]
                if (!item.prefix || !item.content) {
                  this.jsonError = `第${i + 1}道题目的第${j + 1}个选项格式不正确，应包含prefix和content字段`
                  this.questionsCount = 0
                  return
                }
              }
              break
            case 2: // 多选题
              if (!question.items || !Array.isArray(question.items) || question.items.length === 0) {
                this.jsonError = `第${i + 1}道题目缺少items字段或格式不正确`
                this.questionsCount = 0
                return
              }
              if (!question.correctArray || !Array.isArray(question.correctArray) || question.correctArray.length === 0) {
                this.jsonError = `第${i + 1}道题目缺少correctArray字段或格式不正确`
                this.questionsCount = 0
                return
              }
              // 验证items格式
              for (let j = 0; j < question.items.length; j++) {
                const item = question.items[j]
                if (!item.prefix || !item.content) {
                  this.jsonError = `第${i + 1}道题目的第${j + 1}个选项格式不正确，应包含prefix和content字段`
                  this.questionsCount = 0
                  return
                }
              }
              break
            case 4: // 填空题
              if (!question.items || !Array.isArray(question.items)) {
                this.jsonError = `第${i + 1}道题目缺少items字段或格式不正确`
                this.questionsCount = 0
                return
              }
              // 填空题的items不能为空数组，需要包含填空项
              if (question.items.length === 0) {
                this.jsonError = `第${i + 1}道填空题的items不能为空，需要包含填空项`
                this.questionsCount = 0
                return
              }
              // 验证填空题items格式
              let totalScore = 0
              for (let j = 0; j < question.items.length; j++) {
                const item = question.items[j]
                if (!item.itemUuid || !item.prefix || !item.content || item.score === undefined) {
                  this.jsonError = `第${i + 1}道题目的第${j + 1}个填空项格式不正确，应包含itemUuid、prefix、content、score字段`
                  this.questionsCount = 0
                  return
                }
                totalScore += Number(item.score)
                
                // 检查标题中是否包含对应的gapfilling-span标签
                const spanRegex = new RegExp(`<span class="gapfilling-span ${item.itemUuid}">.*?</span>`)
                if (!spanRegex.test(question.title)) {
                  this.jsonError = `第${i + 1}道题目的标题中缺少对应的填空标记：<span class="gapfilling-span ${item.itemUuid}">${item.prefix}</span>`
                  this.questionsCount = 0
                  return
                }
              }
              // 验证总分是否等于各空分值之和
              if (totalScore !== Number(question.score)) {
                this.jsonError = `第${i + 1}道填空题的总分(${question.score})不等于各空分值之和(${totalScore})`
                this.questionsCount = 0
                return
              }
              break
            case 5: // 简答题
              if (!question.correct) {
                this.jsonError = `第${i + 1}道题目缺少correct字段`
                this.questionsCount = 0
                return
              }
              // 简答题的items可为空数组
              break
          }
        }
        
        this.jsonError = ''
        this.questionsCount = questions.length
      } catch (e) {
        this.jsonError = 'JSON格式错误：' + e.message
        this.questionsCount = 0
      }
    },
    submitForm() {
      let _this = this
      this.$refs.form.validate((valid) => {
        if (valid && this.isFormValid) {
          this.formLoading = true
          
          try {
            const questions = JSON.parse(this.form.questionsJson)
            let successCount = 0
            let errorCount = 0
            
            // 逐个提交题目
            const submitPromises = questions.map(question => {
              const questionData = {
                id: null,
                questionType: question.questionType, // 使用JSON中的questionType
                gradeLevel: this.form.gradeLevel,
                subjectId: this.form.subjectId,
                title: question.title,
                items: question.items || [],
                analyze: question.analyze,
                score: question.score,
                difficult: question.difficult
              }
              
              // 根据题型设置不同的正确答案字段
              if (question.questionType === 2) {
                // 多选题使用correctArray
                questionData.correct = ''
                questionData.correctArray = question.correctArray
              } else {
                // 其他题型使用correct
                questionData.correct = question.correct
              }
              
              return questionApi.edit(questionData).then(re => {
                if (re.code === 1) {
                  successCount++
                } else {
                  errorCount++
                  console.error('题目添加失败:', re.message)
                }
              }).catch(e => {
                errorCount++
                console.error('题目添加异常:', e)
              })
            })
            
            Promise.all(submitPromises).then(() => {
              this.formLoading = false
              if (errorCount === 0) {
                this.$message.success(`成功添加 ${successCount} 道题目`)
                this.$router.push('/exam/question/list')
              } else {
                this.$message.warning(`成功添加 ${successCount} 道题目，失败 ${errorCount} 道`)
              }
            })
            
          } catch (e) {
            this.formLoading = false
            this.$message.error('JSON解析失败：' + e.message)
          }
        } else {
          return false
        }
      })
    },
    resetForm() {
      this.$refs['form'].resetFields()
      this.form = {
        gradeLevel: null,
        subjectId: null,
        questionsJson: ''
      }
      this.jsonError = ''
      this.questionsCount = 0
    },
    showJsonExample() {
      this.jsonExample = `批量添加题目JSON格式说明

【基本要求】
- JSON必须是数组格式，包含多个题目对象
- 每个题目必须包含：questionType、title、analyze、score、difficult字段
- questionType：1=单选题，2=多选题，3=判断题，4=填空题，5=简答题

【题型详解】

1. 单选题 (questionType: 1)
{
  "questionType": 1,
  "title": "题目内容",
  "items": [
    {"prefix": "A", "content": "选项A"},
    {"prefix": "B", "content": "选项B"},
    {"prefix": "C", "content": "选项C"},
    {"prefix": "D", "content": "选项D"}
  ],
  "analyze": "解析内容",
  "correct": "A",  // 正确答案，单个选项
  "score": 5,
  "difficult": 1
}

2. 多选题 (questionType: 2) ⚠️ 注意：使用correctArray字段
{
  "questionType": 2,
  "title": "题目内容",
  "items": [
    {"prefix": "A", "content": "选项A"},
    {"prefix": "B", "content": "选项B"},
    {"prefix": "C", "content": "选项C"},
    {"prefix": "D", "content": "选项D"}
  ],
  "analyze": "解析内容",
  "correctArray": ["A", "B"],  // 正确答案数组，不是correct字段
  "score": 5,
  "difficult": 2
}

3. 判断题 (questionType: 3)
{
  "questionType": 3,
  "title": "题目内容",
  "items": [
    {"prefix": "A", "content": "是"},
    {"prefix": "B", "content": "否"}
  ],
  "analyze": "解析内容",
  "correct": "A",  // A=是，B=否
  "score": 5,
  "difficult": 1
}

4. 填空题 (questionType: 4) ⚠️ 注意：格式最复杂，标题中需要包含span标签
{
  "questionType": 4,
  "title": "题目内容<span class=\\"gapfilling-span 4f8f08c9-8fc2-41ba-89b3-a8654720fb34\\">1</span>",
  "items": [
    {
      "itemUuid": "4f8f08c9-8fc2-41ba-89b3-a8654720fb34",  // 每空一个唯一的UUID，必须与标题中的UUID对应
      "prefix": "1",
      "content": "填空答案",
      "score": 1  // 每个空的分值
    }
  ],
  "analyze": "解析内容",
  "correct": "",  // 填空题为空字符串
  "score": 1,     // 总分必须等于各空分值之和
  "difficult": 2
}

5. 简答题 (questionType: 5)
{
  "questionType": 5,
  "title": "题目内容",
  "items": [],  // 简答题items为空数组
  "analyze": "解析内容",
  "correct": "标准答案",  // 简答题的标准答案
  "score": 10,
  "difficult": 3
}

【常见错误】
❌ 多选题使用correct字段（应该用correctArray）
❌ 填空题标题缺少span标签
❌ 填空题itemUuid与标题中的UUID不匹配
❌ 填空题总分不等于各空分值之和
❌ 简答题items不为空数组
❌ JSON格式错误（缺少引号、逗号等）

【完整示例】
[
  {
    "questionType": 1,
    "title": "单选题示例",
    "items": [{"prefix": "A", "content": "选项A"}, {"prefix": "B", "content": "选项B"}],
    "analyze": "解析",
    "correct": "A",
    "score": 5,
    "difficult": 1
  },
  {
    "questionType": 2,
    "title": "多选题示例",
    "items": [{"prefix": "A", "content": "选项A"}, {"prefix": "B", "content": "选项B"}],
    "analyze": "解析",
    "correctArray": ["A", "B"],
    "score": 5,
    "difficult": 2
  }
]`
      this.jsonExampleVisible = true
    },
    insertExample(questionType) {
      let example = ''
      switch (questionType) {
        case 1: // 单选题
          example = `{
  "questionType": 1,
  "title": "单选题示例",
  "items": [
    {"prefix": "A", "content": "选项A"},
    {"prefix": "B", "content": "选项B"},
    {"prefix": "C", "content": "选项C"},
    {"prefix": "D", "content": "选项D"}
  ],
  "analyze": "单选题解析",
  "correct": "A",
  "score": 5,
  "difficult": 1
}`
          break
        case 2: // 多选题
          example = `{
  "questionType": 2,
  "title": "多选题示例",
  "items": [
    {"prefix": "A", "content": "选项A"},
    {"prefix": "B", "content": "选项B"},
    {"prefix": "C", "content": "选项C"},
    {"prefix": "D", "content": "选项D"}
  ],
  "analyze": "多选题解析",
  "correctArray": ["A", "B"],
  "score": 5,
  "difficult": 2
}`
          break
        case 3: // 判断题
          example = `{
  "questionType": 3,
  "title": "判断题示例",
  "items": [
    {"prefix": "A", "content": "是"},
    {"prefix": "B", "content": "否"}
  ],
  "analyze": "判断题解析",
  "correct": "A",
  "score": 5,
  "difficult": 1
}`
          break
        case 4: // 填空题
          example = `{
  "questionType": 4,
  "title": "填空题示例<span class=\\"gapfilling-span 4f8f08c9-8fc2-41ba-89b3-a8654720fb34\\">1</span>",
  "items": [
    {
      "itemUuid": "4f8f08c9-8fc2-41ba-89b3-a8654720fb34",
      "prefix": "1",
      "content": "填空题答案1",
      "score": 1
    }
  ],
  "analyze": "填空题解析",
  "correct": "",
  "score": 1,
  "difficult": 2
}`
          break
        case 5: // 简答题
          example = `{
  "questionType": 5,
  "title": "简答题示例",
  "items": [],
  "analyze": "简答题解析",
  "correct": "简答题答案",
  "score": 10,
  "difficult": 3
}`
          break
      }
      
      // 如果当前文本框为空，直接插入示例
      if (!this.form.questionsJson.trim()) {
        this.form.questionsJson = `[${example}]`
      } else {
        // 直接追加到现有内容中
        try {
          const currentJson = JSON.parse(this.form.questionsJson)
          if (Array.isArray(currentJson)) {
            currentJson.push(JSON.parse(example))
            this.form.questionsJson = JSON.stringify(currentJson, null, 2)
          } else {
            this.form.questionsJson = `[${JSON.stringify(currentJson)}, ${example}]`
          }
        } catch (e) {
          this.form.questionsJson = `[${this.form.questionsJson}, ${example}]`
        }
      }
      
      this.validateJson()
      this.$message.success('示例已追加到文本框')
    },
    formatJson() {
      if (!this.form.questionsJson.trim()) {
        this.$message.warning('请先输入JSON内容')
        return
      }
      
      try {
        const parsed = JSON.parse(this.form.questionsJson)
        this.form.questionsJson = JSON.stringify(parsed, null, 2)
        this.validateJson()
        this.$message.success('JSON格式化完成')
      } catch (e) {
        this.$message.error('JSON格式错误，无法格式化：' + e.message)
      }
    },
    ...mapActions('exam', { initSubject: 'initSubject' })
  },
  computed: {
    isFormValid() {
      return this.form.gradeLevel && this.form.subjectId && this.form.questionsJson && !this.jsonError && this.questionsCount > 0
    },
    ...mapState('enumItem', {
      levelEnum: state => state.user.levelEnum
    }),
    ...mapState('exam', { subjects: state => state.subjects })
  }
}
</script>

<style scoped>
.el-textarea {
  font-family: 'Courier New', monospace;
}
::v-deep .el-form-item__label {
  width: 120px !important;
}
::v-deep .el-form-item__content {
  margin-left: 120px !important;
}
</style> 