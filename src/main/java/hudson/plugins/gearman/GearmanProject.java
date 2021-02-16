package hudson.plugins.gearman;

import hudson.model.*;
import hudson.model.queue.QueueTaskFuture;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class acts as facade for manipulation with Jenkins items in Gearman plugin. It allows to use
 * one common object no matter whether the underlaying Jenkins project is {@link AbstractProject} or
 * {@link WorkflowJob}, or something not yet supported. All {@link Run} related methods are accessed by
 * {@link GearmanProject#getJob()}, all differentiated methods are implemented specifically by this object.
 *
 * It's kind of a split brain, but it is caused by the jenkins creators them self, for details
 * see https://jenkins.io/doc/developer/plugin-development/pipeline-integration/.
 */
public abstract class GearmanProject<JobT extends Job, RunT extends Run> {

    /**
     * Is job supported by Gearman plugin.
     *
     * @param object to test
     * @return true if it's supported otherwise false
     */
    static boolean isSupported(Object object){
        return object instanceof WorkflowJob || object instanceof AbstractProject;
    }

    /**
     * Returns GearmanProject implementation for given Job.
     *
     * @param job item
     * @return GearmanProject implementation or throws exception if jonb is not supported
     */
    static GearmanProject projectFactory(Job<?, ?> job){
        if(job instanceof WorkflowJob){
            return new GearmanWorkflowProject((WorkflowJob) job);
        }else if(job instanceof AbstractProject){
            return new GearmanAbstractProject((AbstractProject) job);
        }else {
            throw new IllegalArgumentException(job + "is not supported!");
        }
    }

    /**
     * Based on supported jobs return all items supported by Gearman plugin
     *
     * @return list of all supported items
     */
    static List<GearmanProject> getAllItems(){
        return Jenkins.getInstance().getAllItems(Job.class)
                .stream()
                .filter( (Job job) -> isSupported(job))
                .map( (Job job) -> projectFactory(job) )
                .collect(Collectors.toList());
    }

    private JobT project;

    public GearmanProject(JobT project) {
        this.project = project;
    }

    /**
     * Flag whether the project is disabled
     * @return
     */
    abstract boolean isDisabled();

    /**
     * Label assigned for the project
     * @return
     */
    abstract Label getAssignedLabel();

    /**
     * Schedule build of the project
     * @param quietPeriod
     * @param c
     * @param actions
     * @return
     */
    abstract QueueTaskFuture scheduleBuild2(int quietPeriod, Cause c, Action... actions);

    /**
     * Get build data specific for this type of project. Common build data are: worker, manager, number, name,
     * url, result.
     *
     * @param run object of the JobT
     * @return specific build data map.
     */
    protected Map getBuildData(RunT run){
        return null;
    }


    /**
     * Get the project itself
     * @return encapsulated project
     */
    public JobT getJob(){
        return project;
    }
}
