package me.xueyao.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author simonxue
 */
@Data
public class TaskDTO implements Serializable {

    private String id;

    private String name;

    private String description;

    private String assignee;
}
