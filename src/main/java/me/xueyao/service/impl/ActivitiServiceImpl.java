package me.xueyao.service.impl;

import me.xueyao.base.R;
import me.xueyao.entity.TaskDTO;
import me.xueyao.service.ActivitiService;
import me.xueyao.util.ActivitiUtils;
import org.activiti.api.process.model.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Simon.Xue
 * @date 2/2/21 1:38 PM
 **/
@Service
public class ActivitiServiceImpl implements ActivitiService {
    @Autowired
    private ActivitiUtils activitiUtils;
    @Override
    public R start(String processDefinitionKey) {
        ProcessInstance processInstance = activitiUtils.startProcessInstance(processDefinitionKey, processDefinitionKey, null);
        return R.ofSuccess("启动实例流程", processInstance.getId());
    }

    @Override
    public R taskList(String username) {
        List<Task> taskList = activitiUtils.queryTaskList(username);
        List<TaskDTO> taskDTOList = taskList.stream().map(task -> {
            TaskDTO taskDTO = new TaskDTO();
            taskDTO.setAssignee(task.getAssignee());
            taskDTO.setId(task.getId());
            taskDTO.setName(task.getName());
            taskDTO.setDescription(task.getDescription());
            return taskDTO;
        }).collect(Collectors.toList());
        return R.ofSuccess("查询任务列表成功", taskDTOList);
    }

    @Override
    public R managerApprove(String taskId, String key, String value) {
        Map<String, Object> variables = new HashMap<>(1);
        if (StringUtils.isNoneEmpty(key)
                && StringUtils.isNoneBlank(key)) {
            variables.put(key, value);
        }

        activitiUtils.completeTask(taskId, variables);
        return R.ofSuccess("完成任务");
    }

    @Override
    public R back(String currentTaskId, String targetTaskId) {
        activitiUtils.backTask(currentTaskId, targetTaskId);
        return R.ofSuccess("撤回成功");
    }

    @Override
    public void showImg(String instanceKey, HttpServletResponse response) {
        activitiUtils.showImg(instanceKey, response);
    }


    @Override
    public R importXml(MultipartFile file, String type, String typeName) {
        activitiUtils.importBpmnFile(file, type, typeName);
        return R.ofSuccess("导入流程定义成功");
    }
}
