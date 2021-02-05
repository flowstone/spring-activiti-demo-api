package me.xueyao.controller;

import me.xueyao.entity.AjaxResult;
import me.xueyao.entity.BizLeaveVo;
import me.xueyao.entity.TableDataInfo;
import me.xueyao.service.IBizLeaveService;
import me.xueyao.service.IProcessService;
import me.xueyao.util.ShiroUtils;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;

/**
 * 请假业务Controller
 *
 * @author Xianlu Tech
 * @date 2019-10-11
 */
@RestController
@RequestMapping("/leave")
public class BizLeaveController extends BaseController {
    private String prefix = "leave";

    @Autowired
    private IBizLeaveService bizLeaveService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private IProcessService processService;

    @GetMapping()
    public String leave(ModelMap mmap) {
        mmap.put("currentUser", ShiroUtils.getSysUser());
        return prefix + "/leave";
    }

    /**
     * 查询请假业务列表
     */
    @PostMapping("/list")
    public TableDataInfo list(BizLeaveVo bizLeave) {
       /* if (!SysUser.isAdmin(ShiroUtils.getUserId())) {
            bizLeave.setCreateBy(ShiroUtils.getLoginName());
        }*/
        bizLeave.setType("leave");
        startPage();
        List<BizLeaveVo> list = bizLeaveService.selectBizLeaveList(bizLeave);
        return getDataTable(list);
    }

    /**
     * 导出请假业务列表
     */
    @PostMapping("/export")
    public AjaxResult export(BizLeaveVo bizLeave) {
        bizLeave.setType("leave");
        List<BizLeaveVo> list = bizLeaveService.selectBizLeaveList(bizLeave);
//        ExcelUtil<BizLeaveVo> util = new ExcelUtil<BizLeaveVo>(BizLeaveVo.class);
//        return util.exportExcel(list, "leave");
        return null;
    }

    /**
     * 新增请假业务
     */
    @GetMapping("/add")
    public String add() {
        return prefix + "/add";
    }

    /**
     * 新增保存请假业务
     */
    @PostMapping("/add")
    public AjaxResult addSave(BizLeaveVo bizLeave) {
        /*
        Long userId = ShiroUtils.getUserId();
        if (SysUser.isAdmin(userId)) {
            return error("提交申请失败：不允许管理员提交申请！");
        }
        */
        bizLeave.setType("leave");
        return toAjax(bizLeaveService.insertBizLeave(bizLeave));
    }

    /**
     * 修改请假业务
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        BizLeaveVo bizLeave = bizLeaveService.selectBizLeaveById(id);
        mmap.put("bizLeave", bizLeave);
        return prefix + "/edit";
    }

    /**
     * 修改保存请假业务
     */
    @PostMapping("/edit")
    public AjaxResult editSave(BizLeaveVo bizLeave) {
        return toAjax(bizLeaveService.updateBizLeave(bizLeave));
    }

    /**
     * 删除请假业务
     */
    @PostMapping( "/remove")
    public AjaxResult remove(String ids) {
        return toAjax(bizLeaveService.deleteBizLeaveByIds(ids));
    }

    /**
     * 提交申请
     * @param id  任务的id(biz_leave的id)
     * @param applyUserId  由前端传递值，后期改成后端直接获取登录信息
     */
    @PostMapping( "/submitApply")
    public AjaxResult submitApply(Long id, String applyUserId) {
        BizLeaveVo leave = bizLeaveService.selectBizLeaveById(id);
        //String applyUserId = ShiroUtils.getLoginName();
        bizLeaveService.submitApply(leave, applyUserId, "leave", new HashMap<>());
        return success();
    }

    @GetMapping("/leaveTodo")
    public String todoView() {
        return prefix + "/leaveTodo";
    }

    /**
     * 我的待办列表
     * @param bizLeave
     * @param loginName 由前端传递值，后期改成后端直接获取登录信息
     * @return
     */
    @PostMapping("/taskList")
    public TableDataInfo taskList(BizLeaveVo bizLeave, String loginName) {
        bizLeave.setType("leave");
        List<BizLeaveVo> list = bizLeaveService.findTodoTasks(bizLeave, loginName);
        return getDataTable(list);
    }

    /**
     * 加载审批弹窗
     * @param taskId
     * @param mmap
     * @return
     */
    @GetMapping("/showVerifyDialog/{taskId}")
    public String showVerifyDialog(@PathVariable("taskId") String taskId, ModelMap mmap) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        BizLeaveVo bizLeave = bizLeaveService.selectBizLeaveById(new Long(processInstance.getBusinessKey()));
        mmap.put("bizLeave", bizLeave);
        mmap.put("taskId", taskId);
        String verifyName = task.getTaskDefinitionKey().substring(0, 1).toUpperCase() + task.getTaskDefinitionKey().substring(1);
        return prefix + "/task" + verifyName;
    }

    @GetMapping("/showFormDialog/{instanceId}")
    public String showFormDialog(@PathVariable("instanceId") String instanceId, ModelMap mmap) {
        String businessKey = processService.findBusinessKeyByInstanceId(instanceId);
        BizLeaveVo bizLeaveVo = bizLeaveService.selectBizLeaveById(new Long(businessKey));
        mmap.put("bizLeave", bizLeaveVo);
        return prefix + "/view";
    }

    /**
     * 完成任务
     *
     * @return
     */
    @RequestMapping(value = "/complete/{taskId}", method = {RequestMethod.POST, RequestMethod.GET})
    public AjaxResult complete(@PathVariable("taskId") String taskId, @RequestParam(value = "saveEntity", required = false) String saveEntity,
                               @ModelAttribute("preloadLeave") BizLeaveVo leave, HttpServletRequest request) {
        boolean saveEntityBoolean = BooleanUtils.toBoolean(saveEntity);
        processService.complete(taskId, leave.getInstanceId(), leave.getTitle(), leave.getReason(), "leave", new HashMap<String, Object>(), request);
        if (saveEntityBoolean) {
            bizLeaveService.updateBizLeave(leave);
        }
        return success("任务已完成");
    }

    /**
     * 自动绑定页面字段
     */
    @ModelAttribute("preloadLeave")
    public BizLeaveVo getLeave(@RequestParam(value = "id", required = false) Long id, HttpSession session) {
        if (id != null) {
            return bizLeaveService.selectBizLeaveById(id);
        }
        return new BizLeaveVo();
    }

    @GetMapping("/leaveDone")
    public String doneView() {
        return prefix + "/leaveDone";
    }

    /**
     * 我的已办列表
     * @param bizLeave
     * @param loginName 此处先由前端传递参数，以后改成后端获取登录用户名
     * @return
     */
    @PostMapping("/taskDoneList")
    public TableDataInfo taskDoneList(BizLeaveVo bizLeave, String loginName) {
        bizLeave.setType("leave");
        List<BizLeaveVo> list = bizLeaveService.findDoneTasks(bizLeave, loginName);
        return getDataTable(list);
    }

}
