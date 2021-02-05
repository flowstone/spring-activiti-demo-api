package me.xueyao.service.impl;

import com.github.pagehelper.Page;
import me.xueyao.base.PageResult;
import me.xueyao.base.R;
import me.xueyao.entity.SysUser;
import me.xueyao.entity.dto.LeaveDTO;
import me.xueyao.entity.dto.LeaveUpdateDTO;
import me.xueyao.entity.vo.LeaveVo;
import me.xueyao.mapper.LeaveMapper;
import me.xueyao.mapper.SysUserMapper;
import me.xueyao.service.ILeaveService;
import me.xueyao.service.IProcessService;
import me.xueyao.util.BeanCompareUtils;
import me.xueyao.util.Convert;
import me.xueyao.util.DateUtils;
import me.xueyao.util.StringUtils;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.persistence.entity.TaskEntityImpl;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 请假业务Service业务层处理
 *
 * @author Xianlu Tech
 * @date 2019-10-11
 */
@Service
@Transactional
public class LeaveServiceImpl implements ILeaveService {
    @Autowired
    private LeaveMapper leaveMapper;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private IProcessService processService;

    /**
     * 查询请假业务
     *
     * @param id 请假业务ID
     * @return 请假业务
     */
    @Override
    public LeaveVo selectLeaveById(Long id) {
        LeaveVo leave = leaveMapper.selectLeaveById(id);
        SysUser sysUser = userMapper.selectUserByLoginName(leave.getApplyUser());
        if (sysUser != null) {
            leave.setApplyUserName(sysUser.getUserName());
        }
        return leave;
    }

    /**
     * 查询请假业务列表
     *
     * @param leaveDTO  请假业务
     * @param LoginName
     * @return 请假业务
     */
    @Override
    public R selectLeaveList(LeaveDTO leaveDTO, String LoginName) {

        Page page = new Page(leaveDTO.getPageNum(), leaveDTO.getPageSize());

        Page<LeaveVo> leaveVoPage = leaveMapper.selectLeaveList(leaveDTO, page);
        leaveVoPage.getResult().forEach(leaveVo -> {
            SysUser sysUser = userMapper.selectUserByLoginName(leaveVo.getCreateBy());
            if (sysUser != null) {
                leaveVo.setCreateUserName(sysUser.getUserName());
            }
            sysUser = userMapper.selectUserByLoginName(leaveVo.getApplyUser());
            if (sysUser != null) {
                leaveVo.setApplyUserName(sysUser.getUserName());
            }

            // 当前环节
            if (StringUtils.isNotBlank(leaveVo.getInstanceId())) {
                List<Task> taskList = taskService.createTaskQuery()
                        .processInstanceId(leaveVo.getInstanceId())
                        .list();
                // 例如请假会签，会同时拥有多个任务
                if (!CollectionUtils.isEmpty(taskList)) {
                    TaskEntityImpl task = (TaskEntityImpl) taskList.get(0);
                    leaveVo.setTaskId(task.getId());
                    if (task.getSuspensionState() == 2) {
                        leaveVo.setTaskName("已挂起");
                        leaveVo.setSuspendState("2");
                    } else {
                        leaveVo.setTaskName(task.getName());
                        leaveVo.setSuspendState("1");
                    }
                } else {
                    // 已办结或者已撤销
                    leaveVo.setTaskName("已结束");
                }
            } else {
                leaveVo.setTaskName("未启动");
            }
        });

        return R.ofSuccess("查询成功", new PageResult<>(leaveVoPage));
    }

    /**
     * 新增请假业务
     *
     * @param leaveDTO  请假业务
     * @param loginName
     * @return 结果
     */
    @Override
    public R insertLeave(LeaveDTO leaveDTO, String loginName) {
        leaveDTO.setCreateBy(loginName);
        leaveDTO.setCreateTime(DateUtils.getNowDate());
        int count = leaveMapper.insertLeave(leaveDTO);
        return R.ofSuccess("添加成功", count);
    }


    /**
     * 删除请假业务对象
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public R deleteLeaveByIds(String ids) {
        int count = leaveMapper.deleteLeaveByIds(Convert.toStrArray(ids));
        return R.ofSuccess("删除成功", count);
    }

    /**
     * 删除请假业务信息
     *
     * @param id 请假业务ID
     * @return 结果
     */
    @Override
    public R deleteLeaveById(Long id) {
        int count = leaveMapper.deleteLeaveById(id);
        return R.ofSuccess("删除成功", count);
    }

    /**
     * 启动流程
     *
     * @param id
     * @param applyUserId
     * @return
     */
    @Override
    public R submitApply(Long id, String applyUserId) {
        LeaveVo leave = selectLeaveById(id);
        leave.setApplyUser(applyUserId);
        leave.setApplyTime(DateUtils.getNowDate());
        leave.setUpdateBy(applyUserId);
        leaveMapper.updateLeave(leave);
        // 实体类 ID，作为流程的业务 key
        String businessKey = leave.getId().toString();

        String key = "leave";
        Map<String, Object> variables = new HashMap<>();
        ProcessInstance processInstance = processService.submitApply(applyUserId, businessKey,
                leave.getTitle(), leave.getReason(), key, variables);

        String processInstanceId = processInstance.getId();
        // 建立双向关系
        leave.setInstanceId(processInstanceId);
        leaveMapper.updateLeave(leave);

        return R.ofSuccess("启动流程", processInstance);
    }

    /**
     * 查询待办任务
     */
    @Override
    public R findTodoTasks(LeaveDTO leaveDTO, String userId) {
        // 手动分页
        Integer pageNum = leaveDTO.getPageNum();
        Integer pageSize = leaveDTO.getPageSize();
        List<LeaveVo> result = new ArrayList<>();
        List<Task> tasks = processService.findTodoTasks(userId, leaveDTO.getType());

        for (Task task : tasks) {
            TaskEntityImpl taskImpl = (TaskEntityImpl) task;
            String processInstanceId = taskImpl.getProcessInstanceId();
            // 条件过滤 1
            if (StringUtils.isNotBlank(leaveDTO.getInstanceId()) && !leaveDTO.getInstanceId().equals(processInstanceId)) {
                continue;
            }
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            String businessKey = processInstance.getBusinessKey();
            LeaveVo taskLeave = leaveMapper.selectLeaveById(new Long(businessKey));
            // 条件过滤 2
            if (StringUtils.isNotBlank(leaveDTO.getType()) && !leaveDTO.getType().equals(taskLeave.getType())) {
                continue;
            }

            taskLeave.setTaskId(taskImpl.getId());
            if (2 == taskImpl.getSuspensionState()) {
                taskLeave.setTaskName("已挂起");
            } else {
                taskLeave.setTaskName(taskImpl.getName());
            }


            SysUser sysUser = userMapper.selectUserByLoginName(taskLeave.getApplyUser());
            taskLeave.setApplyUserName(sysUser.getUserName());
            result.add(taskLeave);

        }


        return R.ofSuccess("查询成功", new PageResult<>(pageNum, pageSize, result));
    }

    /**
     * 查询已办列表
     *
     * @param leaveDTO
     * @param userId
     * @return
     */
    @Override
    public R findDoneTasks(LeaveDTO leaveDTO, String userId) {
        // 手动分页
        Integer pageNum = leaveDTO.getPageNum();
        Integer pageSize = leaveDTO.getPageSize();

        List<LeaveVo> results = new ArrayList<>();
        List<HistoricTaskInstance> hisList = processService.findDoneTasks(userId, leaveDTO.getType());
        // 根据流程的业务ID查询实体并关联
        for (HistoricTaskInstance instance : hisList) {
            String processInstanceId = instance.getProcessInstanceId();
            // 条件过滤 1
            if (StringUtils.isNotBlank(leaveDTO.getInstanceId())
                    && !leaveDTO.getInstanceId().equals(processInstanceId)) {
                continue;
            }
            HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            String businessKey = processInstance.getBusinessKey();
            LeaveVo hisLeaveVo = leaveMapper.selectLeaveById(new Long(businessKey));

            // 条件过滤 2
            if (StringUtils.isNotBlank(leaveDTO.getType())
                    && !leaveDTO.getType().equals(hisLeaveVo.getType())) {
                continue;
            }
            hisLeaveVo.setTaskId(instance.getId());
            hisLeaveVo.setTaskName(instance.getName());
            hisLeaveVo.setDoneTime(instance.getEndTime());
            SysUser sysUser = userMapper.selectUserByLoginName(hisLeaveVo.getApplyUser());
            hisLeaveVo.setApplyUserName(sysUser.getUserName());
            results.add(hisLeaveVo);
        }

        return R.ofSuccess("查询成功", new PageResult<>(pageNum, pageSize, results));
    }

    @Override
    public R updateById(Long id, LeaveUpdateDTO leaveUpdateDTO) {
        LeaveVo leaveVo = selectLeaveById(id);
        leaveVo.setUpdateTime(DateUtils.getNowDate());
        BeanUtils.copyProperties(BeanCompareUtils.getEmptyPropertyNames(leaveUpdateDTO), leaveVo);
        int count = leaveMapper.updateLeave(leaveVo);
        return R.ofSuccess("修改成功", count);
    }

    @Override
    public R updateLeave(LeaveVo leaveVo) {
        int count = leaveMapper.updateLeave(leaveVo);
        return R.ofSuccess("更新成功", count);
    }


}
