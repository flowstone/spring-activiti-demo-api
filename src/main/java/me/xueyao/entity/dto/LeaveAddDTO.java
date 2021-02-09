package me.xueyao.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Simon.Xue
 * @date 2/9/21 9:39 AM
 **/
@ApiModel(value = "添加请假实体")
@Data
public class LeaveAddDTO implements Serializable {
    /**
     * 标题
     */
    @ApiModelProperty(value = "标题", required = true, dataType = "String", position = 0)
    @NotEmpty(message = "标题不能为空")
    private String title;

    /**
     * 原因
     */
    @ApiModelProperty(value = "申请原因", required = true, dataType = "String", position = 1)
    @NotEmpty(message = "申请原因不能为空")
    private String reason;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间", required = true, dataType = "String", position = 2, example = "2021-01-01 01:01:00")
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间", required = true, dataType = "String", position = 3, example = "2021-01-05 01:01:00")
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;


}
