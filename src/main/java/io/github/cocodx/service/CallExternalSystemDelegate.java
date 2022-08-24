package io.github.cocodx.service;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * @author amazfit
 * @date 2022-08-25 上午1:57
 **/
public class CallExternalSystemDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) {
        System.out.println("Calling the external system for employee "+ delegateExecution.getVariable("employee"));
    }
}
