/*
 *
 * Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package hudson.plugins.gearman;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.queue.QueueTaskFuture;
import hudson.slaves.DumbSlave;
import jenkins.model.Jenkins;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

import static org.junit.Assert.assertEquals;

/**
 * Test for the {@link GearmanPluginUtil} class.
 *
 * @author Khai Do
 */
public class GearmanPluginUtilTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testGetRealNameSlave() throws Exception {
        DumbSlave slave = j.createOnlineSlave();

        // createOnlineSlave sets the slave name to slave0. Do not change
        // this with setNodeName as the name is supposed to be immutable
        // except when cloning a preexisting slave.
        assertEquals("slave0", GearmanPluginUtil.getRealName(slave.toComputer()));
        j.getInstance().removeNode(slave);
    }

    @Test
    public void testGetRealNameBuiltIn() throws Exception {

        assertEquals("built-in", GearmanPluginUtil.getRealName(Jenkins.getInstance().getComputer("")));
    }

    @Test
    public void testFindJob_FreeStyleProject() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject("freestyle-project");
        QueueTaskFuture<FreeStyleBuild> fBuild = project.scheduleBuild2(0);
        FreeStyleBuild build1 = fBuild.get();
        assertEquals(build1, GearmanPluginUtil.findBuild("freestyle-project", build1.number));
    }

    @Test
    public void testFindJob_WorkflowJob() throws Exception {
        WorkflowJob project = j.createProject(WorkflowJob.class, "workflow-job");
        QueueTaskFuture<WorkflowRun> fBuild = project.scheduleBuild2(0);
        WorkflowRun build1 = fBuild.get();
        assertEquals(build1, GearmanPluginUtil.findBuild("workflow-job", build1.number));
    }

    @Test
    public void testFindJob_InFolder() throws Exception {
        MockFolder folder = j.createFolder("test-folder");
        FreeStyleProject projectInFolder = folder.createProject(FreeStyleProject.class, "freestyle-project-in-folder");
        QueueTaskFuture<FreeStyleBuild> fBuild2 = projectInFolder.scheduleBuild2(0);
        FreeStyleBuild build2 = fBuild2.get();
        assertEquals(build2, GearmanPluginUtil.findBuild("freestyle-project-in-folder", build2.number));
    }

}
