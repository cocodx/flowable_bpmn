package io.github.cocodx;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 或签测试
 */
public class Test03 {

    ProcessEngineConfiguration configuration;

    //?currentSchema=flowable&stringtype=unspecified&nullCatalogMeansCurrent=true
    @Before
    public void before(){
        configuration = new StandaloneProcessEngineConfiguration();
        configuration.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres")
                .setJdbcUsername("postgres")
                .setJdbcPassword("password")
                .setJdbcDriver("org.postgresql.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE)
                .setDatabaseSchema("flowable");
    }

    /**
     * 部署deploy.getId()=1
     */
    @Test
    public void testDeploy(){
        //1. 获取 ProcessEngine 对象
        ProcessEngine processEngine = configuration.buildProcessEngine();

        //2. 获取RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();

        //3. 完成流程的部署操作
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("huoqian.bpmn20.xml")
                .name("或签流程")
                .deploy()//流程部署
                ;

        System.out.println("部署ID deploy.getId()="+deploy.getId());
        System.out.println("deploy.getName()="+deploy.getName());
        System.out.println("deploy.getKey() = " + deploy.getKey());
    }

    /**
     * huoqianKey:1:4
     *
     */
    @Test
    public void testDeployQuery(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        ProcessDefinition processDefinition = processDefinitionQuery.deploymentId("1").singleResult();

        System.out.println("processDefinition.getDeploymentId()" + processDefinition.getDeploymentId());
        System.out.println("processDefinition.getName()" + processDefinition.getName());
        System.out.println("processDefinition.getDescription()" + processDefinition.getDescription());
        System.out.println("processDefinition.getId()" + processDefinition.getId());

    }

    /**
     * holidayRequest.getProcessDefinitionId(); = huoqianKey:1:4
     * holidayRequest.getActivityId(); = null
     * holidayRequest.getId(); = 2501
     */
    @Test
    public void testRunProcess(){
        ProcessEngine processEngine = configuration.buildProcessEngine();

        //我们需要通过RuntimeService来启动流程实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //构建流程实例
        Map<String,Object> variables = new HashMap<>();
        variables.put("employee","zhangsan");
        variables.put("outdays",3);
        variables.put("description","工作类了，出去玩玩");
        //启动流程实例
        ProcessInstance holidayRequest = runtimeService.startProcessInstanceByKey("huoqianKey", variables);
//        ProcessInstance holidayRequest = runtimeService.startProcessInstanceByKeyAndTenantId("myTestKey1234", variables, "user1");
        System.out.println("holidayRequest.getProcessDefinitionId(); = " + holidayRequest.getProcessDefinitionId());
        System.out.println("holidayRequest.getActivityId(); = " + holidayRequest.getActivityId());
        System.out.println("holidayRequest.getId(); = " + holidayRequest.getId());

    }


    @Test
    public void testCompleteTask(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("huoqianKey")
                .processInstanceId("2501")
//                .taskAssignee("$INITIATOR")
//                .taskAssignee("user1")
//                .taskAssignee("user2")
                .singleResult();
        //创建流程变量
        Map<String,Object> map = new HashMap<>();
//        map.put("outcome","通过");
//        //完成任务
//        taskService.complete(task.getId(),map);
    }

    @org.junit.Test
    public void testHistory(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processDefinitionId("huoqianKey:1:4")
                .finished()//查询的历史记录状态是完成的
                .orderByHistoricActivityInstanceEndTime().asc() //指定排序的字段和顺序
                .list();
        for (HistoricActivityInstance history:list){
            System.out.println(history.getActivityName()+":"+history.getAssignee()+"--"+history.getActivityId()+":"+history.getDurationInMillis()+"毫秒");
        }
    }
}
