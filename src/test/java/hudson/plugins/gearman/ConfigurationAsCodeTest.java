package hudson.plugins.gearman;

import io.jenkins.plugins.casc.ConfigurationContext;
import io.jenkins.plugins.casc.ConfiguratorRegistry;
import io.jenkins.plugins.casc.misc.ConfiguredWithCode;
import io.jenkins.plugins.casc.misc.JenkinsConfiguredWithCodeRule;
import io.jenkins.plugins.casc.model.CNode;
import org.junit.ClassRule;
import org.junit.Test;

import static io.jenkins.plugins.casc.misc.Util.getUnclassifiedRoot;
import static io.jenkins.plugins.casc.misc.Util.toStringFromYamlFile;
import static io.jenkins.plugins.casc.misc.Util.toYamlString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ConfigurationAsCodeTest {

    @ClassRule
    @ConfiguredWithCode("configuration-as-code.yml")
    public static JenkinsConfiguredWithCodeRule rule = new JenkinsConfiguredWithCodeRule();

    @Test
    public void shouldSupportConfigurationAsCode() throws Exception {
        GearmanPluginConfig gpc = rule.jenkins.getDescriptorByType(GearmanPluginConfig.class);;

        assertThat(gpc.getHost(), is("myhost.example"));
        assertThat(gpc.getPort(), is(12345));
        assertThat(gpc.isEnablePlugin(), is(true));
    }

    @Test
    public void shouldSupportConfigurationAsCodeExport() throws Exception {
        ConfiguratorRegistry registry = ConfiguratorRegistry.get();
        ConfigurationContext context = new ConfigurationContext(registry);
        CNode gearmanAttribute = getUnclassifiedRoot(context).get("gearmanPluginConfig");

        String exported = toYamlString(gearmanAttribute);
        String expected = toStringFromYamlFile(this, "configuration-as-code-expected.yml");

        assertThat(exported, is(expected));
    }
}
