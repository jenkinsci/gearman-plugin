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

import hudson.model.Computer;
import hudson.model.Label;
import hudson.model.Node;
import hudson.model.Node.Mode;
import hudson.model.labels.LabelAtom;
import org.gearman.worker.GearmanFunctionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/*
 * This is the thread to run gearman executors
 * Executors are used to initiate jenkins builds
 *
 * @author Khai Do
 *
 */
public class ExecutorWorkerThread extends AbstractWorkerThread{

    private static final Logger logger = LoggerFactory
            .getLogger(Constants.PLUGIN_LOGGER_NAME);

    private final Computer computer;
    private final String builtInName;

    private HashMap<String,GearmanFunctionFactory> functionMap;

    // constructor
    public ExecutorWorkerThread(String host, int port, String name,
                                Computer computer, String builtInName,
                                AvailabilityMonitor availability) {
        super(host, port, name, availability);
        this.computer = computer;
        this.builtInName = builtInName;
    }

    @Override
    protected void initWorker() {
        availability.unlock(worker);
        super.initWorker();
        this.functionMap = new HashMap<String,GearmanFunctionFactory>();
    }

    /**
     * Register gearman functions on this computer.  This will unregister all
     * functions before registering new functions.  Works for free-style
     * and maven projects but does not work for multi-config projects
     *
     * How functions are registered:
     *  - If the project has no label then we register the project with all
     *      computers
     *
     *      build:pep8 on precise-123
     *      build:pep8 on oneiric-456
     *
     *  - If the project contains one label then we register with the computer
     *      that contains the corresponding label. Labels with '&amp;&amp;' is
     *      considered just one label
     *
     *      build:pep8:precise on precise-123
     *      build:pep8 on precise-123
     *      build:pep8:precise on precise-129
     *      build:pep8 on precise-129
     *
     *  - If the project contains multiple labels separated by '||' then
     *      we register with the computers that contain the corresponding labels
     *
     *      build:pep8:precise on precise-123
     *      build:pep8 on precise-123
     *      build:pep8:precise on precise-129
     *      build:pep8 on precise-129
     *      build:pep8:oneiric on oneiric-456
     *      build:pep8 on oneiric-456
     *      build:pep8:oneiric on oneiric-459
     *      build:pep8 on oneiric-459
     *
     */
    @Override
    public void registerJobs() {
        if (worker == null || functionMap == null) {
            // We haven't been initialized yet; the run method will call this again
            return;
        }

        HashMap<String,GearmanFunctionFactory> newFunctionMap = new HashMap<String,GearmanFunctionFactory>();

        if (!computer.isOffline()) {
            Node node = computer.getNode();

            for (GearmanProject project : GearmanProject.getAllItems()) {

                if (project.isDisabled()) { // ignore all disabled projects
                    continue;
                }

                String projectName = project.getJob().getName();
                Label label = project.getAssignedLabel();

                if (label == null) { // project has no label -> so register
                                     // "build:projectName" on all non exclusive nodes
                    if (node.getMode() != Mode.EXCLUSIVE) {
                        String jobFunctionName = "build:" + projectName;
                        newFunctionMap.put(jobFunctionName, new CustomGearmanFunctionFactory(
                            jobFunctionName, StartJobWorker.class.getName(),
                            project, computer, this.builtInName, worker));
                    }
                } else { // register "build:$projectName:$label" if this
                         // node matches a node from the project label

                    Set<Node> projectLabelNodes = label.getNodes();
                    Set<LabelAtom> projectLabelAtoms = label.listAtoms();
                    Set<LabelAtom> nodeLabelAtoms = node.getAssignedLabels();
                    // Get the intersection of label atoms for the project and the current node
                    Set<LabelAtom> nodeProjectLabelAtoms = new HashSet<LabelAtom>(projectLabelAtoms);
                    nodeProjectLabelAtoms.retainAll(nodeLabelAtoms);

                    // Register functions iff the current node is in
                    // the list of nodes for the project's label
                    if (projectLabelNodes.contains(node)) {
                        String jobFunctionName = "build:" + projectName;
                        // register without label (i.e. "build:$projectName")
                        newFunctionMap.put(jobFunctionName, new CustomGearmanFunctionFactory(
                                jobFunctionName, StartJobWorker.class.getName(),
                                project, computer, this.builtInName, worker));
                        // iterate over the intersection of project and node labels
                        for (LabelAtom labelAtom : nodeProjectLabelAtoms) {
                            jobFunctionName = "build:" + projectName
                                + ":" + labelAtom.getDisplayName();
                            // register with label (i.e. "build:$projectName:$label")
                            newFunctionMap.put(jobFunctionName, new CustomGearmanFunctionFactory(
                                    jobFunctionName, StartJobWorker.class.getName(),
                                    project, computer, this.builtInName, worker));
                        }
                    }
                }
            }
        }
        logger.debug("---- Worker "+ getName() +" registerJobs try to register " + newFunctionMap.size() + " function(s)");
        if (!newFunctionMap.keySet().equals(functionMap.keySet())) {
            functionMap = newFunctionMap;
            Set<GearmanFunctionFactory> functionSet = new HashSet<GearmanFunctionFactory>(functionMap.values());
            updateJobs(functionSet);
        } else {
            logger.debug("---- Worker "+ getName() +" registerJobs no changes");
        }
    }

    public synchronized Computer getComputer() {
        return computer;
    }
}
