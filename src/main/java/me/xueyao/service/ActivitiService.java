package me.xueyao.service;

import me.xueyao.base.R;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Simon.Xue
 * @date 2/1/21 3:01 PM
 **/
public interface ActivitiService {
    /**
     * 启动实例流程
     * @param processDefinitionKey
     * @return
     */
    R start(String processDefinitionKey);

    /**
     * 查询任务列表
     * @param username 用户名称
     * @return
     */
    R taskList(String username);

    /**
     * 完成任务
     * @param taskId
     * @param key
     * @param value
     * @return
     */
    R managerApprove(String taskId, String key, String value);

    /**
     * 撤回
     * @param currentTaskId
     * @param targetTaskId
     * @return
     */
    R back(String currentTaskId, String targetTaskId);

    /**
     * 显示图片
     * @param instanceKey
     * @param response
     * @return
     */
    void showImg(String instanceKey, HttpServletResponse response);



    /**
     * 导入流程定义
     * @param file
     * @param type
     * @param typeName
     * @return
     */
    R importXml(MultipartFile file, String type, String typeName);
}
