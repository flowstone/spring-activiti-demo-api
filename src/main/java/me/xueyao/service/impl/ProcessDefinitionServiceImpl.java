package me.xueyao.service.impl;

import com.github.pagehelper.Page;
import lombok.extern.slf4j.Slf4j;
import me.xueyao.base.PageResult;
import me.xueyao.base.R;
import me.xueyao.entity.CustomProcessDefinition;
import me.xueyao.service.IProcessDefinitionService;
import me.xueyao.util.Convert;
import me.xueyao.util.StringUtils;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.zip.ZipInputStream;

@Slf4j
@Service
public class ProcessDefinitionServiceImpl implements IProcessDefinitionService {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    /**
     * 分页查询流程定义文件
     *
     * @return
     */
    @Override
    public R listProcessDefinition(CustomProcessDefinition customProcessDefinition) {
        //手动分页
        Integer pageNum = customProcessDefinition.getPageNum();
        Integer pageSize = customProcessDefinition.getPageSize();

        Page<CustomProcessDefinition> list = new Page<>();
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        processDefinitionQuery.orderByProcessDefinitionId().orderByProcessDefinitionVersion().desc();
        if (StringUtils.isNotBlank(customProcessDefinition.getName())) {
            processDefinitionQuery.processDefinitionNameLike("%" + customProcessDefinition.getName() + "%");
        }
        if (StringUtils.isNotBlank(customProcessDefinition.getKey())) {
            processDefinitionQuery.processDefinitionKeyLike("%" + customProcessDefinition.getKey() + "%");
        }
        if (StringUtils.isNotBlank(customProcessDefinition.getCategory())) {
            processDefinitionQuery.processDefinitionCategoryLike("%" + customProcessDefinition.getCategory() + "%");
        }

        List<ProcessDefinition> processDefinitionList;
        if (pageNum != null && pageSize != null) {
            processDefinitionList = processDefinitionQuery.listPage((pageNum - 1) * pageSize, pageSize);
            list.setTotal(processDefinitionQuery.count());
            list.setPageNum(pageNum);
            list.setPageSize(pageSize);
        } else {
            processDefinitionList = processDefinitionQuery.list();
        }
        for (ProcessDefinition definition : processDefinitionList) {
            ProcessDefinitionEntityImpl entityImpl = (ProcessDefinitionEntityImpl) definition;
            CustomProcessDefinition entity = new CustomProcessDefinition();
            BeanUtils.copyProperties(definition, entity);

            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(definition.getDeploymentId())
                    .singleResult();
            entity.setDeploymentTime(deployment.getDeploymentTime());
            entity.setSuspendState(entityImpl.getSuspensionState() + "");
            if (entityImpl.getSuspensionState() == 1) {
                entity.setSuspendStateName("已激活");
            } else {
                entity.setSuspendStateName("已挂起");
            }
            list.add(entity);
        }

        return R.ofSuccess("查询成功", new PageResult<>(list));
    }

    @Override
    public R deployProcessDefinition(String filePath) {
        if (StringUtils.isNotBlank(filePath)) {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                log.error("e = {}", e.getMessage());
                R.ofSystem("文件不存在");
            }

            if (filePath.endsWith(".zip")) {
                ZipInputStream inputStream = new ZipInputStream(fileInputStream);
                repositoryService.createDeployment()
                        .addZipInputStream(inputStream)
                        .deploy();
            } else if (filePath.endsWith(".bpmn")) {
                repositoryService.createDeployment()
                        .addInputStream(filePath, fileInputStream)
                        .deploy();
            }
        }
        return R.ofSuccess("部署流程定义成功");
    }

    @Override
    public R deleteProcessDeploymentByIds(String deploymentIds) {
        String[] deploymentIdsArr = Convert.toStrArray(deploymentIds);
        int counter = 0;
        for (String deploymentId : deploymentIdsArr) {
            List<ProcessInstance> instanceList = runtimeService.createProcessInstanceQuery()
                    .deploymentId(deploymentId)
                    .list();
            if (!CollectionUtils.isEmpty(instanceList)) {
                // 存在流程实例的流程定义
                R.ofParam("删除失败，存在运行中的流程实例");
            }
            // true 表示级联删除引用，比如 act_ru_execution 数据
            repositoryService.deleteDeployment(deploymentId, true);
            counter++;
        }
        return R.ofSuccess("删除成功", counter);
    }

    @Override
    public R suspendOrActiveApply(String id, String suspendState) {
        if (suspendState.equals("1")) {
            repositoryService.activateProcessDefinitionById(id);
        } else {

            // 当流程定义被挂起时，已经发起的该流程定义的流程实例不受影响（如果选择级联挂起则流程实例也会被挂起）。
            // 当流程定义被挂起时，无法发起新的该流程定义的流程实例。
            // 直观变化：act_re_procdef 的 SUSPENSION_STATE_ 为 2
            repositoryService.suspendProcessDefinitionById(id);
        }
        return R.ofSuccess("操作成功");
    }

}
