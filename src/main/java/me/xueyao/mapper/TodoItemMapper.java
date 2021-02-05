package me.xueyao.mapper;

import me.xueyao.entity.TodoItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 待办事项Mapper接口
 *
 * @author Xianlu Tech
 * @date 2019-11-08
 */
@Repository
public interface TodoItemMapper {
    /**
     * 查询待办事项
     *
     * @param id 待办事项ID
     * @return 待办事项
     */
    TodoItem selectBizTodoItemById(Long id);

    /**
     * 查询待办事项列表
     *
     * @param todoItem 待办事项
     * @return 待办事项集合
     */
    List<TodoItem> selectBizTodoItemList(TodoItem todoItem);

    /**
     * 新增待办事项
     *
     * @param todoItem 待办事项
     * @return 结果
     */
    int insertBizTodoItem(TodoItem todoItem);

    /**
     * 修改待办事项
     *
     * @param todoItem 待办事项
     * @return 结果
     */
    int updateBizTodoItem(TodoItem todoItem);

    /**
     * 删除待办事项
     *
     * @param id 待办事项ID
     * @return 结果
     */
    int deleteBizTodoItemById(Long id);

    /**
     * 批量删除待办事项
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    int deleteBizTodoItemByIds(String[] ids);

    /**
     * @param taskId
     * @return
     */
    @Select("SELECT * FROM BIZ_TODO_ITEM WHERE TASK_ID = #{taskId}")
    TodoItem selectTodoItemByTaskId(@Param(value = "taskId") String taskId);

    /**
     * 此处有问题题 后期改 请查询用户表和角色表
     *
     * @param taskId
     * @return
     */
    @Select("SELECT USER_ID_ FROM ACT_ID_MEMBERSHIP WHERE GROUP_ID_ = (SELECT GROUP_ID_ FROM ACT_RU_IDENTITYLINK WHERE TASK_ID_ = #{taskId})")
    List<String> selectTodoUserListByTaskId(@Param(value = "taskId") String taskId);

    /**
     * 查询
     *
     * @param taskId
     * @param todoUserId
     * @return
     */
    @Select("SELECT * FROM BIZ_TODO_ITEM WHERE TASK_ID = #{taskId} AND TODO_USER_ID = #{todoUserId}")
    TodoItem selectTodoItemByCondition(@Param(value = "taskId") String taskId, @Param(value = "todoUserId") String todoUserId);

    /**
     * @param id
     * @return
     */
    @Select("SELECT USER_ID_ FROM ACT_ID_MEMBERSHIP WHERE USER_ID_ = (SELECT USER_ID_ FROM ACT_RU_IDENTITYLINK WHERE TASK_ID_ = #{taskId})")
    String selectTodoUserByTaskId(String id);
}
