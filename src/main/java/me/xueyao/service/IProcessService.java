package me.xueyao.service;

import me.xueyao.base.R;
import me.xueyao.entity.HistoricActivity;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author simonxue
 */
public interface IProcessService {

    /**
     * 查询审批历史列表
     *
     * @param processInstanceId
     * @param historicActivity
     * @return
     */
    R selectHistoryList(String processInstanceId, HistoricActivity historicActivity);

    /**
     * 提交申请
     *
     * @param applyUserId 申请人
     * @param businessKey 业务表 id
     * @param key         流程定义 key
     * @param variables   流程变量
     * @return
     */
    ProcessInstance submitApply(String applyUserId, String businessKey,
                                String itemName, String itemContent,
                                String key, Map<String, Object> variables);

    /**
     * 查询未完成的任务
     *
     * @param userId
     * @param key
     * @return
     */
    List<Task> findTodoTasks(String userId, String key);

    /**
     * 查询已完成的任务
     *
     * @param userId
     * @param key
     * @return
     */
    List<HistoricTaskInstance> findDoneTasks(String userId, String key);

    /**
     * 完成任务
     *
     * @param taskId
     * @param instanceId
     * @param itemName
     * @param itemContent
     * @param module
     * @param variables
     * @param request
     * @param loginName
     * @return
     */
    R complete(String taskId, String instanceId, String itemName, String itemContent, String module,
               Map<String, Object> variables, HttpServletRequest request);

    /**
     * 委托任务
     *
     * @param taskId
     * @param fromUser
     * @param delegateToUser
     * @return
     */
    R delegate(String taskId, String fromUser, String delegateToUser);

    /**
     * 撤销
     *
     * @param instanceId
     * @param deleteReason
     * @return
     */
    R cancelApply(String instanceId, String deleteReason);

    /**
     * 挂起或激活申请
     *
     * @param instanceId
     * @param suspendState
     * @return
     */
    R suspendOrActiveApply(String instanceId, String suspendState);

    /**
     * @param instanceId
     * @return
     */
    String findBusinessKeyByInstanceId(String instanceId);
}
