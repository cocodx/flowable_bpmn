package io.github.cocodx.test1;

import org.flowable.engine.*;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;

import java.util.*;

/**
 * @author amazfit
 * @date 2022-08-25 上午12:45
 **/
public class HolidayRequest {

    public static void main(String[] args) {
        ProcessEngineConfiguration configuration = new StandaloneProcessEngineConfiguration();
        configuration.setJdbcUrl("jdbc:mysql://localhost:3306/db_flowable?characterEncoding=utf-8&useSSL=false&serverTimeZone=GMT%2B8")
                .setJdbcDriver("com.mysql.cj.jdbc.Driver")
                .setJdbcUsername("root")
                .setJdbcPassword("password")
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        ProcessEngine processEngine = configuration.buildProcessEngine();

        RepositoryService repositoryService = processEngine.getRepositoryService();
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("holiday-request.bpmn20.xml").deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
        System.out.println("Found process definition" + processDefinition.getName());

        //启动流程实例
        Scanner scanner = new Scanner(System.in);

        System.out.println("who are you？");
        String employee = scanner.nextLine();

        System.out.println("How many holidays do you want to request?");
        Integer nrOfHolidays = Integer.valueOf(scanner.nextLine());

        System.out.println("why do you need them?");
        String description = scanner.nextLine();

        //使用RuntimeService启动一个流程实例
        RuntimeService runtimeService = processEngine.getRuntimeService();
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("employee",employee);
        variables.put("nrOfHolidays",nrOfHolidays);
        variables.put("description",description);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("holiday-request", variables);
        //这时候是已经启动了一个用户任务，用户是一个等待状态（wait state）


        //获得实际的任务列表,配置查询只返回“manager”组的任务
        TaskService taskService = processEngine.getTaskService();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
        System.out.println("You have "+tasks.size()+" tasks:"   );
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println((i+1)+") "+ tasks.get(i).getName());
        }

        //使用任务id获取特定流程实例的变量，并在屏幕中显示实际的申请
        System.out.println("which task would you like to complete？");
        Integer taskIndex = Integer.valueOf(scanner.nextLine());
        Task task = tasks.get(taskIndex-1);
        Map<String, Object> processVariables = taskService.getVariables(task.getId());
        System.out.println(processVariables.get("employee")+" wants "+processVariables.get("nrOfHolidays") + " of holidays. Do you approve this?");

        //批准这个任务
        boolean approved = scanner.nextLine().toLowerCase().equals("y");
        HashMap<String, Object> variables1 = new HashMap<>();
        variables1.put("approved",approved);
        taskService.complete(task.getId(),variables1);


    }
}
