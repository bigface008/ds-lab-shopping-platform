# 项目管理

## 项目目录结构

```
ds-lab-shopping-platform/
|-- config/         部署各组件时所编写的配置文件
|   |-- ...
|
|-- doc/            各模块的说明文档、各组件的部署步骤
|   |-- ...
|
|-- src/            各模块的源代码
|   |-- sender/
|   |   |-- ...
|   |
|   |-- {module name}/
|   |   |-- ...
|   |
|   |-- ...
|
|-- README.md       项目说明文档
```

## Git 工作流

### 开发者

#### <在任意分支> 创建新的工作分支来开始工作

   1. 选择一个模块分支作为基础分支

      `git checkout -b {base-branch} origin/{base-branch}`

      或

      `git checkout {base-branch}`

   2. `git pull`

   3. 创建新的工作分支

      `git checkout -b {your-branch}`

#### <在工作分支> 在上次的工作分支继续工作

   1. `git fetch`

   2. 从基础分支合并

      `git merge origin/{base-branch}`

   3. (视情况) 处理冲突

   4. (视情况) 提交合并

      `git commit ...`

#### <在工作分支> 完成部分工作

   1. 完成部分工作 (编写代码/文档)

   2. 提交工作

      `git commit ...`

#### <在工作分支> 完成所有工作后

   1. git fetch

   2. 选择一个模块分支作为目标分支，并从目标分支合并

      `git merge origin/{target-branch}`

   3. (视情况) 处理冲突

   4. (视情况) 提交合并

      `git commit ...`

   5. 推送工作到 Github

      `git push --set-upstream origin`

      或

      `git push`

   6. 创建 PR 来将变更同步到目标分支中

      1. 在项目主页点击 **New pull request** 按钮

      2. **base** 选择 {target-branch}, **compare** 选择 {your-branch}

      3. 右侧面板 **Reviewers** 下选中评审员

      4. 右侧面板 **Assignees** 点击 **assign yourself**

      5. 右侧面板 **Labels** 下选中所有合适的标签

      6. 点击 **Create pull request** 按钮

### 评审员

#### 收到开发者 PR 后

审核并讨论之后选择合并或否决 PR。

若合并则勾选合并后删除原分支。

#### 一个模块完成阶段性工作后

创建 PR

1. 在项目主页点击 **New pull request** 按钮

2. **base** 选择 master, **compare** 选择 模块分支

3. 右侧面板 **Reviewers** 下选中总评审员

4. 右侧面板 **Assignees** 点击 **assign yourself**

5. 右侧面板 **Labels** 下选中所有合适的标签

6. 点击 **Create pull request** 按钮

#### 收到评审员 PR 后

审核并讨论之后选择合并或否决PR。

### 分支管理

远端仓库维护主分支和各模块各一个分支

开发者分支在 PR 后删除

- master
- sender
- receiver
- exchange-rate-updater
- spark-driver
- nginx
- zookeeper
- kafka
- spark
- mysql

## 敏感信息

- 敏感信息包括 ip, port, 用户名, 密码 等等

- **文档** 及 **配置文件** 中的敏感信息需要打码

   如替换为 {server-ip}, {port}, {username}, {password}, {secret} 等等

- 禁止将敏感信息硬编码入 **代码文件** 中，应该通过读取配置文件或者利用运行参数、环境变量来传递它们
