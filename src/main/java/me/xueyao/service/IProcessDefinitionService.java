package me.xueyao.service;

import me.xueyao.base.R;
import me.xueyao.entity.CustomProcessDefinition;

/**
 * @author Simon.Xue
 * @date 2/4/21 2:31 PM
 **/
public interface IProcessDefinitionService {


    /**
     * 流程定义列表
     *
     * @param customProcessDefinition
     * @return
     */
    R listProcessDefinition(CustomProcessDefinition customProcessDefinition);

    /**
     * 部署流程定义
     *
     * @param filePath
     */
    R deployProcessDefinition(String filePath);

    /**
     * 删除流程定义
     *
     * @param deploymentIds
     * @return
     */
    R deleteProcessDeploymentByIds(String deploymentIds);

    /**
     * 激活或挂起流程定义
     *
     * @param id           流程定义id
     * @param suspendState active 1 suspended 2
     */
    R suspendOrActiveApply(String id, String suspendState);
}
