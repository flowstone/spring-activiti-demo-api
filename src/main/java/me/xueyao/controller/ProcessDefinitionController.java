package me.xueyao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import me.xueyao.base.R;
import me.xueyao.config.Global;
import me.xueyao.constant.Constants;
import me.xueyao.entity.CustomProcessDefinition;
import me.xueyao.service.IProcessDefinitionService;
import me.xueyao.util.FileUploadUtils;
import me.xueyao.util.StringUtils;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * 流程定义相关操作
 * @author simonxue
 */
@Slf4j
@RestController
@RequestMapping("/definition")
public class ProcessDefinitionController {


    private String prefix = "definition";

    @Autowired
    private IProcessDefinitionService iProcessDefinitionService;
    @Autowired
    private RepositoryService repositoryService;

    @GetMapping
    public String processDefinition() {
        return prefix + "/definition";
    }

    @PostMapping("/list")
    @ResponseBody
    public R list(@RequestParam("pageNum") Integer pageNum,
                  @RequestParam("pageSize") Integer pageSize) {
        CustomProcessDefinition processDefinition = new CustomProcessDefinition();
        processDefinition.setPageNum(pageNum);
        processDefinition.setPageSize(pageSize);
        return iProcessDefinitionService.listProcessDefinition(processDefinition);
    }

    /**
     * 部署流程定义
     */
    @PostMapping("/upload")
    @ResponseBody
    public R upload(@RequestParam("processDefinition") MultipartFile file) {
        try {
            if (!file.isEmpty()) {
                String extensionName = file.getOriginalFilename()
                        .substring(file.getOriginalFilename().lastIndexOf('.') + 1);
                if (!"bpmn".equalsIgnoreCase(extensionName)
                        && !"zip".equalsIgnoreCase(extensionName)
                        && !"bar".equalsIgnoreCase(extensionName)) {
                    return R.ofParam("流程定义文件仅支持 bpmn, zip 和 bar 格式！");
                }
                // p.s. 此时 FileUploadUtils.upload() 返回字符串 fileName 前缀为 Constants.RESOURCE_PREFIX，需剔除
                // 详见: FileUploadUtils.getPathFileName(...)
                String fileName = FileUploadUtils.upload(Global.getProfile() + "/processDefiniton", file);
                if (StringUtils.isNotBlank(fileName)) {
                    String realFilePath = Global.getProfile() + fileName.substring(Constants.RESOURCE_PREFIX.length());
                    return iProcessDefinitionService.deployProcessDefinition(realFilePath);
                }
            }
            return R.ofParam("不允许上传空文件！");
        } catch (Exception e) {
            log.error("上传流程定义文件失败！", e);
            return R.ofParam(e.getMessage());
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public R remove(String ids) {
        return iProcessDefinitionService.deleteProcessDeploymentByIds(ids);
    }


    @PostMapping("/suspendOrActiveApply")
    public R suspendOrActiveApply(String id, String suspendState) {
        iProcessDefinitionService.suspendOrActiveApply(id, suspendState);
        return R.ofSuccess("Success");
    }

    /**
     * 读取流程资源
     *
     * @param processDefinitionId 流程定义ID
     * @param resourceName        资源名称
     */
    @RequestMapping(value = "/readResource")
    public void readResource(@RequestParam("pdid") String processDefinitionId,
                             @RequestParam("resourceName") String resourceName,
                             HttpServletResponse response)
            throws Exception {
        ProcessDefinitionQuery pdq = repositoryService.createProcessDefinitionQuery();
        org.activiti.engine.repository.ProcessDefinition pd = pdq.processDefinitionId(processDefinitionId).singleResult();

        // 通过接口读取
        InputStream resourceAsStream = repositoryService.getResourceAsStream(pd.getDeploymentId(), resourceName);

        // 输出资源内容到相应对象
        byte[] b = new byte[1024];
        int len = -1;
        while ((len = resourceAsStream.read(b, 0, 1024)) != -1) {
            response.getOutputStream().write(b, 0, len);
        }
    }

    /**
     * 转换流程定义为模型
     *
     * @param processDefinitionId
     * @return
     * @throws UnsupportedEncodingException
     * @throws XMLStreamException
     */
    @PostMapping(value = "/convert2Model")
    public R convertToModel(@Param("processDefinitionId") String processDefinitionId)
            throws UnsupportedEncodingException, XMLStreamException {
        org.activiti.engine.repository.ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                processDefinition.getResourceName());
        XMLInputFactory xif = XMLInputFactory.newInstance();
        InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
        XMLStreamReader xtr = xif.createXMLStreamReader(in);
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

        BpmnJsonConverter converter = new BpmnJsonConverter();
        ObjectNode modelNode = converter.convertToJson(bpmnModel);
        Model modelData = repositoryService.newModel();
        modelData.setKey(processDefinition.getKey());
        modelData.setName(processDefinition.getResourceName());
        modelData.setCategory(processDefinition.getDeploymentId());

        ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processDefinition.getName());
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processDefinition.getDescription());
        modelData.setMetaInfo(modelObjectNode.toString());

        repositoryService.saveModel(modelData);

        repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));

        return R.ofSuccess("Success");
    }

}
