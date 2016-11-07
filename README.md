jenkins_deploy
==============

Role focuses on post installation, configuration of jenkins.

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
## Modify below entries to customize UI
jenkins_deploy_css_url: https://jenkins-contrib-themes.github.io/jenkins-material-theme/dist/material-blue.css
jenkins_deploy_js_url: ""

# Drop a groovy script in templates and then activate it here
jenkins_deploy_groovy: []

# These github auth settings wont be utilized unless
#   the groovy scripts github_auth and github_auth_strategy are deployed
#   ( the data below is bogus. Use your own ids here)
jenkins_deploy_gh_oauth:
  url: https://github.com
  api_url: https://api.github.com
  client_id: 12314124412
  secret_id: 23424324324
  scope: "read:org"

jenkins_deploy_gh_oauth_strategy:
  admins:
    - msheiny
  organizations:
    - organization
  github_webhook: true
  authread_all: true

# Nginx recomended settings
jenkins_nginx_usercontent: |
  location /userContent {
    root {{ jenkins_home }}/;
        if (!-f $request_filename){
           rewrite (.*) /$1 last;
       break;
        }
    sendfile on;
  }

jenkins_nginx_root: |
  location / {
    sendfile off;
    proxy_pass         http://127.0.0.1:{{ jenkins_http_port }};
    proxy_redirect     default;

    proxy_set_header   Host              $host;
    proxy_set_header   X-Real-IP         $remote_addr;
    proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
    proxy_set_header   X-Forwarded-Proto https;
    proxy_max_temp_file_size 0;
    proxy_http_version 1.0;

    #this is the maximum upload size
    client_max_body_size       10m;
    client_body_buffer_size    128k;

    proxy_connect_timeout      90;
    proxy_send_timeout         90;
    proxy_read_timeout         90;

    proxy_buffer_size          4k;
    proxy_buffers              4 32k;
    proxy_busy_buffers_size    64k;
    proxy_temp_file_write_size 64k;
  }
```

Dependencies
------------

* `geerlingguy.jenkins` - Used for installing jenkins, java, and plugins
* `jdauphant.nginx` - Used for installing and configuring nginx reverse proxy

Example Playbook
----------------

Check out `./playbook.yml`

License
-------

MIT
