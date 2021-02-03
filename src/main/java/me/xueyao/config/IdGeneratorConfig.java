package me.xueyao.config;

import lombok.extern.slf4j.Slf4j;
import me.xueyao.util.IdWorker;
import org.activiti.engine.impl.cfg.IdGenerator;
import org.springframework.stereotype.Component;

/**
 * 自定义id策略
 * @author Simon.Xue
 * @date 2/1/21 1:52 PM
 **/
@Slf4j
@Component
public class IdGeneratorConfig implements IdGenerator {
    @Override
    public String getNextId() {
        String nextId = String.valueOf(new IdWorker(1, 1).nextId());
        log.info("nextId = {}", nextId);
        return nextId;
    }
}
