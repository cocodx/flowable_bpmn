package io.github.cocodx.service;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * @author amazfit
 * @date 2022-08-25 上午1:57
 **/
public class CallExternalSystemDelegate implements JavaDelegate {
    /**
     * 这是一个flowable触发器
     * @param delegateExecution
     */
    @Override
    public void execute(DelegateExecution delegateExecution) {
        //触发执行的逻辑 按照我们在流程中的定义应该给被拒绝的员工发送通知的邮件
        System.out.println("Calling the external system for employee "+ delegateExecution.getVariable("employee"));
    }
}
