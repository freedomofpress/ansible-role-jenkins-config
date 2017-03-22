// Adapted from https://github.com/beedemo/pse-cje-sa/quickstart/init_22_github_org_project.groovy
import hudson.model.*;
import jenkins.model.*;

import hudson.security.ACL;
import jenkins.branch.OrganizationFolder;
import jenkins.scm.api.SCMSourceOwner;
import jenkins.scm.api.SCMSourceOwners;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMNavigator;

import java.util.logging.Logger

Logger logger = Logger.getLogger("guthub_folder.groovy")

def j = Jenkins.instance

File disableScript = new File(j.rootDir, ".disable-init_gh_folder")
if (disableScript.exists()) {
    logger.info("DISABLED github folder script")
    return
}

{% for gh_org in jenkins_deploy_gh_folder %}
    println "--> creating {{ gh_org.job_name }}"
    def jobConfigXml = """
	<jenkins.branch.OrganizationFolder plugin="branch-api@2.0.6">
	  <actions/>
	  <description></description>
	  <properties>
		<com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider_-FolderCredentialsProperty plugin="cloudbees-folder@5.17">
		  <domainCredentialsMap class="hudson.util.CopyOnWriteMap\$Hash">
			<entry>
			  <com.cloudbees.plugins.credentials.domains.Domain plugin="credentials@2.1.11">
				<specifications/>
			  </com.cloudbees.plugins.credentials.domains.Domain>
			  <java.util.concurrent.CopyOnWriteArrayList/>
			</entry>
		  </domainCredentialsMap>
		</com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider_-FolderCredentialsProperty>
		<org.jenkinsci.plugins.pipeline.modeldefinition.config.FolderConfig plugin="pipeline-model-definition@1.0.2">
		  <dockerLabel></dockerLabel>
		  <registry plugin="docker-commons@1.6"/>
		</org.jenkinsci.plugins.pipeline.modeldefinition.config.FolderConfig>
		<jenkins.branch.NoTriggerOrganizationFolderProperty>
		  <branches>{{ gh_org.no_branches | default(".*") }}</branches>
		</jenkins.branch.NoTriggerOrganizationFolderProperty>
	  </properties>
	  <folderViews class="jenkins.branch.OrganizationFolderViewHolder">
		<owner reference="../.."/>
	  </folderViews>
	  <healthMetrics>
		<com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric plugin="cloudbees-folder@5.17">
		  <nonRecursive>{{ gh_org.health_recursive }}</nonRecursive>
		</com.cloudbees.hudson.plugins.folder.health.WorstChildHealthMetric>
	  </healthMetrics>
	  <icon class="jenkins.branch.MetadataActionFolderIcon">
		<owner class="jenkins.branch.OrganizationFolder" reference="../.."/>
	  </icon>
	  <orphanedItemStrategy class="com.cloudbees.hudson.plugins.folder.computed.DefaultOrphanedItemStrategy" plugin="cloudbees-folder@5.17">
		<pruneDeadBranches>{{ gh_org.prune_dead_branch | default(true) }}</pruneDeadBranches>
		<daysToKeep>{{ gh_org.prune_days | default(0) }}</daysToKeep>
		<numToKeep>{{ gh_org.prune_number | default(0) }}</numToKeep>
	  </orphanedItemStrategy>
	  <triggers>
		<com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger plugin="cloudbees-folder@5.17">
		  <spec>H H * * *</spec>
		  <interval>86400000</interval>
		</com.cloudbees.hudson.plugins.folder.computed.PeriodicFolderTrigger>
	  </triggers>
	  <navigators>
		<org.jenkinsci.plugins.github__branch__source.GitHubSCMNavigator plugin="github-branch-source@2.0.3">
		  <repoOwner>{{ gh_org.owner }}</repoOwner>
		  <scanCredentialsId>{{ gh_org.scan_cred_id }}</scanCredentialsId>
		  <checkoutCredentialsId>{{ gh_org.checkout_cred_id | default("SAME") }}</checkoutCredentialsId>
		  <pattern>{{ gh_org.scan_pattern | default(".*") }}</pattern>
		  <buildOriginBranch>{{ gh_org.origin_branch }}</buildOriginBranch>
		  <buildOriginBranchWithPR>{{ gh_org.origin_branch_pr }}</buildOriginBranchWithPR>
		  <buildOriginPRMerge>{{ gh_org.origin_pr_merge | default(false) }}</buildOriginPRMerge>
		  <buildOriginPRHead>{{ gh_org.origin_pr_head | default(false) }}</buildOriginPRHead>
		  <buildForkPRMerge>{{ gh_org.fork_pr_merge | default(false) }}</buildForkPRMerge>
		  <buildForkPRHead>{{ gh_org.fork_pr_head | default(false) }}</buildForkPRHead>
		</org.jenkinsci.plugins.github__branch__source.GitHubSCMNavigator>
	  </navigators>
	  <projectFactories>
		<org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProjectFactory plugin="workflow-multibranch@2.12"/>
	  </projectFactories>
	</jenkins.branch.OrganizationFolder>
    """

    job = j.createProjectFromXML("{{ gh_org.job_name }}", new ByteArrayInputStream(jobConfigXml.getBytes("UTF-8")));
    job.save()
    //the following will actually kickoff the initial scanning
    ACL.impersonate(ACL.SYSTEM, new Runnable() {
                    @Override public void run() {
                        for (final SCMSourceOwner owner : SCMSourceOwners.all()) {
                            if (owner instanceof OrganizationFolder) {
                                OrganizationFolder orgFolder = (OrganizationFolder) owner;
                                for (GitHubSCMNavigator navigator : orgFolder.getNavigators().getAll(GitHubSCMNavigator.class)) {
                                    orgFolder.scheduleBuild();
                                }
                            }
                        }
                    }
                });
    logger.info("created {{ gh_org.job_name }}")
{% endfor %}
 //create marker file to disable scripts from running twice
 disableScript.createNewFile()
