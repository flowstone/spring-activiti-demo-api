package me.xueyao.controller;

import me.xueyao.base.R;
import me.xueyao.entity.dto.LeaveDTO;
import me.xueyao.entity.dto.LeaveUpdateDTO;
import me.xueyao.entity.vo.LeaveVo;
import me.xueyao.service.ILeaveService;
import me.xueyao.service.IProcessService;
import me.xueyao.util.ShiroUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * 请假流程操作
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
     * 查询请假业务列表
     */
    @PostMapping("/list")
    public R list(LeaveDTO leaveDTO) {
        leaveDTO.setType("leave");
        return iLeaveService.selectLeaveList(leaveDTO);
    }


    /**
     * 新增保存请假业务
     */
    @PostMapping("/add")
    public R addSave(LeaveDTO leaveDTO) {
        /*
        Long userId = ShiroUtils.getUserId();
        if (SysUser.isAdmin(userId)) {
            return error("提交申请失败：不允许管理员提交申请！");
        }
        */
        leaveDTO.setType("leave");
        return iLeaveService.insertLeave(leaveDTO);
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
     */
    @PostMapping("/submitApply")
    public R submitApply(Long id) {
        return iLeaveService.submitApply(id);
    }

    /**
     * 我的待办列表
     * @param leaveDTO
     * @return
     */
    @PostMapping("/taskList")
    public R taskList(LeaveDTO leaveDTO) {
        leaveDTO.setType("leave");
        System.out.println(ShiroUtils.getLoginName());
        leaveDTO.setCreateBy(ShiroUtils.getLoginName());
        return iLeaveService.findTodoTasks(leaveDTO);
    }


    /**
     * 完成任务
     *
     * @return
     */
    @PostMapping(value = "/complete")
    public R complete(@RequestParam("taskId") String taskId,
                      @RequestParam(value = "saveEntity", required = false) String saveEntity,
                      LeaveVo leave, HttpServletRequest request) {
        boolean saveEntityBoolean = BooleanUtils.toBoolean(saveEntity);
        processService.complete(taskId, leave.getInstanceId(), leave.getTitle(), leave.getReason(),
                "leave", new HashMap<>(16), request);
        if (saveEntityBoolean) {
            iLeaveService.updateLeave(leave);
        }
        return R.ofSuccess("任务已完成");
    }


    /**
     * 我的已办列表
     *
     * @param leaveDTO
     * @return
     */
    @PostMapping("/taskDoneList")
    public R taskDoneList(LeaveDTO leaveDTO) {
        leaveDTO.setType("leave");
        return iLeaveService.findDoneTasks(leaveDTO);
    }

}
