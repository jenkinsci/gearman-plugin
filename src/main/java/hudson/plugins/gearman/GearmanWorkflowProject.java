package hudson.plugins.gearman;

import com.google.common.collect.Lists;
import hudson.model.*;
import hudson.model.queue.QueueTaskFuture;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GearmanWorkflowProject extends GearmanProject<WorkflowJob, WorkflowRun> {

    public GearmanWorkflowProject(WorkflowJob project) {
        super(project);
    }

    @Override
    public QueueTaskFuture<WorkflowRun> scheduleBuild2(int quietPeriod, Cause c, Action... actions) {
        List<Action> actionsList = Lists.newArrayList(actions);
        if (c != null) {
            actionsList.add(new CauseAction(c));
        }
        return  getJob().scheduleBuild2(quietPeriod, actionsList.toArray(new Action[actionsList.size()]));
    }

    @Override
    public boolean isDisabled() {
        return getJob().isDisabled();
    }

    @Override
    public Label getAssignedLabel() {
        return getJob().getAssignedLabel();
    }
}
