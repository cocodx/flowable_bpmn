package io.github.cocodx.test1;

import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;

import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

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


    }
}
