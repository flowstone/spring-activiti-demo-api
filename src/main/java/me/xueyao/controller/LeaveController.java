package me.xueyao.controller;

import me.xueyao.base.R;
import me.xueyao.entity.BeanUtils;
import me.xueyao.entity.dto.LeaveAddDTO;
import me.xueyao.entity.dto.LeaveDTO;
import me.xueyao.entity.dto.LeaveUpdateDTO;
import me.xueyao.service.ILeaveService;
import me.xueyao.service.IProcessService;
import me.xueyao.util.ShiroUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;

/**
 * 请假流程操作
 * @author simonxue
 */
@RestController
@RequestMapping("/leave")
public class LeaveController {

    @Autowired
    private ILeaveService iLeaveService;
    @Autowired
    private IProcessService processService;



    /**
     * 查询请假业务列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @PostMapping("/list")
    public R list(@RequestParam(value = "pageNum") Integer pageNum,
                  @RequestParam(value = "pageSize") Integer pageSize) {
        LeaveDTO leaveDTO = new LeaveDTO();
        leaveDTO.setPageNum(pageNum);
        leaveDTO.setPageSize(pageSize);
        leaveDTO.setType("leave");
        return iLeaveService.selectLeaveList(leaveDTO);
    }


    /**
     * 新增保存请假业务
     * @param leaveAddDTO
     */
    @PostMapping("/add")
    public R addSave(@RequestBody @Valid LeaveAddDTO leaveAddDTO) {
        /*
        Long userId = ShiroUtils.getUserId();
        if (SysUser.isAdmin(userId)) {
            return error("提交申请失败：不允许管理员提交申请！");
        }
        */
        LeaveDTO leaveDTO = new LeaveDTO();
        BeanUtils.copyBeanProp(leaveDTO, leaveAddDTO);
        leaveDTO.setType("leave");
        return iLeaveService.insertLeave(leaveDTO);
    }

    /**
     * Todo 暂时没有用到
     * 修改请假业务
     * @param id
     * @param leaveUpdateDTO
     */
    @GetMapping("/edit/{id}")
    public R updateById(@PathVariable("id") Long id, LeaveUpdateDTO leaveUpdateDTO) {
        return iLeaveService.updateById(id, leaveUpdateDTO);
    }


    /**
     * Todo 暂时没有用到
     * 删除请假业务
     * @param ids
     */
    @PostMapping("/remove")
    public R remove(String ids) {
        return iLeaveService.deleteLeaveByIds(ids);
    }


    /**
     * 提交申请
     * @param id  任务的id(biz_leave的id)
     * @return
     */
    @PostMapping("/submitApply")
    public R submitApply(@RequestParam(value = "id") Long id) {
        return iLeaveService.submitApply(id);
    }


    /**
     * 我的待办列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @PostMapping("/taskList")
    public R taskList(@RequestParam("pageNum")Integer pageNum,
                      @RequestParam("pageSize")Integer pageSize) {
        LeaveDTO leaveDTO = new LeaveDTO();
        leaveDTO.setPageNum(pageNum);
        leaveDTO.setPageSize(pageSize);
        leaveDTO.setType("leave");
        leaveDTO.setCreateBy(ShiroUtils.getLoginName());
        return iLeaveService.findTodoTasks(leaveDTO);
    }


    /**
     * 完成任务
     * @param taskId
     * @param instanceId
     * @param title
     * @param reason
     * @param request
     * request 传递的参数有三种情况
     * ------1、部门领导审批----------
     *   "p_B_deptLeaderApproved: true
     *   "p_COM_comment": "备注内容"
     * ------------------------------
     *
     * ------2、人事审批--------------
     *    "p_B_hrApproved": true
     *    "p_COM_comment":"备注内容"
     * ------------------------------
     *
     * ------3、销假--------------
     *    "p_DT_realityStartTime": "2021-02-04 02:35" //真实开始时间
     *    "p_DT_realityEndTime":"2021-02-13 11:55" //真实结束时间
     * ------------------------------
     * @return
     */
    @PostMapping(value = "/complete")
    public R complete(@RequestParam("taskId") String taskId,
                      @RequestParam("instanceId") String instanceId,
                      @RequestParam("title") String title,
                      @RequestParam("reason") String reason,
                      HttpServletRequest request) {
        processService.complete(taskId, instanceId, title, reason,
                "leave", new HashMap<>(16), request);

        return R.ofSuccess("任务已完成");
    }


    /**
     * 我的已办列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @PostMapping("/taskDoneList")
    public R taskDoneList(@RequestParam("pageNum") Integer pageNum,
                          @RequestParam("pageSize") Integer pageSize) {
        LeaveDTO leaveDTO = new LeaveDTO();
        leaveDTO.setPageNum(pageNum);
        leaveDTO.setPageSize(pageSize);
        leaveDTO.setType("leave");
        return iLeaveService.findDoneTasks(leaveDTO);
    }

}
