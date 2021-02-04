package me.xueyao.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author simonxue
 */
@Getter
@Setter
public class CustomProcessDefinition extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private String key;

    private int version;

    private String category;

    private String description;

    private String deploymentId;

    private Date deploymentTime;

    private String diagramResourceName;

    private String resourceName;

    /** 流程实例状态 1 激活 2 挂起 */
    private String suspendState;

    private String suspendStateName;

    private Integer pageNum;

    private Integer pageSize;
}
