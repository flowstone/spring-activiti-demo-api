package me.xueyao.entity.dto;

import lombok.Data;
import me.xueyao.entity.Leave;

import java.util.Date;

/**
 * @author simonxue
 */
@Data
public class LeaveUpdateDTO extends Leave {

    /**
     * 申请人姓名
     */
    private String applyUserName;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 办理时间
     */
    private Date doneTime;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 流程实例状态 1 激活 2 挂起
     */
    private String suspendState;


    private Integer pageNum;

    private Integer pageSize;


}
