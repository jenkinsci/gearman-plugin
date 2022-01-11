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

import hudson.slaves.DumbSlave;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;

/**
 * Test for the {@link ExecutorWorkerThread} class.
 *
 * @author Khai Do
 */
public class GearmanProxyTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();
    GearmanProxy gp;

    @Before
    public void setUp() throws Exception {
        gp = GearmanProxy.getInstance();
    }

    @After
    public void tearDown() throws Exception {
        gp.testResetHandles();
    }

    @Test
    public void testCreateManagementWorker() {

        assertEquals(0, gp.getNumExecutors());

        gp.createManagementWorker();

        // mgmt: 1 built-in
        assertEquals(1, gp.getNumExecutors());
        //        assertTrue(gp.getGmwtHandles().get(0).isAlive());
    }

    @Test
    public void testCreateExecutorWorkersOnNode() throws Exception {

        DumbSlave slave = j.createSlave();

        assertEquals(0, gp.getNumExecutors());

        gp.createExecutorWorkersOnNode(slave.toComputer());

        // exec: 1 built-in
        assertEquals(1, gp.getNumExecutors());
    }

    @Test
    public void testInitWorkers() {

        gp.initWorkers();

        // exec: 1 slave, 1 built-in + mgmnt: 1
        assertEquals(3, gp.getNumExecutors());
    }

    @Test
    public void testInitWorkers2() throws Exception {

        DumbSlave slave = j.createSlave();
        gp.initWorkers();

        // exec: 2 slaves, 1 built-in + mgmnt: 1
        assertEquals(4, gp.getNumExecutors());
    }
}
