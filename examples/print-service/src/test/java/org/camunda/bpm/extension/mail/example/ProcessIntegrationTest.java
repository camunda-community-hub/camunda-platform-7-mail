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

import java.io.IOException;
import java.util.List;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ProcessIntegrationTest {

  @Rule public ProcessEngineRule engineRule = new ProcessEngineRule();

  private PrintServiceProcessApplication processApplication;

  private RuntimeService runtimeService;
  private TaskService taskService;

  @Before
  public void init() throws IOException {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();

    processApplication = new PrintServiceProcessApplication();
  }

  @Deployment(resources = "processes/printProcess.bpmn")
  @Test
  public void processPrintMail() throws Exception {

    processApplication.startService(engineRule.getProcessEngine());

    TaskQuery taskQuery = taskService.createTaskQuery().taskName("print it");

    // wait for first mail
    while (taskQuery.count() == 0) {
      Thread.sleep(500);
    }

    List<Task> tasks = taskQuery.list();
    assertThat(tasks).isNotEmpty();

    for (Task task : tasks) {
      taskService.complete(task.getId());
    }

    waitForAsyncJobs();

    assertThat(runtimeService.createProcessInstanceQuery().list()).isEmpty();
  }

  private void waitForAsyncJobs() throws InterruptedException {
    JobQuery jobQuery = engineRule.getManagementService().createJobQuery().executable();

    while (jobQuery.count() > 0) {
      Thread.sleep(500);
    }
  }

  @After
  public void cleanup() throws Exception {

    processApplication.stopService();
  }
}
