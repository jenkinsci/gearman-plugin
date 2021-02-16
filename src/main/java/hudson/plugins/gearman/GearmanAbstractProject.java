package hudson.plugins.gearman;

import com.google.common.collect.Maps;
import hudson.model.*;
import hudson.model.labels.LabelAtom;
import hudson.model.queue.QueueTaskFuture;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GearmanAbstractProject extends GearmanProject<AbstractProject, AbstractBuild> {

    public GearmanAbstractProject(AbstractProject project) {
        super(project);
    }

    @Override
    public QueueTaskFuture scheduleBuild2(int quietPeriod, Cause c, Action... actions) {
        return getJob().scheduleBuild2(quietPeriod, c, actions);
    }

    @Override
    public boolean isDisabled() {
        return getJob().isDisabled();
    }

    @Override
    public Label getAssignedLabel() {
        return getJob().getAssignedLabel();
    }

    @Override
    protected Map getBuildData(AbstractBuild run) {
        Map<String, Object> buildData = Maps.newHashMap();
        Node node = run.getBuiltOn();
        if (node != null) {
            buildData.put("node_name", node.getNodeName());
            buildData.put("node_labels", node.getAssignedLabels().stream()
                    .map( (LabelAtom labelAtom ) -> labelAtom.getDisplayName() )
                    .collect(Collectors.toList())
            );
        }
        return buildData;
    }
}
