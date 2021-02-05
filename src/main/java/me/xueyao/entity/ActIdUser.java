package me.xueyao.entity;

import lombok.Data;

/**
 * 流程用户对象 act_id_user
 *
 * @author Xianlu Tech
 * @date 2019-10-02
 */
@Data
public class ActIdUser extends BaseEntity {
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
     * 名字
     */
    private String first;

    /**
     * 姓氏
     */
    private String last;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 头像
     */
    private String pictureId;

    /**
     * 用户组
     */
    private String[] groupIds;

    /**
     * 用户组是否存在此用户标识 默认不存在
     */
    private boolean flag = false;


}
