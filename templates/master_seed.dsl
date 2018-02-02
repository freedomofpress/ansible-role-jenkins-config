def seedfile = new File('{{ jenkins_deploy_seed_user }}')


job('master_seed_job') {
  description ('Job that defines all other jobs. This is controlled via ansible. Edits made directly will be lost on reboot')
  logRotator { numToKeep(10) }
  triggers { cron('@hourly') }
  steps {
    dsl {
      text(seedfile.text)
      removeAction('{{ jenkins_deploy_seed_removeaction }}')
      ignoreExisting({{ jenkins_deploy_seed_ignore|bool|lower }})
      removeViewAction('{{ jenkins_deploy_seed_viewaction }}')
    }
  }
}

