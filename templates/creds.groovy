import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import hudson.plugins.sshslaves.*
import hudson.util.Secret
import java.io.FileInputStream
import org.apache.commons.fileupload.FileItem
{% if jenkins_deploy_gce_keys -%}
import com.google.jenkins.plugins.credentials.oauth.JsonServiceAccountConfig
import com.google.jenkins.plugins.credentials.oauth.GoogleRobotPrivateKeyCredentials
{% endif %}
{% if jenkins_deploy_aws_keys -%}
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl
{% endif %}

domain = Domain.global()
store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

{% for key in jenkins_deploy_ssh_files %}
// SSH KEYS
privateKey = new BasicSSHUserPrivateKey(
    CredentialsScope.GLOBAL,
    "{{ key.keyname }}",
    "{{ key.username }}",
    new BasicSSHUserPrivateKey.FileOnMasterPrivateKeySource("{{jenkins_home}}/.ssh/{{key.keyname}}"),
    "{{ key.passphrase | default('') }}",
    "{{ key.description | default('') }}"
)
store.addCredentials(domain, privateKey)
{% endfor %}

{% for key in jenkins_deploy_aws_keys %}
// AWS KEYS
aws_cred = new AWSCredentialsImpl(
    CredentialsScope.GLOBAL,
    "{{ key.id }}",
    "{{ key.key }}",
    "{{ key.secret }}",
    "{{ key.desc }}",
    "",
    ""
)
store.addCredentials(domain, aws_cred)
{% endfor %}

{% for key in jenkins_deploy_userpass_creds %}
// USERNAME + PASSWORD CREDS
userandpass = new UsernamePasswordCredentialsImpl(
    CredentialsScope.GLOBAL,
    "{{ key.id }}",
    "{{ key.description | default('') }}",
    "{{ key.username | default('') }}",
    "{{ key.password }}"
)
store.addCredentials(domain, userandpass)
{% endfor %}

{% for key in jenkins_deploy_secret_creds %}
// Secret Text
secretText = new StringCredentialsImpl(
    CredentialsScope.GLOBAL,
    "{{ key.id }}",
    "{{ key.desc }}",
    Secret.fromString("{{ key.secret }}"))

store.addCredentials(domain, secretText)
{% endfor %}

{% for key in jenkins_deploy_gce_keys -%}
// Google JSON store

// file gets copied to the jenkins system out of band
def filey = new FileInputStream('{{ jenkins_home }}/.gce/{{ key.project_id }}.json')

// pretending to be a FileItem type and feeding in the appropriate methods :shhhh:
fileItem = [ getSize: { return 1L }, getInputStream: { return filey } ] as FileItem

def ServiceAccount = new JsonServiceAccountConfig(fileItem, null)

def GoogleAccount = new GoogleRobotPrivateKeyCredentials("{{ key.project_id }}", ServiceAccount, null)

store.addCredentials(domain, GoogleAccount)
{% endfor %}
