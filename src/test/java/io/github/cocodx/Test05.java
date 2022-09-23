package io.github.cocodx;

import org.flowable.bpmn.constants.BpmnXMLConstants;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * 顺序，会签，后面有一个排它网关
 */
public class Test05 {

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
                .addClasspathResource("huiqian2.bpmn20.xml")
                .name("会签2流程")
                .deploy()//流程部署
                ;

        System.out.println("部署ID deploy.getId()="+deploy.getId());
        System.out.println("deploy.getName()="+deploy.getName());
        System.out.println("deploy.getKey() = " + deploy.getKey());
    }

    @Test
    public void testRunProcess(){
        ProcessEngine processEngine = configuration.buildProcessEngine();

        //我们需要通过RuntimeService来启动流程实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //构建流程实例
        Map<String,Object> variables = new HashMap<>();
        variables.put("employee","zhangsan");
        variables.put("description","聚餐地点，梧桐里，投票");
        variables.put("persons", Arrays.asList("张三","李四","王五"));
        //启动流程实例
        ProcessInstance holidayRequest = runtimeService.startProcessInstanceByKey("huiqian2Key", variables);
//        ProcessInstance holidayRequest = runtimeService.startProcessInstanceByKeyAndTenantId("myTestKey1234", variables, "user1");
        System.out.println("holidayRequest.getProcessDefinitionId(); = " + holidayRequest.getProcessDefinitionId());
        System.out.println("holidayRequest.getActivityId(); = " + holidayRequest.getActivityId());
        System.out.println("holidayRequest.getId(); = " + holidayRequest.getId());
    }

    /**
     * 查询用户的任务
     */
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
        taskQuery.processDefinitionKey("huiqian2Key");
        //查询这个任务的处理组
        List<Task> allTask = new ArrayList<>();
        for (String userName:Arrays.asList("张三","李四","王五")){
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
     * 完成任务
     */
    @Test
    public void testCompleteTask(){
        ProcessEngine processEngine = configuration.buildProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        //得到查询对象
        TaskQuery taskQuery = taskService.createTaskQuery();
        //指定查询的流程编号
        taskQuery.processDefinitionKey("huiqian2Key");
        taskQuery.processInstanceId("32501");
        //查询这个任务的处理组
        Task task = taskQuery
                .taskAssignee("王五")
                .singleResult();
        if (task!=null){
            HashMap<String, Object> map = new HashMap<>();
            Integer agree = (Integer) taskService.getVariable(task.getId(), "agree");
            map.put("agree",agree+1);
            Integer reject = (Integer) taskService.getVariable(task.getId(), "reject");
            if (agree>reject){
                map.put("status","true");
            }
            taskService.complete(task.getId(),map);
        }

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
        ProcessInstance processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId("32501").singleResult();
        String processDefinitionId;
        List<String> activeActivityIds = new ArrayList<>();
        List<String> highLightedFlows = new ArrayList<>();
        // 2.获取所有的历史轨迹线对象
        List<HistoricActivityInstance> list = processEngine.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId("32501")
                .activityType(BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW).list();
        list.forEach(item -> highLightedFlows.add(item.getActivityId()));
        // 3.获取流程定义id和高亮的节点id
        if (processInstance!=null){
            processDefinitionId = processInstance.getProcessDefinitionId();
            activeActivityIds = processEngine.getRuntimeService().getActiveActivityIds("32501");
        }else{
            // 3.2 已经结束的流程实例
            HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceId("32501").singleResult();
            processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            // 3.3 获取结束节点列表
            List<HistoricActivityInstance> historicEnds = processEngine.getHistoryService().createHistoricActivityInstanceQuery()
                    .processInstanceId("32501").activityType(BpmnXMLConstants.ELEMENT_EVENT_END).list();
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
}
