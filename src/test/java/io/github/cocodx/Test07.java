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
import org.junit.Before;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

/**
 * 测试有并行网关，与，无并行网关的区别
 *
 * 有并行网关
 * @author amazfit
 * @date 2022-10-12 下午2:17
 **/
public class Test07 {

    ProcessEngineConfiguration configuration;

    /**
     * 要新建模式flowable
     */
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

    @Test
    public void testDeploy(){
        //1. 获取 ProcessEngine 对象
        ProcessEngine processEngine = configuration.buildProcessEngine();

        //2. 获取RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();

        //3. 完成流程的部署操作
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("ceshi2.bpmn20.xml")
                .name("没有并行网关的流程")
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
        ProcessInstance holidayRequest = runtimeService.startProcessInstanceByKey("ceshi2key", variables);
//        ProcessInstance holidayRequest = runtimeService.startProcessInstanceByKeyAndTenantId("myTestKey1234", variables, "user1");
        System.out.println("holidayRequest.getProcessDefinitionId(); = " + holidayRequest.getProcessDefinitionId());
        System.out.println("holidayRequest.getActivityId(); = " + holidayRequest.getActivityId());
        System.out.println("holidayRequest.getId(); = " + holidayRequest.getId());
    }

    @Test
    public void testCompleteTask(){
        String assignee = "8";
        ProcessEngine processEngine = configuration.buildProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        List<Task> taskList = taskService.createTaskQuery().taskAssignee(assignee).list();
        taskList.forEach(item->{
            taskService.complete(item.getId());
        });
    }

    @Test
    public void createFlowInstanceImage() throws Exception {
        String procInstId = "15001";
        ProcessEngine processEngine = configuration.buildProcessEngine();
        //1.获取当前的流程实例
        ProcessInstance processInstance = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(procInstId).singleResult();
        String processDefinitionId;
        List<String> activeActivityIds = new ArrayList<>();
        List<String> highLightedFlows = new ArrayList<>();
        // 2.获取所有的历史轨迹线对象
        List<HistoricActivityInstance> list = processEngine.getHistoryService().createHistoricActivityInstanceQuery().processInstanceId(procInstId)
                .activityType(BpmnXMLConstants.ELEMENT_SEQUENCE_FLOW).list();
        list.forEach(item -> highLightedFlows.add(item.getActivityId()));
        // 3.获取流程定义id和高亮的节点id
        if (processInstance!=null){
            processDefinitionId = processInstance.getProcessDefinitionId();
            activeActivityIds = processEngine.getRuntimeService().getActiveActivityIds(procInstId);
        }else{
            // 3.2 已经结束的流程实例
            HistoricProcessInstance historicProcessInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery()
                    .processInstanceId(procInstId).singleResult();
            processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            // 3.3 获取结束节点列表
            List<HistoricActivityInstance> historicEnds = processEngine.getHistoryService().createHistoricActivityInstanceQuery()
                    .processInstanceId(procInstId).activityType(BpmnXMLConstants.ELEMENT_EVENT_END).list();
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
