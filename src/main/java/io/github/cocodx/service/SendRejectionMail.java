package io.github.cocodx.service;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * @author amazfit
 * @date 2022-09-21 下午11:00
 **/
public class SendRejectionMail implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) {
        System.out.println("不好意思，你的请假被拒绝，安心工作");
    }
}
