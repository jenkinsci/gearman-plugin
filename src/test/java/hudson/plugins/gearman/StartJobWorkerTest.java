package hudson.plugins.gearman;

import com.google.gson.Gson;
import hudson.model.Computer;
import hudson.model.FreeStyleProject;
import org.gearman.client.GearmanJobResult;
import org.gearman.common.GearmanJobServerSession;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link StartJobWorker}
 */
public class StartJobWorkerTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    public StartJobWorker createWorker(GearmanProject gearmanProject) throws IOException, InterruptedException {
        // mock worker
        MyGearmanWorkerImpl myGearmanWorker = mock(MyGearmanWorkerImpl.class);

        // mock session to server
        GearmanJobServerSession listener = mock(GearmanJobServerSession.class);
        doNothing().when(listener).handleGearmanIOEvent(any());

        // setup available computers
        Computer masterNode = j.jenkins.getComputer("");
        GearmanProxy.getInstance().createExecutorWorkersOnNode(masterNode);

        // setup worker
        StartJobWorker startJobWorker = new StartJobWorker(
                gearmanProject, // zuul project
                masterNode, // computer - master node
                masterNode.getHostName(),
                myGearmanWorker
        );
        startJobWorker.registerEventListener(listener);
        return startJobWorker;
    }

    @Test
    public void testJobStart_FreeStyleProject() throws IOException, InterruptedException {
        // setup project
        FreeStyleProject project = j.createFreeStyleProject("workflow-project");
        GearmanProject gearmanProject = GearmanProject.projectFactory(project);

        // payload
        Map<String, String> data = new HashMap<>();
        data.put("param1", "blue");
        data.put("param2", "red");
        data.put("param3", "yellow");
        Gson gson = new Gson();

        StartJobWorker startJobWorker = createWorker(gearmanProject);
        startJobWorker.setUniqueId(UUID.randomUUID().toString().getBytes("UTF-8"));
        startJobWorker.setJobHandle("build:workflow-project".getBytes("UTF-8"));
        startJobWorker.setData(gson.toJson(data).getBytes("UTF-8"));

        // run
        assertTrue(project.getBuilds().isEmpty());

        GearmanJobResult jobResult = startJobWorker.executeFunction();
        assertTrue(jobResult.jobSucceeded());
        assertFalse(project.getBuilds().isEmpty());
        assertEquals(1, project.getBuilds().size());
    }

    @Test
    public void testJobStart_WorkflowJob() throws IOException, InterruptedException {
        // setup project
        WorkflowJob project = j.createProject(WorkflowJob.class, "workflow-project");
        CpsFlowDefinition flowDefinition = new CpsFlowDefinition("echo 'this is a test'", false);
        project.setDefinition(flowDefinition);

        GearmanProject gearmanProject = GearmanProject.projectFactory(project);

        // payload
        Map<String, String> data = new HashMap<>();
        data.put("param1", "blue");
        data.put("param2", "red");
        data.put("param3", "yellow");
        Gson gson = new Gson();

        StartJobWorker startJobWorker = createWorker(gearmanProject);
        startJobWorker.setUniqueId(UUID.randomUUID().toString().getBytes("UTF-8"));
        startJobWorker.setJobHandle("build:freestyle-project".getBytes("UTF-8"));
        startJobWorker.setData(gson.toJson(data).getBytes("UTF-8"));

        // run
        assertTrue(project.getBuilds().isEmpty());

        GearmanJobResult jobResult = startJobWorker.executeFunction();
        assertTrue(jobResult.jobSucceeded());
        assertFalse(project.getBuilds().isEmpty());
        assertEquals(1, project.getBuilds().size());
    }
}
