package me.xueyao.util;

import lombok.extern.slf4j.Slf4j;
import me.xueyao.config.DeleteTaskCmd;
import me.xueyao.config.SetFlowNodeAndGoCmd;
import me.xueyao.repository.ActivitiMapper;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.impl.HistoricActivityInstanceQueryProperty;
import org.activiti.engine.impl.HistoricTaskInstanceQueryProperty;
import org.activiti.engine.impl.TaskQueryProperty;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskInfo;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Activiti工具类
 * @author Simon.Xue
 * @date 2/1/21 3:23 PM
 **/
@Slf4j
@Component
@Transactional(rollbackFor = Exception.class)
public class ActivitiUtils {
    private static final String USER_TASK = "userTask";
    private static final String PARALLEL_GATEWAY = "parallelGateway";
    private static final String EXCLUSIVE_GATEWAY = "exclusiveGateway";
    private static final String BPMN_NOT_SUPPORT = "bpmn not support";

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private ProcessRuntime processRuntime;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ActivitiMapper activitiMapper;

    public RepositoryService getRepositoryService() {
        return repositoryService;
    }

    /**
     * 根据路径部署
     * @param bpmnFilePath diagrams/myHelloWorld.bpmn
     * @param bpmnPngPath diagrams/myHelloWorld.png
     */
    public void deployProcessByPath(String bpmnFilePath, String bpmnPngPath) {
        Deployment deploy = repositoryService
                .createDeployment()
                .addClasspathResource(bpmnFilePath)
                .addClasspathResource(bpmnPngPath).deploy();
        log.info("deploy id = {}", deploy.getId());
        log.info("deploy time = {}", deploy.getDeploymentTime());
    }

    /**
     * 删除给定的部署 和 级联删除到流程实例、历史流程实例和作业
     * @param deploymentId deploymentId
     * @param condition 级联删除
     */
    public void deleteDeployment(String deploymentId, Boolean condition) {
        repositoryService.deleteDeployment(deploymentId, condition);
    }

    public void viewPng(String outPutPath) {
        //部署ID
        String deploymentId = "1";
        //获取资源名称
        List<String> list = repositoryService.getDeploymentResourceNames(deploymentId);
        //获得资源名称  后缀.png
        String resourceName = "";
        if (list !=null && !list.isEmpty()) {
            for (String name : list) {
                if (name.contains(".png")) {
                    resourceName = name;
                }
            }
        }
        //获得输入流 存放.png文件
        InputStream in = repositoryService.getResourceAsStream(deploymentId, resourceName);

        try {
            FileUtils.copyInputStreamToFile(in, new File(outPutPath + resourceName));
        } catch (Exception e) {
            log.error("view png error", e);
        }

    }

    /**
     * 设置流程变量
     * @param processInstanceId
     * @param assignee
     * @param variables
     */
    public void setProcessVariables(String processInstanceId, String assignee, Map<String, Object> variables) {
        //查询当前办理人的任务ID
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskAssignee(assignee)
                .singleResult();

        //设置流程变量[基本类型]
        taskService.setVariables(task.getId(), variables);
    }

    /**
     * 查询历史的流程变量
     * @param variableName
     * @return
     */
    public List<HistoricVariableInstance> getHistoryProcessVariable(String variableName) {
        return historyService.createHistoricVariableInstanceQuery()
                .variableName(variableName)
                .list();
    }

    /**
     * 查询组任务
     * @param candidateUser
     * @return
     */
    public List<Task> findGroupTaskList(String candidateUser) {
        return taskService.createTaskQuery()
                .taskCandidateUser(candidateUser)
                .list();
    }

    /**
     * 将组任务指定给某个人->个人任务(拾取任务)
     * @param taskId
     * @param userId
     */
    public void claim(String taskId, String userId) {
        taskService.claim(taskId, userId);
    }

    /**
     * 将个人任务再回退到组任务(前提 之前这个任务是组任务)
     * @param taskId 组任务的id
     */
    public void setAssignee(String taskId) {
        taskService.setAssignee(taskId, null);
    }

    /**
     * 向组任务中添加成员
     * @param taskId 组任务id
     * @param userId 成员id
     */
    public void addGroupUser(String taskId, String userId) {
        taskService.addCandidateUser(taskId, userId);
    }

    /**
     * 删除组任务中成员
     * @param taskId 组任务id
     * @param userId 成员id
     */
    public void deleteGroupUser(String taskId, String userId) {
        taskService.deleteCandidateUser(taskId, userId);
    }


    /**
     * 启动流程实例
     * @param processDefinitionKey
     * @param name
     * @param variable
     * @return
     */
    public ProcessInstance startProcessInstance(String processDefinitionKey, String name, Map<String, Object> variable) {
        ProcessInstance processInstance = processRuntime.start(ProcessPayloadBuilder
                .start()
                .withProcessDefinitionId(processDefinitionKey)
                .withName(name)
                .withVariables(variable)
                .build());

        log.info("process id = {}, process definition id = {}", processInstance.getId()
                , processInstance.getProcessDefinitionId());
        return processInstance;
    }

    /**
     * 创建任务节点(多人审批)
     * @param id
     * @param name
     * @param assignee
     * @return
     */
    public UserTask createUsersTask(String id, String name, List<String> assignee) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setCandidateUsers(assignee);
        return userTask;
    }

    /**
     * 创建任务节点(单人审批)
     * @param id
     * @param name
     * @param assignee
     * @return
     */
    public UserTask createUserTask(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setId(id);
        userTask.setName(name);
        userTask.setAssignee(assignee);
        return userTask;
    }

    /**
     * 完成任务
     * @param taskId
     * @param variables
     */
    public void completeTask(String taskId, Map<String, Object> variables) {
        taskService.setVariables(taskId, variables);
        taskService.complete(taskId);
    }

    /**
     * 处理当前用户的任务
     * @param processDefinitionKey 流程定义的key
     */
    public void completeTaskByProcessDefinitionKey(String processDefinitionKey) {
        Task task = taskService.createTaskQuery()
                .processDefinitionKey(processDefinitionKey)
                .taskAssignee(UserUtils.getCurrentUserDetails().getUsername())
                .singleResult();
        if (task != null) {
            taskService.complete(task.getId());
        }
    }

    /**
     * 查询任务列表
     * @param assignee
     * @return
     */
    public List<Task> queryTaskList(String assignee) {
        return taskService.createTaskQuery().
                taskAssignee(assignee)
                .list();
    }

    /**
     * 创建连线
     * @param from
     * @param to
     * @return
     */
    public SequenceFlow createSequenceFlow(String from, String to) {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }

    /**
     * 开始节点
     * @return
     */
    public StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");
        startEvent.setName("start");
        return startEvent;
    }

    /**
     * 结束节点
     * @return
     */
    public EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        endEvent.setName("end");
        return endEvent;
    }

    /**
     * 查询申请人已申请任务(完成状态)
     * @param user
     * @param processDefinitionKey
     * @return
     */
    public List<HistoricProcessInstance> queryApplyHistory(String user, String processDefinitionKey) {
        return historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .startedBy(user)
                .finished()
                .orderByProcessInstanceEndTime()
                .desc().list();
    }

    /**
     * 审批人已办理任务(完成状态)
     * @param user
     * @param processDefinitionKey
     * @return
     */
    public List<HistoricTaskInstance> queryFinished(String user, String processDefinitionKey) {
        List<HistoricProcessInstance> hisProInstance = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .involvedUser(user)
                .finished()
                .orderByProcessInstanceEndTime()
                .desc()
                .list();

        List<HistoricTaskInstance> historicTaskInstanceList = new LinkedList<>();
        hisProInstance.forEach(h-> {
            List<HistoricTaskInstance> hisTaskInstanceList = historyService.createHistoricTaskInstanceQuery()
                    .processInstanceId(h.getId())
                    .processFinished()
                    .taskAssignee(user)
                    .orderByHistoricTaskInstanceEndTime()
                    .desc().list();
            hisTaskInstanceList.forEach(task-> {
                if (task.getAssignee().equals(user)) {
                    historicTaskInstanceList.add(task);
                }
            });
        });
        return historicTaskInstanceList;
    }

    /**
     * 发起人查询执行中的任务
     * @param user
     * @return
     */
    public List<org.activiti.engine.runtime.ProcessInstance> queryNow(String user) {
        return runtimeService.createProcessInstanceQuery()
                .startedBy(user)
                .list();
    }

    /**
     * 根据人员查询待审批任务
     * @param assignee
     * @return
     */
    public List<Task> findUnApprrove(String assignee) {
        return taskService.createTaskQuery().taskCandidateOrAssigned(assignee).list();
    }

    /**
     * 进行审批
     * @param msg 审批意见
     * @param isAgree 是否同意 1同意 0拒绝
     * @param taskId 任务id
     * @param processId 流程id
     * @return
     */
    public Boolean approve(String msg, Integer isAgree, String taskId, String processId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (isAgree == 0) {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());

            Execution execution = runtimeService.createExecutionQuery()
                    .executionId(task.getExecutionId())
                    .singleResult();

            String activityId = execution.getActivityId();
            FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(activityId);
            //清理流程未执行节点
            flowNode.getOutgoingFlows().clear();
            //建立新方向
            List<SequenceFlow> newSequenceFlowList = new ArrayList<>();
            SequenceFlow sequenceFlow = new SequenceFlow();
            sequenceFlow.setId(String.valueOf(new IdWorker(1,1).nextId()));
            sequenceFlow.setSourceFlowElement(flowNode);
            sequenceFlow.setTargetFlowElement(createEndEvent());
            newSequenceFlowList.add(sequenceFlow);
            flowNode.setOutgoingFlows(newSequenceFlowList);
        } else if (isAgree == 1) {
            // 同意，继续下一个节点
            taskService.addComment(task.getId(), task.getProcessInstanceId(), msg);
            taskService.complete(task.getId());
        }
        return true;
    }

    /**
     * 根据启动key获取最新流程
     * @param processDefinitionKey
     * @return
     */
    public List<ProcessDefinition> getLastestProcess(String processDefinitionKey) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                // 使用流程定义的版本降序排列
                .orderByProcessDefinitionAppVersion()
                .desc()
                .list();
    }

    /**
     * 获取流程走过的线
     * @param bpmnModel
     * @param historicActivityInstances
     * @param historicActivityDoneList
     * @param taskIds
     * @return
     */
    public List<String> getHighLightedFlows(BpmnModel bpmnModel,
                                            List<HistoricActivityInstance> historicActivityInstances,
                                            List<String> historicActivityDoneList,
                                            List<String> taskIds) {
        List<String> highFlows = new ArrayList<>();
        if (historicActivityInstances == null
                && historicActivityInstances.isEmpty()) {
            return highFlows;
        }

        Map<String, HistoricActivityInstance> historicActivityInstanceMap = historicActivityInstances.stream()
                .collect(Collectors
                        .toMap(HistoricActivityInstance::getActivityId,
                                historicActivityInstance -> historicActivityInstance,
                                BinaryOperator.maxBy(Comparator.comparing(HistoricActivityInstance::getId))));

        Map<String, HistoricVariableInstance> historicVariableInstanceMap = getHistoricVariableInstanceMap(historicActivityInstances.get(0).getProcessInstanceId());

        //遍历历史节点
        Map<String, FlowElement> flowElementMap = bpmnModel.getMainProcess().getFlowElements().stream().collect(Collectors.toMap(FlowElement::getId, Function.identity()));
        historicActivityInstances.forEach(historicActivityInstance -> {
            //得到节点定义的详情信息
            FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId());
            //取出节点的所有出去的线，对所有的线进行遍历
            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows();

            pvmTransitions.forEach(pvmTransition -> {
                if (historicActivityDoneList.contains(pvmTransition.getSourceRef())
                        && null != historicActivityInstanceMap.get(pvmTransition.getSourceRef())) {
                    if (null != taskIds && !taskIds.isEmpty()) {
                        if (!taskIds.contains(historicActivityInstanceMap.get(pvmTransition.getSourceRef()).getTaskId())) {
                            if (flowElementMap.get(pvmTransition.getTargetRef()) instanceof ExclusiveGateway
                                    && querySequenceFlowCondition(pvmTransition, historicVariableInstanceMap)) {
                                highFlows.add(pvmTransition.getId());
                            } else {
                                if (!(flowElementMap.get(pvmTransition.getTargetRef()) instanceof EndEvent)
                                        && historicActivityInstanceMap.get(pvmTransition.getTargetRef()) != null
                                        && querySequenceFlowCondition(pvmTransition, historicVariableInstanceMap)) {
                                    highFlows.add((pvmTransition.getId()));
                                }
                            }
                        } else {
                            if (historicActivityInstanceMap.get(pvmTransition.getTargetRef()) != null
                                    && querySequenceFlowCondition(pvmTransition, historicVariableInstanceMap)) {
                                highFlows.add(pvmTransition.getId());
                            }
                        }
                    }
                }
            });
        });
        return highFlows;
    }

    /**
     * 获得历史变量实例 Map
     * @param processInstanceId
     * @return
     */
    private Map<String, HistoricVariableInstance> getHistoricVariableInstanceMap(String processInstanceId) {
        List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();

        Map<String, HistoricVariableInstance> historicVariableInstanceMap = historicVariableInstances.stream()
                .collect(Collectors
                        .toMap(HistoricVariableInstance::getVariableName,
                                historicVariableInstance -> historicVariableInstance,
                                BinaryOperator.maxBy(Comparator.comparing(HistoricVariableInstance::getId))));

        return historicVariableInstanceMap;
    }

    private boolean querySequenceFlowCondition(SequenceFlow pvmTransition, Map<String, HistoricVariableInstance> historicVariableInstanceMap) {
        String conditionExpression = pvmTransition.getConditionExpression();
        if (StringUtils.isEmpty(conditionExpression)) {
            conditionExpression = conditionExpression.substring(conditionExpression.indexOf("{") + 1, conditionExpression.indexOf("}"));
            List<String> strArr = Arrays.asList(conditionExpression.split("=="));
            strArr.forEach(s -> s = s.replace(" ", ""));

            if (historicVariableInstanceMap.get(strArr.get(0)).getValue().equals(strArr.get(1).replaceAll("\"", ""))) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 获取下一个节点信息
     * @param bpmnModel 流程模型
     * @param historicActivityInstanceMap
     * @param i 当前已经遍历的历史节点索引(找下一个节点从此节点后)
     * @param historicActivityInstance 当前遍历到的历史节点实例
     * @return FlowNode 下一个节点信息
     */
    private static List<FlowNode> getNextFlowNode(BpmnModel bpmnModel,
                                                  Map<String, HistoricActivityInstance> historicActivityInstanceMap,
                                                  int i,
                                                  HistoricActivityInstance historicActivityInstance) {
        //保存后一个节点
        List<FlowNode> flowNodeList = new ArrayList<>();
        if (i == historicActivityInstanceMap.size()) {
            return flowNodeList;
        }

        FlowNode activityImpl = (FlowNode) bpmnModel.getMainProcess().getFlowElement(historicActivityInstance.getActivityId());
        List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows();
        if (pvmTransitions.size() == 1) {
            if (historicActivityInstanceMap.get(pvmTransitions.get(0).getTargetRef()) != null) {
                FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess()
                        .getFlowElement(historicActivityInstanceMap
                                .get(pvmTransitions.get(0).getTargetRef())
                                .getActivityId());
                flowNodeList.add(flowNode);
                return flowNodeList;
            }
        } else {
            pvmTransitions.forEach(sequenceFlow -> {
                FlowNode flowNode = (FlowNode) bpmnModel.getMainProcess().getFlowElement(sequenceFlow.getTargetRef());
                flowNodeList.add(flowNode);
            });
            //return flowNodeList;
        }
        return flowNodeList;
    }

    /**
     * 查询当前用户的任务列表
     * @param processDefinitionKey 流程定义的key
     * @return
     */
    public List<Task> findPersonalTaskList(String processDefinitionKey) {
        return taskService.createTaskQuery()
                .processDefinitionKey(processDefinitionKey)
                .taskAssignee(UserUtils.getCurrentUserDetails().getUsername())
                .list();
    }

    /**
     * 查询流程历史信息
     * @param processDefinitionKey
     * @return
     */
    public List<HistoricActivityInstance> queryHistory(String processDefinitionKey) {
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        ProcessDefinition processDefinition = processDefinitionQuery.processDefinitionKey(processDefinitionKey)
                .orderByProcessDefinitionAppVersion()
                .desc()
                .singleResult();

        if (processDefinition != null) {
            HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();
            return query.processDefinitionId(processDefinition.getId())
                    .orderByHistoricActivityInstanceStartTime().asc().list();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * 判断流程是否完成
     * @param processInstanceId
     * @return 已完成 true 未完成 false
     */
    public boolean isFinished(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery()
                .finished()
                .processInstanceId(processInstanceId)
                .count() > 0;
    }

    /**
     * 输出图像
     * @param response 响应实体
     * @param bpmnModel 图像对象
     * @param flowIds 已执行的线集合
     * @param executeActivityIdList  已执行的节点ID集合
     */
    public void outputImg(HttpServletResponse response, BpmnModel bpmnModel,
                          List<String> flowIds, List<String> executeActivityIdList) {
        InputStream imageStream = null;
        try {
            imageStream = new DefaultProcessDiagramGenerator()
                    .generateDiagram(bpmnModel, executeActivityIdList,
                            flowIds, "宋体",
                            "微软雅黑", "黑体",
                            true, "png");

            // 输出资源内容到相应对象
            byte[] b = new byte[1024];
            int len;
            while ((len = imageStream.read(b, 0, 1024)) != -1) {
                response.getOutputStream().write(b, 0, len);
            }
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("out put process img error！ = {}", e);
        } finally { // 流关闭
            try {
                if (imageStream != null) {
                    imageStream.close();
                }
            } catch (IOException e) {
                log.error("IoException, {}", e);
            }
        }
    }

    public void showImg(String instanceKey, HttpServletResponse response) {
        if (null == instanceKey) {
            return;
        }

        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(instanceKey)
                .singleResult();
        if (instanceKey == null) {
            return;
        }

        BpmnModel bpmnModel = repositoryService.getBpmnModel(processInstance.getProcessDefinitionId());

        HistoricActivityInstanceQuery historicInstanceQuery = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(instanceKey);

        List<HistoricActivityInstance> historicActivityInstanceList = historicInstanceQuery
                .orderBy(HistoricActivityInstanceQueryProperty.HISTORIC_ACTIVITY_INSTANCE_ID)
                .asc()
                .list();

        if (historicActivityInstanceList == null || historicActivityInstanceList.isEmpty()) {
            outputImg(response, bpmnModel, null, null);
            return;
        }

        Map<String, FlowNode> flowNodeMap = getFlowNodeMap(historicActivityInstanceList, processInstance.getProcessDefinitionId());
        //处理撤回这种情况 根据id排序
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(instanceKey)
                .orderBy(TaskQueryProperty.TASK_ID)
                .asc()
                .list();

        Set<String> executedActivityIdList = new LinkedHashSet<>();
        List<String> taskIdList = tasks == null ? null : tasks.stream().map(TaskInfo::getId).collect(Collectors.toList());
        List<String> taskKeyList = tasks == null ? null : tasks.stream().map(TaskInfo::getTaskDefinitionKey).collect(Collectors.toList());

        if (tasks != null) {
            //串行
            if (tasks.size() == 1) {
                for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
                    if (historicActivityInstance.getTaskId() == null
                            || historicActivityInstance.getActivityId().equals(tasks.get(0).getTaskDefinitionKey())) {
                        executedActivityIdList.add(historicActivityInstance.getActivityId());

                    } else {
                        executedActivityIdList.add(historicActivityInstance.getActivityId());
                        break;
                    }
                }

            } else {
                List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
                        .processInstanceId(processInstance.getId())
                        .list();

                Map<String, HistoricVariableInstance> historicVariableInstanceMap = historicVariableInstances.stream()
                        .collect(Collectors
                                .toMap(HistoricVariableInstance::getVariableName,
                                        historicVariableInstance -> historicVariableInstance,
                                        BinaryOperator.maxBy(Comparator.comparing(HistoricVariableInstance::getId)))
                        );

                //并行
                Collection<FlowElement> flowElementCollection = bpmnModel.getMainProcess().getFlowElements();
                Map<String, List<String>> parentMap = new HashMap<>(tasks.size());
                for (FlowElement flowElement : flowElementCollection) {
                    List<String> parentCodeList = new LinkedList<>();
                    if (flowNodeMap.get(flowElement.getId()) != null) {
                        List<SequenceFlow> sequenceFlows = flowNodeMap.get(flowElement.getId()).getIncomingFlows();
                        if (sequenceFlows != null && !sequenceFlows.isEmpty()) {
                            for (SequenceFlow sequenceFlow : sequenceFlows) {
                                parentCodeList.add(sequenceFlow.getSourceRef());
                            }
                            parentMap.put(flowElement.getId(), parentCodeList);
                        }
                    }
                }

                Set<String> sameParentTaskCode = new LinkedHashSet<>();
                for (Task task : tasks) {
                    //找到所有任务拥有相同父级的集合任务
                    for (String taskKey : parentMap.get(task.getTaskDefinitionKey())) {
                        for (String key : parentMap.keySet()) {
                            if (parentMap.get(key).contains(taskKey)) {
                                sameParentTaskCode.add(key);
                                break;
                            }
                        }
                    }
                }
                //说明是并行，但是做完的任务
                for (String sameParentTask : sameParentTaskCode) {
                    if (!taskKeyList.contains(sameParentTask)) {
                        List<SequenceFlow> sequenceFlows = flowNodeMap.get(sameParentTask).getOutgoingFlows();
                        if (sequenceFlows != null && !sequenceFlows.isEmpty()) {
                            for (SequenceFlow sequenceFlow : sequenceFlows) {
                                if (querySequenceFlowCondition(sequenceFlow, historicVariableInstanceMap)) {
                                    executedActivityIdList.add(sequenceFlow.getTargetRef());
                                }
                            }
                        }
                    }
                }

                for (String taskKey : sameParentTaskCode) {
                    for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
                        String activityId = historicActivityInstance.getActivityId();
                        if (historicActivityInstance.getTaskId() == null || !activityId.equals(taskKey)) {
                            executedActivityIdList.add(activityId);
                        } else {
                            executedActivityIdList.add(activityId);
                            break;
                        }
                    }

                }
            }
        }
        //获取流程走过的线
        List<String> executedActivityIdListResult = new ArrayList<>(executedActivityIdList);
        List<String> flowIds = getHighLightedFlows(bpmnModel, historicActivityInstanceList,
                executedActivityIdListResult, taskIdList);

        //输出图像，并设置高亮
        outputImg(response, bpmnModel, flowIds, executedActivityIdListResult);
    }

    private Map<String, FlowNode> getFlowNodeMap(List<HistoricActivityInstance> historicActivityInstanceList, String processDefinitionId) {
        org.activiti.bpmn.model.Process process = repositoryService
                .getBpmnModel(processDefinitionId)
                .getMainProcess();

        Map<String, FlowNode> flowNodeMap = new HashMap<>(historicActivityInstanceList.size());

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
            String activityId = historicActivityInstance.getActivityId();
            if (flowNodeMap.get(activityId) == null) {
                FlowNode sourceNode = (FlowNode) process.getFlowElement(activityId);
                flowNodeMap.put(activityId, sourceNode);
            }
        }
        return flowNodeMap;
    }

    /**
     * 撤回任务
     *
     * @param currentTaskId currentTaskId
     * @param targetTaskId  targetTaskId 目标任务，如果为空，默认返回上级，如果找到上级有2个，那目标任务必须得传
     */
    @Transactional(rollbackFor = Exception.class)
    public void backTask(String currentTaskId, String targetTaskId) {
        //准备数据
        TaskService taskService = processEngine.getTaskService();
        // 当前任务
        Task currentTask = taskService.createTaskQuery()
                .taskId(currentTaskId)
                .singleResult();

        String processInstanceId = currentTask.getProcessInstanceId();

        // 获取流程定义
        //任务历史数据
        List<HistoricTaskInstance> historicTaskInstances = historyService
                .createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderBy(HistoricTaskInstanceQueryProperty.HISTORIC_TASK_INSTANCE_ID)
                .desc()
                .list();
        Map<String, HistoricTaskInstance> historicTaskInstanceMap = historicTaskInstances.stream()
                .collect(Collectors.toMap(HistoricTaskInstance::getId, Function.identity()));

        //所有节点操作数据
        HistoricActivityInstanceQuery historyInstanceQuery = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId);
        List<HistoricActivityInstance> historicActivityInstanceList = historyInstanceQuery
                .orderBy(HistoricActivityInstanceQueryProperty.HISTORIC_ACTIVITY_INSTANCE_ID)
                .asc()
                .list();
        Map<String, List<HistoricActivityInstance>> historicActivityInstanceMap = historicActivityInstanceList.stream()
                .collect(Collectors.groupingBy(HistoricActivityInstance::getActivityId));
        Map<String, FlowNode> flowNodeMap = getFlowNodeMap(historicActivityInstanceList, currentTask.getProcessDefinitionId());
        //排除当前任务外的所有正在进行的任务
        List<Task> taskList = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list()
                .stream()
                .filter(task -> !task.getId().equals(currentTask.getId()))
                .collect(Collectors.toList());
        handleBackTask(currentTask, currentTask.getTaskDefinitionKey()
                ,targetTaskId, historicTaskInstanceMap
                ,historicActivityInstanceMap, flowNodeMap
                ,taskList, historicActivityInstanceList);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleBackTask(Task currentTask, String taskDefinitionKey,
                               String targetTaskId, Map<String, HistoricTaskInstance> historicTaskInstanceMap,
                               Map<String, List<HistoricActivityInstance>> historicActivityInstanceMap,
                               Map<String, FlowNode> flowNodeMap, List<Task> taskList,
                               List<HistoricActivityInstance> historicActivityInstanceList) {
        //判断是否并行
        if (taskList == null || taskList.isEmpty()) {
            //串行
            handleSerial(currentTask, taskDefinitionKey,
                    targetTaskId, historicTaskInstanceMap,
                    historicActivityInstanceMap, flowNodeMap,
                    taskList, historicActivityInstanceList);
        } else {
            //并行
            handleParallel(currentTask, taskDefinitionKey,
                    targetTaskId, historicTaskInstanceMap,
                    historicActivityInstanceMap, flowNodeMap,
                    taskList, historicActivityInstanceList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleParallel(Task currentTask, String taskDefinitionKey,
                               String targetTaskId, Map<String, HistoricTaskInstance> historicTaskInstanceMap,
                               Map<String, List<HistoricActivityInstance>> historicActivityInstanceMap,
                               Map<String, FlowNode> flowNodeMap, List<Task> taskList,
                               List<HistoricActivityInstance> historicActivityInstanceList) {
        List<SequenceFlow> sequenceFlows = flowNodeMap.get(taskDefinitionKey).getIncomingFlows();
        if (sequenceFlows.size() == 1) {
            //当前节点的上级节点只有一条
            SequenceFlow sequenceFlow = sequenceFlows.get(0);
            //查询历史节点
            HistoricActivityInstance historicActivityInstance = historicActivityInstanceList.stream()
                    .filter(query -> query.getActivityId().equals(sequenceFlow.getSourceRef()))
                    .collect(Collectors.toList())
                    .get(0);

            //判断来源类型
            if (historicActivityInstance.getActivityType().equals(PARALLEL_GATEWAY)) {
                //网关
                //查找网关的父任务
                Set<String> parentFlowNodes = queryParentFlowNode(historicActivityInstance.getActivityId(), flowNodeMap);
                if (!parentFlowNodes.isEmpty()) {
                    if (parentFlowNodes.size() == 1) {
                        //如果只有一个父节点
                        String activityId = new ArrayList<>(parentFlowNodes).get(0);
                        if (historicActivityInstanceMap.get(activityId).get(0).getActivityType().equals(USER_TASK)) {
                            //用户任务
                            deleteTaskMultiple(flowNodeMap, null,
                                    null, activityId, currentTask, taskList,
                                    historicActivityInstance.getActivityId());
                        } else {
                            //递归去查找父任务的前一个
                            handleBackTask(currentTask, historicActivityInstanceMap.get(activityId).get(0).getActivityId(),
                                    targetTaskId, historicTaskInstanceMap,
                                    historicActivityInstanceMap, flowNodeMap,
                                    taskList, historicActivityInstanceList);
                        }
                    } else {
                        //当前节点的上级节点有多条 这里需要指定要回退的taskId
                        deleteTaskMultiple(flowNodeMap, historicTaskInstanceMap,
                                targetTaskId, null, currentTask,
                                taskList, historicActivityInstance.getActivityId());
                    }
                } else {
                    //没有父级任务，图有问题
                    throw new IllegalArgumentException("bpmn doc error");
                }

            } else if (historicActivityInstance.getActivityType().equals(USER_TASK)) {
                //用户任务
                deleteTaskMultiple(flowNodeMap, null,
                        null, historicActivityInstance.getActivityId(),
                        currentTask, taskList, historicActivityInstance.getActivityId());
            } else {
                //todo 还没想好这种场景
                throw new IllegalArgumentException(BPMN_NOT_SUPPORT);
            }
        } else {
            //当前节点的上级节点有多条 这里需要指定要回退的taskId
            deleteTaskMultiple(flowNodeMap, historicTaskInstanceMap,
                    targetTaskId, null,
                    currentTask, taskList, null);

        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleSerial(Task currentTask, String taskDefinitionKey,
                             String targetTaskId, Map<String, HistoricTaskInstance> historicTaskInstanceMap,
                             Map<String, List<HistoricActivityInstance>> historicActivityInstanceMap,
                             Map<String, FlowNode> flowNodeMap, List<Task> taskList,
                             List<HistoricActivityInstance> historicActivityInstanceList) {

        FlowNode currentNode = flowNodeMap.get(taskDefinitionKey);
        List<SequenceFlow> sequenceFlows = currentNode.getIncomingFlows();
        if (sequenceFlows.size() == 1) {
            SequenceFlow sequenceFlow = sequenceFlows.get(0);
            HistoricActivityInstance historicActivityInstance = historicActivityInstanceMap.get(sequenceFlow.getSourceRef()).get(0);
            //网关
            if (historicActivityInstance.getActivityType().equals(PARALLEL_GATEWAY)
                    || historicActivityInstance.getActivityType().equals(EXCLUSIVE_GATEWAY)) {
                //查找网关的父任务
                Set<String> parentFlowNodes = queryParentFlowNode(historicActivityInstance.getActivityId(), flowNodeMap);
                if (!parentFlowNodes.isEmpty()) {
                    handleBackTaskSingle(parentFlowNodes, currentTask, targetTaskId,
                            historicTaskInstanceMap, historicActivityInstanceMap, flowNodeMap,
                            taskList, historicActivityInstanceList);
                } else {
                    //当前节点的上级节点有多条 这里需要指定要回退的taskId
                    deleteTaskMultiple(flowNodeMap, historicTaskInstanceMap, targetTaskId,
                            null, currentTask, taskList, null);
                }
            } else if (historicActivityInstance.getActivityType().equals(USER_TASK)) {
                deleteTaskSingle(flowNodeMap, historicActivityInstance.getActivityId(), currentTask.getId());
            } else {
                //todo 还没想好这种场景
                //throw new CommonValidateException(BPMN_NOT_SUPPORT);
            }
        } else {
            Map<String, HistoricVariableInstance> historicVariableInstanceMap = getHistoricVariableInstanceMap(currentTask.getProcessInstanceId());
            //串行的也有多条连线，可能是通过排他网关过来的
            Set<HistoricActivityInstance> historicActivityInstances = new HashSet<>();
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                //这边他的parent可能是没做过的，要找做过的
                if (historicActivityInstanceMap.get(sequenceFlow.getSourceRef()) != null
                        && querySequenceFlowCondition(sequenceFlow, historicVariableInstanceMap)) {
                    historicActivityInstances.addAll(historicActivityInstanceMap.get(sequenceFlow.getSourceRef()));
                }
            }
            //走过的只有一个
            if (historicActivityInstances.size() == 1) {
                List<HistoricActivityInstance> historicActivityInstancesList = new ArrayList<>(historicActivityInstances);
                if (historicActivityInstancesList.get(0).getActivityType().equals(USER_TASK)) {
                    deleteTaskSingle(flowNodeMap, historicActivityInstancesList.get(0).getActivityId(), currentTask.getId());
                } else if (historicActivityInstancesList.get(0).getActivityType().equals(EXCLUSIVE_GATEWAY)) {
                    //排他网关
                    Set<String> parentFlowNodes = queryParentFlowNode(historicActivityInstancesList.get(0).getActivityId(), flowNodeMap);
                    handleBackTaskSingle(parentFlowNodes, currentTask, targetTaskId,
                            historicTaskInstanceMap, historicActivityInstanceMap, flowNodeMap,
                            taskList, historicActivityInstanceList);
                } else {
                    //todo 还没想好这种场景
                    //throw new CommonValidateException(BPMN_NOT_SUPPORT);
                }
            } else {
                //当前节点的上级节点有多条 这里需要指定要回退的taskId
                deleteTaskMultiple(flowNodeMap, historicTaskInstanceMap, targetTaskId,
                        null, currentTask, taskList, null);
            }
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public void handleBackTaskSingle(Set<String> parentFlowNodes, Task currentTask, String targetTaskId, Map<String, HistoricTaskInstance> historicTaskInstanceMap, Map<String, List<HistoricActivityInstance>> historicActivityInstanceMap, Map<String, FlowNode> flowNodeMap, List<Task> taskList, List<HistoricActivityInstance> historicActivityInstanceList) {
        if (parentFlowNodes.size() == 1) {
            List<String> parentFlowNodeList = new ArrayList<>(parentFlowNodes);
            if (historicActivityInstanceMap.get(parentFlowNodeList.get(0)).get(0).getActivityType().equals(USER_TASK)) {
                deleteTaskSingle(flowNodeMap, parentFlowNodeList.get(0), currentTask.getId());
            } else {
                //递归去查找父任务的前一个
                handleBackTask(currentTask, historicActivityInstanceMap.get(parentFlowNodeList.get(0)).get(0).getActivityId(),
                        targetTaskId, historicTaskInstanceMap, historicActivityInstanceMap, flowNodeMap,
                        taskList, historicActivityInstanceList);
            }
        } else {
            //当前节点的上级节点有多条 这里需要指定要回退的taskId
            deleteTaskMultiple(flowNodeMap, historicTaskInstanceMap, targetTaskId,
                    null, currentTask, taskList, null);
        }
    }

    private void validatorTargetTask(Map<String, HistoricTaskInstance> historicTaskInstanceMap, String targetTaskId) {
        if (StringUtils.isEmpty(targetTaskId) || StringUtils.isBlank(targetTaskId)) {
            throw new IllegalArgumentException("target task id cannot be null");
        }
        if (historicTaskInstanceMap == null || historicTaskInstanceMap.isEmpty()) {
            throw new IllegalArgumentException("historic task instance cannot be null");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskMultiple(Map<String, FlowNode> flowNodeMap, Map<String, HistoricTaskInstance> historicTaskInstanceMap,
                                   String targetTaskId, String targetTaskDefinitionKey,
                                   Task currentTask, List<Task> taskList, String targetParentTaskDefinitionKey) {
        if (StringUtils.isEmpty(targetTaskDefinitionKey)
                || StringUtils.isBlank(targetTaskDefinitionKey)) {
            validatorTargetTask(historicTaskInstanceMap, targetTaskId);
            targetTaskDefinitionKey = historicTaskInstanceMap.get(targetTaskId).getTaskDefinitionKey();
        }
        FlowNode targetNode = flowNodeMap.get(targetTaskDefinitionKey);
        ManagementService managementService = processEngine.getManagementService();
        //删除当前任务
        managementService.executeCommand(new DeleteTaskCmd(currentTask.getId()));
        // 删除当前运行的其他相同父任务的子任务
        Set<Task> sameParentTasks = getSameParentTasks(flowNodeMap, taskList, targetParentTaskDefinitionKey);
        for (Task task : sameParentTasks) {
            managementService.executeCommand(new DeleteTaskCmd(task.getId()));
        }
        // 流程执行到来源节点
        managementService.executeCommand(new SetFlowNodeAndGoCmd(targetNode, currentTask.getExecutionId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTaskSingle(Map<String, FlowNode> flowNodeMap, String targetTaskActivitiId, String currentTaskId) {
        ManagementService managementService = processEngine.getManagementService();
        FlowNode targetNode = flowNodeMap.get(targetTaskActivitiId);
        // 删除当前运行任务
        String executionEntityId = managementService.executeCommand(new DeleteTaskCmd(currentTaskId));
        // 流程执行到来源节点
        managementService.executeCommand(new SetFlowNodeAndGoCmd(targetNode, executionEntityId));
    }

    private Set<String> queryParentFlowNode(String activityId, Map<String, FlowNode> flowNodeMap) {
        Set<String> flowNodeList = new HashSet<>();
        for (String key : flowNodeMap.keySet()) {
            if (!key.equals(activityId)) {
                FlowNode flowNode = flowNodeMap.get(key);
                List<SequenceFlow> sequenceFlows = flowNode.getOutgoingFlows();
                for (SequenceFlow sequenceFlow : sequenceFlows) {
                    if (sequenceFlow.getTargetRef().equals(activityId)) {
                        flowNodeList.add(key);
                        break;
                    }
                }
            }
        }
        return flowNodeList;
    }

    private Set<Task> getSameParentTasks(Map<String, FlowNode> flowNodeMap, List<Task> taskList, String taskDefinitionKey) {
        if (taskDefinitionKey == null) {
            return new HashSet<>(taskList);
        }
        Set<Task> tasks = new HashSet<>();
        for (Task task : taskList) {
            List<SequenceFlow> sequenceFlows = flowNodeMap.get(task.getTaskDefinitionKey()).getIncomingFlows();
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                if (sequenceFlow.getSourceRef().equals(taskDefinitionKey)) {
                    tasks.add(task);
                    break;
                }
            }
        }
        return tasks;
    }

    @Transactional(rollbackFor = Exception.class)
    public void importBpmnFile(MultipartFile file, String type, String typeName) {
        try {
            InputStream fileInputStream = file.getInputStream();
            //创建转换对象
            BpmnXMLConverter bpmnXMLConverter = new BpmnXMLConverter();
            //读取xml文件
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(fileInputStream);

            //将xml文件转换成BpmnModel
            BpmnModel bpmnModel = bpmnXMLConverter.convertToBpmnModel(xmlStreamReader);
            bpmnModel.getMainProcess().setId(type);
            bpmnModel.getMainProcess().setName(typeName);
            Deployment deployment = repositoryService.createDeployment()
                    .addBpmnModel(typeName + ".bpmn", bpmnModel)
                    .key(String.valueOf(new IdWorker(1,1).nextId()))
                    .deploy();

            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .singleResult();
            BpmnModel model = repositoryService.getBpmnModel(processDefinition.getId());
            if (model != null) {
                Collection<FlowElement> flowElementCollection = model.getMainProcess()
                        .getFlowElements();

                for (FlowElement e : flowElementCollection) {
                    log.info("flowElement id = {},  name = {} ,class = {}" ,e.getId(), e.getName(), e.getClass().toString());
                }
            }
            activitiMapper.updateActReProcdef(processDefinition.getId());
        } catch (Exception e) {
            log.error("导入流程定义失败:{}", e.getMessage(), e);
        }
    }


}
