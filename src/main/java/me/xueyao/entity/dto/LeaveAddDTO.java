package me.xueyao.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Simon.Xue
 * @date 2/9/21 9:39 AM
 **/
@Data
public class LeaveAddDTO implements Serializable {
    /**
     * 标题
     */
    @NotEmpty(message = "标题不能为空")
    private String title;

    /**
     * 原因
     */
    @NotEmpty(message = "申请原因不能为空")
    private String reason;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;


}
