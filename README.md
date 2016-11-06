jenkins_deploy
=========

Some post jenkins deployment tweaks to take advantage.

Requirements
------------

* ansible > 2.1
* molecule


Local testing:
```bash
$ ansible-galaxy install -r requirements.yml
$ molecule test
```

Role Variables
--------------

```yaml
jenkins_deploy_css_url: https://jenkins-contrib-themes.github.io/jenkins-material-theme/dist/material-blue.css
jenkins_deploy_js_url: ""
```

Dependencies
------------

A list of other roles hosted on Galaxy should go here, plus any details in regards to parameters that may need to be set for other roles, or variables that are used from other roles.

Example Playbook
----------------

Check out `./playbook.yml`

License
-------

MIT
