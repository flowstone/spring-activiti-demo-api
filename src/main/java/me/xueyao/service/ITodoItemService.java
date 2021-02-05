package me.xueyao.service;


import me.xueyao.entity.TodoItem;

import java.util.List;

/**
 * 待办事项Service接口
 *
 * @author Xianlu Tech
 * @date 2019-11-08
 */
public interface ITodoItemService {
    /**
     * 查询待办事项
     *
     * @param id 待办事项ID
     * @return 待办事项
     */
    public TodoItem selectBizTodoItemById(Long id);

    /**
     * 查询待办事项列表
     *
     * @param todoItem 待办事项
     * @return 待办事项集合
     */
    public List<TodoItem> selectBizTodoItemList(TodoItem todoItem);

    /**
     * 新增待办事项
     *
     * @param todoItem 待办事项
     * @return 结果
     */
    public int insertBizTodoItem(TodoItem todoItem);

    /**
     * 修改待办事项
     *
     * @param todoItem 待办事项
     * @return 结果
     */
    public int updateBizTodoItem(TodoItem todoItem);

    /**
     * 批量删除待办事项
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteBizTodoItemByIds(String ids);

    /**
     * 删除待办事项信息
     *
     * @param id 待办事项ID
     * @return 结果
     */
    public int deleteBizTodoItemById(Long id);

    int insertTodoItem(String instanceId, String itemName, String itemContent, String module);

    TodoItem selectBizTodoItemByCondition(String taskId, String todoUserId);
}
