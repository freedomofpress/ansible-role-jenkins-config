import hudson.security.SecurityRealm
import jenkins.model.Jenkins
import hudson.security.AuthorizationStrategy
{% if jenkins_deploy_gh_oauth -%}
import org.jenkinsci.plugins.GithubSecurityRealm
{% endif %}
{% if jenkins_deploy_gh_oauth_strategy -%}
import org.jenkinsci.plugins.GithubAuthorizationStrategy
{% endif %}

def instance = Jenkins.getInstance()

{% if jenkins_deploy_gh_oauth %}
String githubWebUri = '{{ jenkins_deploy_gh_oauth.url }}'
String githubApiUri = '{{ jenkins_deploy_gh_oauth.api_url }}'
String clientID = '{{ jenkins_deploy_gh_oauth.client_id }}'
String clientSecret = '{{ jenkins_deploy_gh_oauth.secret_id }}'
String oauthScopes = '{{ jenkins_deploy_gh_oauth.scope }}'
SecurityRealm github_realm = new GithubSecurityRealm(githubWebUri, githubApiUri, clientID, clientSecret, oauthScopes)

//check for equality, no need to modify the runtime if no settings changed
if(!github_realm.equals(instance.getSecurityRealm())) {
    instance.setSecurityRealm(github_realm)
    instance.save()
}
{% endif %}

{% if jenkins_deploy_gh_oauth_strategy %}
{% set oauth = jenkins_deploy_gh_oauth_strategy %}

//permissions are ordered similar to web UI
//Admin User Names
String adminUserNames = '{{ oauth.admins | join(",") }}'
//Participant in Organization
String organizationNames = '{{ oauth.organizations | join(",") }}'
//Use Github repository permissions
boolean useRepositoryPermissions = true
//Grant READ permissions to all Authenticated Users
boolean authenticatedUserReadPermission = {{ oauth.authread_all | default(false) | lower }}
//Grant CREATE Job permissions to all Authenticated Users
boolean authenticatedUserCreateJobPermission = false
//Grant READ permissions for /github-webhook
boolean allowGithubWebHookPermission = {{ oauth.github_webook | default(false) | lower }}
//Grant READ permissions for /cc.xml
boolean allowCcTrayPermission = false
//Grant READ permissions for Anonymous Users
boolean allowAnonymousReadPermission = false
//Grant ViewStatus permissions for Anonymous Users
boolean allowAnonymousJobStatusPermission = false

AuthorizationStrategy github_authorization = new GithubAuthorizationStrategy(adminUserNames,
    authenticatedUserReadPermission,
    useRepositoryPermissions,
    authenticatedUserCreateJobPermission,
    organizationNames,
    allowGithubWebHookPermission,
    allowCcTrayPermission,
    allowAnonymousReadPermission,
    allowAnonymousJobStatusPermission)

//check for equality, no need to modify the runtime if no settings changed
if(!github_authorization.equals(instance.getAuthorizationStrategy())) {
    instance.setAuthorizationStrategy(github_authorization)
    instance.save()
}
{% endif %}
