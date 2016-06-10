/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.extension.mail.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

public class ProcessIntegrationTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  @Deployment(resources = "processes/pizzaOrderProcess.bpmn")
  @Test
  public void test() throws Exception {
    RuntimeService runtimeService = engineRule.getRuntimeService();
    TaskService taskService = engineRule.getTaskService();

    runtimeService.startProcessInstanceByKey("pizzaOrderProcess");

    waitForAsyncJobs();

    List<Task> tasks = taskService.createTaskQuery().taskName("make the pizza").list();
    assertThat(tasks).isNotEmpty();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    tasks = taskService.createTaskQuery().taskName("deliver the pizza").list();
    assertThat(tasks).isNotEmpty();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    waitForAsyncJobs();

    assertThat(runtimeService.createProcessInstanceQuery().list()).isEmpty();
  }

  private void waitForAsyncJobs() throws InterruptedException {
    JobQuery jobQuery = engineRule.getManagementService().createJobQuery().executable();

    while(jobQuery.count() > 0) {
      Thread.sleep(500);
    }
  }

}
