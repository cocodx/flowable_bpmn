package io.github.cocodx;

import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 或签测试|会签测试
 */
public class Test04 {

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
     部署ID deploy.getId()=5001
     */
    @Test
    public void testDeploy(){
        //1. 获取 ProcessEngine 对象
        ProcessEngine processEngine = configuration.buildProcessEngine();

        //2. 获取RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();

        //3. 完成流程的部署操作
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("weixiuProcess.bpmn20.xml")
                .name("会签流程")
                .deploy()//流程部署
                ;

        System.out.println("部署ID deploy.getId()="+deploy.getId());
        System.out.println("deploy.getName()="+deploy.getName());
        System.out.println("deploy.getKey() = " + deploy.getKey());
    }

    /**
     processDefinition.getDeploymentId()5001
     processDefinition.getName()weixiuProcess
     processDefinition.getDescription()维系流程
     processDefinition.getId()weixiu:1:5004
     */
    @Test
    public void testDeployQuery(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        ProcessDefinition processDefinition = processDefinitionQuery.deploymentId("5001").singleResult();

        System.out.println("processDefinition.getDeploymentId()" + processDefinition.getDeploymentId());
        System.out.println("processDefinition.getName()" + processDefinition.getName());
        System.out.println("processDefinition.getDescription()" + processDefinition.getDescription());
        System.out.println("processDefinition.getId()" + processDefinition.getId());

    }

    /**
     holidayRequest.getProcessDefinitionId(); = weixiu:1:5004
     holidayRequest.getActivityId(); = null
     holidayRequest.getId(); = 7501
     */
    @Test
    public void testRunProcess(){
        ProcessEngine processEngine = configuration.buildProcessEngine();

        //我们需要通过RuntimeService来启动流程实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //构建流程实例
        Map<String,Object> variables = new HashMap<>();
        variables.put("employee","zhangsan");
        variables.put("description","78号主机，显卡坏了");
        ArrayList<String> userNames = new ArrayList<>();
        userNames.add("m1");
        userNames.add("m2");
        userNames.add("m3");
        userNames.add("m4");
        userNames.add("m5");
        userNames.add("m6");
        variables.put("taskUserList",userNames);
        //启动流程实例
        ProcessInstance holidayRequest = runtimeService.startProcessInstanceByKey("weixiu", variables);
//        ProcessInstance holidayRequest = runtimeService.startProcessInstanceByKeyAndTenantId("myTestKey1234", variables, "user1");
        System.out.println("holidayRequest.getProcessDefinitionId(); = " + holidayRequest.getProcessDefinitionId());
        System.out.println("holidayRequest.getActivityId(); = " + holidayRequest.getActivityId());
        System.out.println("holidayRequest.getId(); = " + holidayRequest.getId());
    }

    /**
     * 查询用户的任务
     */
    @Test
    public void testQueryTask(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        //得到查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        //指定查询的流程编号
        taskQuery.processDefinitionKey("weixiu");
        //查询这个任务的处理组
        ArrayList<String> userNames = new ArrayList<>();
        userNames.add("m1");
        userNames.add("m2");
        userNames.add("m3");
        userNames.add("m4");
        userNames.add("m5");
        userNames.add("m6");

        List<Task> allTask = new ArrayList<>();
        for (String userName:userNames){
            taskQuery.taskAssignee(userName);
            List<Task> list = taskQuery.list();
            allTask.addAll(list);
        }

        for(Task task:allTask){
            System.out.println("task.getProcessDefinitionId() = " + task.getProcessDefinitionId());
            System.out.println("task.getName() = " + task.getName());
            System.out.println("task.getAssignee() = " + task.getAssignee());
            System.out.println("task.getDescription() = " + task.getDescription());
            System.out.println("task.getId() = " + task.getId());
            System.out.println("---------------");
        }
    }

    /**
     * m1,m2完成任务
     */
    @Test
    public void testM1M2(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        //得到查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        //指定查询的流程编号
        taskQuery.processDefinitionKey("weixiu");
        //查询这个任务的处理组
        ArrayList<String> userNames = new ArrayList<>();
        userNames.add("m1");
        userNames.add("m2");
        List<Task> allTask = new ArrayList<>();
        for (String userName:userNames){
            taskQuery.taskAssignee(userName);
            List<Task> list = taskQuery.list();
            allTask.addAll(list);
        }
        allTask.forEach(item->{
            taskService.complete(item.getId());
        });
    }

    /**
     * m1,m2完成任务了。看看流程结束没
     * 结果 eee
     * m3-m6都完成任务了，看看完成没 rrr
     */
    @Test
    public void createFlowInstanceImage() throws Exception {
        ProcessEngine processEngine = configuration.buildProcessEngine();
        //1.获取当前的流程实例
        ProcessInstance processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId("7501").singleResult();
        String processDefinitionId;
        List<String> activeActivityIds = new ArrayList<>();
        List<String> highLightedFlows = new ArrayList<>();
        // 2.获取所有的历史轨迹线对象
        List<HistoricActivityInstance> list = processEngine.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId("7501")
                .activityType(BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW).list();
        list.forEach(item -> highLightedFlows.add(item.getActivityId()));
        // 3.获取流程定义id和高亮的节点id
        if (processInstance!=null){
            processDefinitionId = processInstance.getProcessDefinitionId();
            activeActivityIds = processEngine.getRuntimeService().getActiveActivityIds("7501");
        }else{
            // 3.2 已经结束的流程实例
            HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceId("7501").singleResult();
            processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            // 3.3 获取结束节点列表
            List<HistoricActivityInstance> historicEnds = processEngine.getHistoryService().createHistoricActivityInstanceQuery()
                    .processInstanceId("7501").activityType(BpmnXMLConstants.ELEMENT_EVENT_END).list();
            List<String> finalActiveActivityIds = activeActivityIds;
            historicEnds.forEach(historicActivityInstance -> finalActiveActivityIds.add(historicActivityInstance.getActivityId()));
        }
        // 4. 获取bpmnModel对象
        BpmnModel bpmnModel = processEngine.getRepositoryService().getBpmnModel(processDefinitionId);
        // 5. 生成图片流
        ProcessEngineConfiguration configuration = processEngine.getProcessEngineConfiguration();
        ProcessDiagramGenerator diagramGenerator = configuration.getProcessDiagramGenerator();
        InputStream inputStream = diagramGenerator.generateDiagram(bpmnModel, "png", activeActivityIds,
                highLightedFlows, "宋体", "宋体", "宋体",
                this.getClass().getClassLoader(), 1.0, true);

        FileOutputStream fileOutputStream = new FileOutputStream("d:/rrr.png");
        byte[] bytes = new byte[1024 * 10];
        int index = 0;
        while ((index=inputStream.read(bytes))!=-1){
            fileOutputStream.write(bytes,0,index);
        }
        fileOutputStream.close();
        inputStream.close();
    }

    /**
     * 都完成任务，再看看任务结束没
     */
    @Test
    public void testM3_M6(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        //得到查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        //指定查询的流程编号
        taskQuery.processDefinitionKey("weixiu");
        //查询这个任务的处理组
        ArrayList<String> userNames = new ArrayList<>();
        userNames.add("m3");
        userNames.add("m4");
        userNames.add("m5");
        userNames.add("m6");
        List<Task> allTask = new ArrayList<>();
        for (String userName:userNames){
            taskQuery.taskAssignee(userName);
            List<Task> list = taskQuery.list();
            allTask.addAll(list);
        }
        allTask.forEach(item->{
            taskService.complete(item.getId());
        });
    }
}
