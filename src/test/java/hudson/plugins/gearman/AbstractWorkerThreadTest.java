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

import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.gearman.common.GearmanNIOJobServerConnection;
import org.gearman.worker.GearmanWorker;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test for the {@link AbstractWorkerThread} class.
 *
 * @author Khai Do
 */
public class AbstractWorkerThreadTest {

    /**
   */
    @Before
    public void setUp() {
        GearmanWorker gearmanWorker = mock(GearmanWorker.class);
        GearmanNIOJobServerConnection conn = new GearmanNIOJobServerConnection("localhost", 4730);
        doNothing().when(gearmanWorker).work();
        when(gearmanWorker.addServer(conn)).thenReturn(true);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testNamedThread() {
        AbstractWorkerThread fakeWorker = new FakeWorkerThread("GearmanServer", 4730, "faker", null);
        assertEquals("faker", fakeWorker.getName());
    }

    @Ignore("Flaky test")
    @Test()
    public void testStartStopThread() {
        AbstractWorkerThread fakeWorker = new FakeWorkerThread("GearmanServer", 4730, "faker", null);
        fakeWorker.start();
        assertTrue(fakeWorker.isAlive());
        fakeWorker.stop();
        assertFalse(fakeWorker.isAlive());
    }
}
