package me.xueyao.controller;

import me.xueyao.base.R;
import me.xueyao.service.ActivitiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Simon.Xue
 * @date 2/1/21 3:01 PM
 **/
@Controller
//@RequestMapping(value = "/v1/activiti")
public class ActivitiController {
    @Autowired
    private ActivitiService activitiService;

    /**
     * 启动实例流程
     * @param processDefinitionKey
     * @return
     */
    @PostMapping("/start")
    public R start(@RequestParam String processDefinitionKey) {
        return activitiService.start(processDefinitionKey);
    }

    /**
     * 查询任务列表
     * @param username
     * @return
     */
    @GetMapping("/taskList")
    public R taskList(@RequestParam String username) {
        return activitiService.taskList(username);
    }


    /**
     * 完成任务
     * @param taskId
     * @param key
     * @param value
     * @return
     */
    @PostMapping("/approve")
    public R managerApprove(@RequestParam String taskId,
                            @RequestParam(required = false) String key,
                            @RequestParam(required = false) String value) {
        return activitiService.managerApprove(taskId, key, value);
    }

    /**
     * 撤回
     * @param currentTaskId
     * @param targetTaskId
     * @return
     */
    @PostMapping("/back")
    public R back(@RequestParam String currentTaskId,
                  @RequestParam(required = false) String targetTaskId) {
        return activitiService.back(currentTaskId, targetTaskId);
    }

    @GetMapping("/showImg")
    public void showImg(@RequestParam(required = false) String instanceKey, HttpServletResponse response) {
        activitiService.showImg(instanceKey, response);
    }



    @PostMapping("/importXml")
    public R importXml(@RequestParam MultipartFile file,
                       @RequestParam String type,
                       @RequestParam String typeName) {
        return activitiService.importXml(file, type, typeName);
    }
}
