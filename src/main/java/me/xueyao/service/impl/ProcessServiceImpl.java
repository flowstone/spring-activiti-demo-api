package me.xueyao.service.impl;

import lombok.extern.slf4j.Slf4j;
import me.xueyao.base.PageResult;
import me.xueyao.base.R;
import me.xueyao.entity.HistoricActivity;
import me.xueyao.entity.SysUser;
import me.xueyao.entity.TodoItem;
import me.xueyao.mapper.SysUserMapper;
import me.xueyao.service.IProcessService;
import me.xueyao.service.ITodoItemService;
import me.xueyao.util.DateUtils;
import me.xueyao.util.StringUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author simonxue
 */
@Slf4j
@Service
@Transactional(rollbackFor = RuntimeException.class)
public class ProcessServiceImpl implements IProcessService {

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private ITodoItemService bizTodoItemService;

    @Override
    public ProcessInstance submitApply(String applyUserId, String businessKey, String itemName, String itemContent,
                                       String module, Map<String, Object> variables) {
        // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
        identityService.setAuthenticatedUserId(applyUserId);
        // 启动流程时设置业务 key
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(module, businessKey, variables);
        // 下一节点处理人待办事项
        bizTodoItemService.insertTodoItem(instance.getProcessInstanceId(), itemName, itemContent, module);
        return instance;
    }

    @Override
    public List<Task> findTodoTasks(String userId, String key) {
        List<Task> tasks = new ArrayList<>();
        // 根据当前人的ID查询
        List<Task> todoList = taskService
                .createTaskQuery()
                .processDefinitionKey(key)
                .taskAssignee(userId)
                .list();
        // 根据当前人未签收的任务
        List<Task> unsignedTasks = taskService
                .createTaskQuery()
                .processDefinitionKey(key)
                .taskCandidateUser(userId)
                .list();
        // 合并
        tasks.addAll(todoList);
        tasks.addAll(unsignedTasks);
        return tasks;
    }

    @Override
    public List<HistoricTaskInstance> findDoneTasks(String userId, String key) {
        List<HistoricTaskInstance> list = historyService
                .createHistoricTaskInstanceQuery()
                .processDefinitionKey(key)
                .taskAssignee(userId)
                .finished()
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list();
        return list;
    }

    @Override
    public R complete(String taskId, String instanceId, String itemName, String itemContent, String module,
                      Map<String, Object> variables, HttpServletRequest request, String loginName) {

        Enumeration<String> parameterNames = request.getParameterNames();
        // 批注
        String comment = null;
        boolean agree = true;
        try {
            while (parameterNames.hasMoreElements()) {
                String parameterName = parameterNames.nextElement();
                if (parameterName.startsWith("p_")) {
                    // 参数结构：p_B_name，p为参数的前缀，B为类型，name为属性名称
                    String[] parameter = parameterName.split("_");
                    if (parameter.length == 3) {
                        String paramValue = request.getParameter(parameterName);
                        Object value = paramValue;
                        if (parameter[1].equals("B")) {
                            value = BooleanUtils.toBoolean(paramValue);
                            agree = (boolean) value;
                        } else if (parameter[1].equals("DT")) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            value = sdf.parse(paramValue);
                        } else if (parameter[1].equals("COM")) {
                            comment = paramValue;
                        }
                        variables.put(parameter[2], value);
                    } else {
                        throw new RuntimeException("invalid parameter for activiti variable: " + parameterName);
                    }
                }
            }
            if (StringUtils.isNotEmpty(comment)) {
                identityService.setAuthenticatedUserId(loginName);
                comment = agree ? "【同意】" + comment : "【拒绝】" + comment;
                taskService.addComment(taskId, instanceId, comment);
            }
            // 被委派人处理完成任务
            // p.s. 被委托的流程需要先 resolved 这个任务再提交。
            // 所以在 complete 之前需要先 resolved
            // resolveTask() 要在 claim() 之前，不然 act_hi_taskinst 表的 assignee 字段会为 null
            taskService.resolveTask(taskId, variables);
            // 只有签收任务，act_hi_taskinst 表的 assignee 字段才不为 null
            //taskService.claim(taskId, ShiroUtils.getLoginName());
            taskService.claim(taskId, loginName);
            taskService.complete(taskId, variables);

            // 更新待办事项状态
            TodoItem query = new TodoItem();
            query.setTaskId(taskId);
            // 考虑到候选用户组，会有多个 todoitem 办理同个 task
            List<TodoItem> updateList = CollectionUtils.isEmpty(bizTodoItemService.selectBizTodoItemList(query)) ? null : bizTodoItemService.selectBizTodoItemList(query);
            for (TodoItem update : updateList) {
                // 找到当前登录用户的 todoitem，置为已办
                if (update.getTodoUserId().equals(loginName)) {
                    update.setIsView("1");
                    update.setIsHandle("1");
                    update.setHandleUserId(loginName);
                    //update.setHandleUserName(ShiroUtils.getSysUser().getUserName());
                    update.setHandleUserName("大明");
                    update.setHandleTime(DateUtils.getNowDate());
                    bizTodoItemService.updateBizTodoItem(update);
                } else {
                    // 删除候选用户组其他 todoitem
                    bizTodoItemService.deleteBizTodoItemById(update.getId());
                }
            }

            // 下一节点处理人待办事项
            bizTodoItemService.insertTodoItem(instanceId, itemName, itemContent, module);
        } catch (Exception e) {
            log.error("error on complete task {}, variables={}", new Object[]{taskId, variables, e});
        }
        return R.ofSuccess("完成任务");
    }

    @Override
    public R selectHistoryList(String processInstanceId, HistoricActivity historicActivity) {
        //手动分页
        Integer pageNum = historicActivity.getPageNum();
        Integer pageSize = historicActivity.getPageSize();

        HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();
        if (StringUtils.isNotBlank(historicActivity.getAssignee())) {
            query.taskAssignee(historicActivity.getAssignee());
        }

        if (StringUtils.isNotBlank(historicActivity.getActivityName())) {
            query.activityName(historicActivity.getActivityName());
        }

        List<HistoricActivityInstance> historicActivityInstanceList = query.processInstanceId(processInstanceId)
                .activityType("userTask")
                .finished()
                .orderByHistoricActivityInstanceStartTime()
                .desc()
                .listPage(pageNum-1* pageSize, pageSize);

        List<HistoricActivity> activityList = historicActivityInstanceList.stream().map(instance -> {
            HistoricActivity activity = new HistoricActivity();
            BeanUtils.copyProperties(instance, activity);
            String taskId = instance.getTaskId();

            List<Comment> comment = taskService.getTaskComments(taskId, "comment");

            if (!CollectionUtils.isEmpty(comment)) {
                activity.setComment(comment.get(0).getFullMessage());
            }

            SysUser sysUser = userMapper.selectUserByLoginName(instance.getAssignee());
            if (sysUser != null) {
                activity.setAssigneeName(sysUser.getUserName());
            }
            return activity;
        }).collect(Collectors.toList());

        return R.ofSuccess("查询成功", new PageResult<>(pageNum, pageSize, activityList));
    }

    @Override
    public R delegate(String taskId, String fromUser, String delegateToUser) {
        taskService.delegateTask(taskId, delegateToUser);
        // 更新待办事项
        TodoItem updateItem = bizTodoItemService.selectBizTodoItemByCondition(taskId, fromUser);
        if (updateItem != null) {
            SysUser todoUser = userMapper.selectUserByLoginName(delegateToUser);
            updateItem.setTodoUserId(delegateToUser);
            updateItem.setTodoUserName(todoUser.getUserName());
            bizTodoItemService.updateBizTodoItem(updateItem);
        }
        return R.ofSuccess("");
    }

    @Override
    public R cancelApply(String instanceId, String deleteReason) {
        // 执行此方法后未审批的任务 act_ru_task 会被删除，流程历史 act_hi_taskinst 不会被删除，并且流程历史的状态为finished完成
        runtimeService.deleteProcessInstance(instanceId, deleteReason);
        return R.ofSuccess("取消申请成功");
    }

    @Override
    public R suspendOrActiveApply(String instanceId, String suspendState) {
        if ("1".equals(suspendState)) {
            // 当流程实例被挂起时，无法通过下一个节点对应的任务id来继续这个流程实例。
            // 通过挂起某一特定的流程实例，可以终止当前的流程实例，而不影响到该流程定义的其他流程实例。
            // 激活之后可以继续该流程实例，不会对后续任务造成影响。
            // 直观变化：act_ru_task 的 SUSPENSION_STATE_ 为 2
            runtimeService.suspendProcessInstanceById(instanceId);
        } else if ("2".equals(suspendState)) {
            runtimeService.activateProcessInstanceById(instanceId);
        }
        return R.ofSuccess("更新成功");
    }

    @Override
    public String findBusinessKeyByInstanceId(String instanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
        if (processInstance == null) {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(instanceId)
                    .singleResult();
            return historicProcessInstance.getBusinessKey();
        } else {
            return processInstance.getBusinessKey();
        }
    }

}
