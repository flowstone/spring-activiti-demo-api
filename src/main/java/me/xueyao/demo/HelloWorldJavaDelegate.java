package me.xueyao.demo;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

/**
 * Service Task 需要指定 class全路径
 * me.xueyao.demo.HelloWorldJavaDelegate
 * @author Simon.Xue
 * @date 2/8/21 4:15 PM
 **/
@Slf4j
public class HelloWorldJavaDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        log.info("processInstanceId = {}, currentActivityId = {}",
                execution.getProcessInstanceId(), execution.getCurrentActivityId());
        log.info("---- 开始调用服务 ----");
        log.info("想啥呢，这只是一个Hello World！");
        log.info("---- 结束调用服务 ----");
    }
}
