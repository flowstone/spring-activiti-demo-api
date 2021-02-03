package me.xueyao.repository;

import org.apache.ibatis.annotations.Mapper;

/**
 * @author Simon.Xue
 * @date 2/1/21 3:29 PM
 **/
@Mapper
public interface ActivitiMapper {

    void updateActReProcdef(String id);
}
