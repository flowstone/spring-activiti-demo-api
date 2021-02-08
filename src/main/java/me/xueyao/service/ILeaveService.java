package me.xueyao.service;

import me.xueyao.base.R;
import me.xueyao.entity.dto.LeaveDTO;
import me.xueyao.entity.dto.LeaveUpdateDTO;
import me.xueyao.entity.vo.LeaveVo;

/**
 * 请假业务Service接口
 *
 * @author Xianlu Tech
 * @date 2019-10-11
 */
public interface ILeaveService {
    /**
     * 查询请假业务
     *
     * @param id 请假业务ID
     * @return 请假业务
     */
    LeaveVo selectLeaveById(Long id);

    /**
     * 查询请假业务列表
     *
     * @param leaveDTO  请假业务
     * @return 请假业务集合
     */
    R selectLeaveList(LeaveDTO leaveDTO);

    /**
     * 新增请假业务
     *
     * @param leaveDTO  请假业务
     * @return 结果
     */
    R insertLeave(LeaveDTO leaveDTO);

    /**
     * 修改请假业务
     *
     * @param LeaveVo 请假业务
     * @return 结果
     */
    R updateLeave(LeaveVo LeaveVo);

    /**
     * 批量删除请假业务
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    R deleteLeaveByIds(String ids);

    /**
     * 删除请假业务信息
     *
     * @param id 请假业务ID
     * @return 结果
     */
    R deleteLeaveById(Long id);

    /**
     * 启动流程
     *
     * @param id
     * @return
     */
    R submitApply(Long id);

    /**
     * 查询我的待办列表
     *
     * @param leaveDTO
     * @return
     */
    R findTodoTasks(LeaveDTO leaveDTO);

    /**
     * 查询已办列表
     *
     * @param leaveDTO
     * @return
     */
    R findDoneTasks(LeaveDTO leaveDTO);

    /**
     * 更新
     *
     * @param id
     * @param leaveUpdateDTO
     * @return
     */
    R updateById(Long id, LeaveUpdateDTO leaveUpdateDTO);
}
