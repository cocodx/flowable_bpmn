package io.github.cocodx.listener;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.HashMap;

public class ApplyTaskListener implements TaskListener {
//    @Override
//    public void notify(DelegateExecution execution) {
//
//    }

    @Override
    public void notify(DelegateTask delegateTask) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("reject",0);//拒绝人数
        map.put("agree",0);//同意人数
        map.put("status","false");//同意人数
        delegateTask.setVariables(map);
    }
}
