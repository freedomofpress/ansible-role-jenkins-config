import jenkins.model.*

def instance = Jenkins.getInstance()
instance.setNumExecutors({{ jenkins_deploy_config_master_exe }})
instance.setNoUsageStatistics({{ jenkins_deploy_config_disableusage_stats | lower() }})

instance.save()
