# Spring activiti demo api

### 开发环境
* SpringBoot
* JPA
* Activiti 7.0

### 功能如下 
* 创建新流程
* 流程进入下一步
* 流程撤销
* 流程回退
* 完成流程
* 流程调用接口

### 时间线

* 开始：2021-01-28
* 结束：2021-02-10

### 特殊说明

完成简单功能即可，时间充足时，可完成前端页面，本项目采用请假示例

### 知识普及
```
ACT_RE_*: RE表示repository，这个前缀的表包含了流程定义和流程静态资源
ACT_RU_*: RU表示runtime，这些运行时的表，包含流程实例，任务，变量，异步任务等运行中的数据。Activiti只在流程实例执行过程中保存这些数据， 在流程结束时就会删除这些记录。
ACT_ID_*: ID表示identity，这些表包含身份信息，比如用户，组等。这些表现在已废弃。
ACT_HI_*: HI表示history，这些表包含历史数据，比如历史流程实例， 变量，任务等。
ACT_GE_*: 通用数据， 用于不同场景下。
ACT_EVT_*: EVT表示EVENT，目前只有一张表ACT_EVT_LOG，存储事件处理日志，方便管理员跟踪处理
```
7.0版本 生成25张表