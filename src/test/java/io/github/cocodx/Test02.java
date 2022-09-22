package io.github.cocodx;

import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author amazfit
 * @date 2022-09-21 下午9:26
 **/
public class Test02 {

    ProcessEngineConfiguration configuration;

    @Before
    public void before(){
        configuration = new StandaloneProcessEngineConfiguration();
        configuration.setJdbcUrl("jdbc:postgresql://192.168.101.10:5432/postgres?currentSchema=icube2_0&stringtype=unspecified")
                .setJdbcUsername("icube")
                .setJdbcPassword("yhlo")
                .setJdbcDriver("org.postgresql.Driver")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    }


    /**
     * 部署流程
     *
     * act_re_deployment
     * act_re_procdef
     * act_ge_bytearray
     * deploy.getId() 15001 20001
     */
    @Test
    public void testDeploy(){
        //1. 获取 ProcessEngine 对象
        ProcessEngine processEngine = configuration.buildProcessEngine();

        //2. 获取RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();

        //3. 完成流程的部署操作
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("myTest.bpmn20.xml")
                .name("请假流程")
                .deploy()//流程部署
                ;

        System.out.println(" deploy.getId()="+deploy.getId());
        System.out.println(" deploy.getName()="+deploy.getName());
        System.out.println("deploy.getKey() = " + deploy.getKey());
    }


    /**
     * 查询流程信息
     *
     * 新的请假流程
     *
     * myTestKey:1:5004
     * myTestKey:2:12504
     * myTestKey123:1:15004
     */
    @Test
    public void testDeployQuery(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        ProcessDefinition processDefinition = processDefinitionQuery.deploymentId("20001").singleResult();

        System.out.println("processDefinition.getDeploymentId()" + processDefinition.getDeploymentId());
        System.out.println("processDefinition.getName()" + processDefinition.getName());
        System.out.println("processDefinition.getDescription()" + processDefinition.getDescription());
        System.out.println("processDefinition.getId()" + processDefinition.getId());

    }

    /**
     * 删除流程定义
     */
    @org.junit.Test
    public void testDeleteDeploy(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //第一个参数是id，如果部署的流程启动了，就不允许删除
//        repositoryService.deleteDeployment("2501");
        //第二个参数是级联删除，如果流程启动了，相关的任务一并会被删除掉
        repositoryService.deleteDeployment("2501",true);
    }

    /**
     * 启动流程实例
     *
     * 一般来说，接收表单数据
     * ProcessInstance getId 流程实例
     * 表 ru
     *
     * holidayRequest.getProcessDefinitionId(); = myTestKey1234:1:20004
     * holidayRequest.getActivityId(); = null
     * holidayRequest.getId(); = 22501
     *
     * 任务表 act_hi_taskinst
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
        ProcessInstance holidayRequest = runtimeService.startProcessInstanceByKey("myTestKey1234", variables);
        System.out.println("holidayRequest.getProcessDefinitionId(); = " + holidayRequest.getProcessDefinitionId());
        System.out.println("holidayRequest.getActivityId(); = " + holidayRequest.getActivityId());
        System.out.println("holidayRequest.getId(); = " + holidayRequest.getId());

    }

    /**
     * 任务查询
     * 待处理的任务，已处理的任务
     *
     * act_ru_task 待处理任务表
     */
    @Test
    public void testQueryTask(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        //得到查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        //指定查询的流程编号
        taskQuery.processDefinitionKey("myTestKey1234");
        //查询这个任务的处理组
//        taskQuery.taskAssignee("$INITIATOR");
//        taskQuery.taskAssignee("user1");
        taskQuery.taskAssignee("user2");
        List<Task> list = taskQuery.list();
        for(Task task:list){
            System.out.println("task.getProcessDefinitionId() = " + task.getProcessDefinitionId());
            System.out.println("task.getName() = " + task.getName());
            System.out.println("task.getAssignee() = " + task.getAssignee());
            System.out.println("task.getDescription() = " + task.getDescription());
            System.out.println("task.getId() = " + task.getId());
        }
    }

    /**
     * 完成当前任务
     */
    @Test
    public void testCompleteTask(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("myTestKey1234")
//                .taskAssignee("$INITIATOR")
//                .taskAssignee("user1")
                .taskAssignee("user2")
                .singleResult();
        //创建流程变量
        Map<String,Object> map = new HashMap<>();
        map.put("outcome","通过");
        //完成任务
        taskService.complete(task.getId(),map);
    }

    @org.junit.Test
    public void testHistory(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        HistoryService historyService = processEngine.getHistoryService();
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()
                .processDefinitionId("myTestKey1234:1:20004")
                .finished()//查询的历史记录状态是完成的
                .orderByHistoricActivityInstanceEndTime().asc() //指定排序的字段和顺序
                .list();
        for (HistoricActivityInstance history:list){
            System.out.println(history.getActivityName()+":"+history.getAssignee()+"--"+history.getActivityId()+":"+history.getDurationInMillis()+"毫秒");
        }
    }
}
