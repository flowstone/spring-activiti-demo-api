package me.xueyao.service.impl;

import me.xueyao.entity.TodoItem;
import me.xueyao.entity.SysUser;
import me.xueyao.mapper.TodoItemMapper;
import me.xueyao.mapper.SysUserMapper;
import me.xueyao.service.ITodoItemService;
import me.xueyao.util.Convert;
import me.xueyao.util.DateUtils;
import me.xueyao.util.StringUtils;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 待办事项Service业务层处理
 *
 * @author Xianlu Tech
 * @date 2019-11-08
 */
@Service
@Transactional
public class TodoItemServiceImpl implements ITodoItemService {
    @Autowired
    private TodoItemMapper todoItemMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private TaskService taskService;

    /**
     * 查询待办事项
     *
     * @param id 待办事项ID
     * @return 待办事项
     */
    @Override
    public TodoItem selectBizTodoItemById(Long id) {
        return todoItemMapper.selectBizTodoItemById(id);
    }

    /**
     * 查询待办事项列表
     *
     * @param todoItem 待办事项
     * @return 待办事项
     */
    @Override
    public List<TodoItem> selectBizTodoItemList(TodoItem todoItem) {
        return todoItemMapper.selectBizTodoItemList(todoItem);
    }

    /**
     * 新增待办事项
     *
     * @param todoItem 待办事项
     * @return 结果
     */
    @Override
    public int insertBizTodoItem(TodoItem todoItem) {
        return todoItemMapper.insertBizTodoItem(todoItem);
    }

    /**
     * 修改待办事项
     *
     * @param todoItem 待办事项
     * @return 结果
     */
    @Override
    public int updateBizTodoItem(TodoItem todoItem) {
        return todoItemMapper.updateBizTodoItem(todoItem);
    }

    /**
     * 删除待办事项对象
     *
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteBizTodoItemByIds(String ids) {
        return todoItemMapper.deleteBizTodoItemByIds(Convert.toStrArray(ids));
    }

    /**
     * 删除待办事项信息
     *
     * @param id 待办事项ID
     * @return 结果
     */
    @Override
    public int deleteBizTodoItemById(Long id) {
        return todoItemMapper.deleteBizTodoItemById(id);
    }

    @Override
    public int insertTodoItem(String instanceId, String itemName, String itemContent, String module) {
        TodoItem todoItem = new TodoItem();
        todoItem.setItemName(itemName);
        todoItem.setItemContent(itemContent);
        todoItem.setIsView("0");
        todoItem.setIsHandle("0");
        todoItem.setModule(module);
        todoItem.setTodoTime(DateUtils.getNowDate());
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(instanceId).active().list();
        int counter = 0;
        for (Task task : taskList) {

            // todoitem 去重
            TodoItem bizTodoItem = todoItemMapper.selectTodoItemByTaskId(task.getId());
            if (bizTodoItem != null) {
                continue;
            }

            TodoItem newItem = new TodoItem();
            BeanUtils.copyProperties(todoItem, newItem);
            newItem.setInstanceId(instanceId);
            newItem.setTaskId(task.getId());
            newItem.setTaskName("task" + task.getTaskDefinitionKey().substring(0, 1).toUpperCase() + task.getTaskDefinitionKey().substring(1));
            newItem.setNodeName(task.getName());
            String assignee = task.getAssignee();
            if (StringUtils.isNotBlank(assignee)) {
                newItem.setTodoUserId(assignee);
                SysUser user = userMapper.selectUserByLoginName(assignee);
                newItem.setTodoUserName(user.getUserName());
                todoItemMapper.insertBizTodoItem(newItem);
                counter++;
            } else {
                // 查询候选用户组
                List<String> todoUserIdList = todoItemMapper.selectTodoUserListByTaskId(task.getId());
                if (!CollectionUtils.isEmpty(todoUserIdList)) {
                    for (String todoUserId : todoUserIdList) {
                        SysUser todoUser = userMapper.selectUserByLoginName(todoUserId);
                        newItem.setTodoUserId(todoUser.getLoginName());
                        newItem.setTodoUserName(todoUser.getUserName());
                        todoItemMapper.insertBizTodoItem(newItem);
                        counter++;
                    }
                } else {
                    // 查询候选用户
                    String todoUserId = todoItemMapper.selectTodoUserByTaskId(task.getId());
                    SysUser todoUser = userMapper.selectUserByLoginName(todoUserId);
                    newItem.setTodoUserId(todoUser.getLoginName());
                    newItem.setTodoUserName(todoUser.getUserName());
                    todoItemMapper.insertBizTodoItem(newItem);
                    counter++;
                }
            }
        }
        return counter;
    }

    @Override
    public TodoItem selectBizTodoItemByCondition(String taskId, String todoUserId) {
        return todoItemMapper.selectTodoItemByCondition(taskId, todoUserId);
    }
}
