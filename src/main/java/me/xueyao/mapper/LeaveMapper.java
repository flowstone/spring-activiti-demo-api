package me.xueyao.mapper;

import com.github.pagehelper.Page;
import me.xueyao.entity.dto.LeaveDTO;
import me.xueyao.entity.vo.LeaveVo;
import org.springframework.stereotype.Repository;

/**
 * 请假业务Mapper接口
 *
 * @author Xianlu Tech
 * @date 2019-10-11
 */
@Repository
public interface LeaveMapper {
    /**
     * 查询请假业务
     *
     * @param id 请假业务ID
     * @return 请假业务
     */
    LeaveVo selectLeaveById(Long id);

    /**
     * 查询请假业务列表
     *
     * @param leaveDTO 请假业务
     * @param page
     * @return 请假业务集合
     */
    Page<LeaveVo> selectLeaveList(LeaveDTO leaveDTO, Page page);

    /**
     * 新增请假业务
     *
     * @param leaveDTO 请假业务
     * @return 结果
     */
    int insertLeave(LeaveDTO leaveDTO);

    /**
     * 修改请假业务
     *
     * @param leaveVo 请假业务
     * @return 结果
     */
    int updateLeave(LeaveVo leaveVo);

    /**
     * 删除请假业务
     *
     * @param id 请假业务ID
     * @return 结果
     */
    int deleteLeaveById(Long id);

    /**
     * 批量删除请假业务
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteLeaveByIds(String[] ids);
}
