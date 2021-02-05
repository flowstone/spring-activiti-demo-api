package me.xueyao.entity;

import lombok.Getter;
import lombok.Setter;
import org.activiti.engine.impl.persistence.entity.HistoricActivityInstanceEntityImpl;

/**
 * 历史任务
 *
 * @author simonxue
 */
@Getter
@Setter
public class HistoricActivity extends HistoricActivityInstanceEntityImpl {

    /**
     * 审批批注
     */
    private String comment;

    /**
     * 办理人姓名
     */
    private String assigneeName;

    private Integer pageNum;

    private Integer pageSize;

}
