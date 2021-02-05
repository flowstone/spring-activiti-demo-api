package me.xueyao.entity;

import lombok.Data;

/**
 * 流程用户组对象 act_id_group
 *
 * @author Xianlu Tech
 * @date 2019-10-02
 */
@Data
public class ActIdGroup extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private String id;

    /**
     * 版本
     */
    private Long rev;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型
     */
    private String type;

    private String[] userIds;

    /**
     * 用户是否存在此用户组标识 默认不存在
     */
    private boolean flag = false;


}
