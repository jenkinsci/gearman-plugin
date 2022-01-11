/*
 * Copyright 2021
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

import hudson.model.Job;
import jenkins.model.Jenkins;

import java.util.HashMap;

import org.gearman.worker.GearmanFunctionFactory;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneOffExecutorWorkerThread extends AbstractWorkerThread{

    private static final Logger logger = LoggerFactory
            .getLogger(Constants.PLUGIN_LOGGER_NAME);

    public OneOffExecutorWorkerThread(String host, int port, String name, AvailabilityMonitor availability) {
        super(host, port, name, availability);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void registerJobs() {
        logger.info("HASHAR REGISTER JOBS");
        if (worker == null) {
            // We haven't been initialized yet; the run method will call this again
            return;
        }

        HashMap<String,GearmanFunctionFactory> newFunctionMap = new HashMap<String,GearmanFunctionFactory>();
        for (GearmanProject project : GearmanProject.getAllItems()) {
            if (project.isDisabled()) {
                continue;
            }

            Job<?,?> job = project.getJob();
            if ( job instanceof WorkflowJob ) {
                String jobFunctionName = "build:" + job.getName();
                logger.warn("REGISTERING " + jobFunctionName);
                newFunctionMap.put(
                    jobFunctionName,
                    new CustomGearmanFunctionFactory(
                        jobFunctionName,
                        StartJobWorker.class.getName(),
                        project, Jenkins.get().getComputer(""), "master", worker
                    )
                );
            }

        }

    }
}