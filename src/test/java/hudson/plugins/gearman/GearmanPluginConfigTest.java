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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import jenkins.model.Jenkins;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;

/**
 * Test for the {@link GearmanPluginConfig} class.
 *
 * @author Khai Do
 */
public class GearmanPluginConfigTest {

    private MockedStatic<Jenkins> mockedJenkins;
    private GearmanPluginConfig gpc;

    /**
   */
    @Before
    public void setUp() {
        Jenkins jenkins = mock(Jenkins.class);

        mockedJenkins = mockStatic(Jenkins.class);
        mockedJenkins.when(Jenkins::get).thenReturn(jenkins);
        mockedJenkins.when(Jenkins::getInstance).thenReturn(jenkins);

        gpc = new GearmanPluginConfig();
    }

    @After
    public void tearDown() throws Exception {
        mockedJenkins.close();
    }

    @Test
    public void testDefaultGearmanHost() {
        assertEquals(Constants.GEARMAN_DEFAULT_TCP_HOST, gpc.getHost());
    }

    @Test
    public void testDefaultGearmanPort() {
        assertEquals(Constants.GEARMAN_DEFAULT_TCP_PORT, gpc.getPort());
    }

    @Test
    public void testDefaultLaunchWorker() {
        assertEquals(Constants.GEARMAN_DEFAULT_ENABLE_PLUGIN,
                gpc.isEnablePlugin());
    }
}
