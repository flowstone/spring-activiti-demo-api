package me.xueyao.entity;

import lombok.Data;

import java.util.Date;

/**
 * 请假业务对象 biz_leave
 *
 * @author Xianlu Tech
 * @date 2019-10-11
 */
@Data
public class BizLeave extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 请假类型 */
    private String type;

    /** 标题 */
    private String title;

    /** 原因 */
    private String reason;

    /** 开始时间 */
    private Date startTime;

    /** 结束时间 */
    private Date endTime;

    /** 请假时长，单位秒 */
    private Long totalTime;

    /** 流程实例ID */
    private String instanceId;

    /** 申请人 */
    private String applyUser;

    /** 申请时间 */
    private Date applyTime;

    /** 实际开始时间 */
    private Date realityStartTime;

    /** 实际结束时间 */
    private Date realityEndTime;


}
