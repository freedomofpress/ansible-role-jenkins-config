import jenkins.model.*
import jenkins.AgentProtocol
import hudson.security.csrf.DefaultCrumbIssuer

def instance = Jenkins.getInstance()
instance.setNumExecutors({{ jenkins_deploy_config_master_exe }})
instance.setNoUsageStatistics({{ jenkins_deploy_config_disableusage_stats | lower() }})


// Disable deprecated Jenkins node protocols
p = AgentProtocol.all()

disable_plugin_list = ["JNLP-connect", "JNLP2-connect", "JNLP3-connect"]

p.each { x ->
	if(x.name && x.name in disable_plugin_list ) {
      p.remove(x)
    }
}

{% if jenkins_deploy_config_disablecrsf %}
instance.setCrumbIssuer(new DefaultCrumbIssuer(true))
{% endif %}


instance.save()
