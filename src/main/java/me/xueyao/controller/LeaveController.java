package me.xueyao.controller;

import me.xueyao.base.R;
import me.xueyao.entity.dto.LeaveDTO;
import me.xueyao.entity.dto.LeaveUpdateDTO;
import me.xueyao.entity.vo.LeaveVo;
import me.xueyao.service.ILeaveService;
import me.xueyao.service.IProcessService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * @author simonxue
 */
@RestController
@RequestMapping("/leave")
public class LeaveController extends BaseController{

    @Autowired
    private ILeaveService iLeaveService;
    @Autowired
    private IProcessService processService;


    /**
     * @Todo 分页无效果
     * 查询请假业务列表
     */
    @PostMapping("/list")
    public R list(LeaveDTO leaveDTO, String loginName) {
        leaveDTO.setType("leave");
        return iLeaveService.selectLeaveList(leaveDTO, loginName);
    }


    /**
     * 新增保存请假业务
     */
    @PostMapping("/add")
    public R addSave(LeaveDTO leaveDTO, String loginName) {
        /*
        Long userId = ShiroUtils.getUserId();
        if (SysUser.isAdmin(userId)) {
            return error("提交申请失败：不允许管理员提交申请！");
        }
        */
        leaveDTO.setType("leave");
        return iLeaveService.insertLeave(leaveDTO, loginName);
    }

    /**
     * 修改请假业务
     */
    @GetMapping("/edit/{id}")
    public R updateById(@PathVariable("id") Long id, LeaveUpdateDTO leaveUpdateDTO) {
        return iLeaveService.updateById(id, leaveUpdateDTO);
    }


    /**
     * 删除请假业务
     */
    @PostMapping("/remove")
    public R remove(String ids) {
        return iLeaveService.deleteLeaveByIds(ids);
    }

    /**
     * 提交申请
     *
     * @param id          任务的id(biz_leave的id)
     * @param applyUserId 由前端传递值，后期改成后端直接获取登录信息
     */
    @PostMapping("/submitApply")
    public R submitApply(Long id, String applyUserId) {
        return iLeaveService.submitApply(id, applyUserId);
    }

    /**
     * 我的待办列表
     * @Todo 分页无效
     * @param leaveDTO
     * @param loginName 由前端传递值，后期改成后端直接获取登录信息
     * @return
     */
    @PostMapping("/taskList")
    public R taskList(LeaveDTO leaveDTO, String loginName) {
        leaveDTO.setType("leave");
        return iLeaveService.findTodoTasks(leaveDTO, loginName);
    }


    /**
     * 完成任务
     *
     * @return
     */
    @PostMapping(value = "/complete/{taskId}")
    public R complete(@PathVariable("taskId") String taskId,
                      @RequestParam(value = "saveEntity", required = false) String saveEntity,
                      @RequestBody LeaveVo leave, HttpServletRequest request, String loginName) {
        boolean saveEntityBoolean = BooleanUtils.toBoolean(saveEntity);
        processService.complete(taskId, leave.getInstanceId(), leave.getTitle(), leave.getReason(),
                "leave", Collections.EMPTY_MAP, request, loginName);
        if (saveEntityBoolean) {
            iLeaveService.updateLeave(leave);
        }
        return R.ofSuccess("任务已完成");
    }


    /**
     * 我的已办列表
     *
     * @param leaveDTO
     * @param loginName 此处先由前端传递参数，以后改成后端获取登录用户名
     * @return
     */
    @PostMapping("/taskDoneList")
    public R taskDoneList(LeaveDTO leaveDTO, String loginName) {
        leaveDTO.setType("leave");
        return iLeaveService.findDoneTasks(leaveDTO, loginName);
    }

}
