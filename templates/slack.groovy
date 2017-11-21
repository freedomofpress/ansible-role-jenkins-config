import jenkins.model.*
import jenkins.plugins.slack.*

jenkins = Jenkins.getInstance()

slack_cfg = jenkins.getDescriptorByType(SlackNotifier.DescriptorImpl)

slack_cfg.teamDomain = "{{ jenkins_slack_settings.domain }}"
slack_cfg.tokenCredentialId = "{{ jenkins_slack_settings.cred_id}}"
slack_cfg.room = "{{ jenkins_slack_settings.room }}"
slack_cfg.baseUrl = "{{jenkins_slack_settings.base_url|default('https://hooks.slack.com/services/')}}"
slack_cfg.sendAs = "{{ jenkins_slack_settings.send_as|default('jenkins') }}"
slack_cfg.save()
