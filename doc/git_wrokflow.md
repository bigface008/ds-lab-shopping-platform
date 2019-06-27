# Git 工作流

## <在任意分支> 创建新的工作分支来开始工作

   1. 选择基础分支

      `git checkout -b {base-branch} origin/{base-branch}`

      或

      `git checkout {base-branch}`

   2. `git pull`

   3. 创建新的工作分支

      `git checkout -b {your-branch}`

## <在工作分支> 在上次的工作分支继续工作

   1. `git fetch`

   2. 从基础分支合并

      `git merge origin/{base-branch}`

   3. (视情况) 处理冲突

   4. (视情况) 提交合并

      `git commit ...`

## <在工作分支> 完成部分工作

   1. 完成部分工作 (编写代码/文档)

   2. 提交工作

      `git commit ...`

## <在工作分支> 完成所有工作后

   1. git fetch

   2. 从目标分支合并

      `git merge origin/{target-branch}`

   3. (视情况) 处理冲突

   4. (视情况) 提交合并

      `git commit ...`

   5. 推送工作到 Github

      `git push --set-upstream origin {your-branch}`

   6. 创建 PR

      1. 在项目主页点击 **New pull request** 按钮

      2. **base** 选择 {target-branch}, **compare** 选择 {your-branch}

      3. 点击 **Create pull request**
