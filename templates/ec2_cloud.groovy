/*
 * Configure the Jenkins EC2 Plugin via Groovy Script
 * Borrowed liberally from
 * @vrivellino - https://gist.github.com/vrivellino/97954495938e38421ba4504049fd44ea
 */

import hudson.model.*
import jenkins.model.*
import hudson.plugins.ec2.*
{% if jenkins_deploy_ec2_clouds %}
import com.amazonaws.services.ec2.model.InstanceType
{% endif %}

def instance = Jenkins.getInstance()

{% for cloud in jenkins_deploy_ec2_clouds %}

def worker_amis = []

{% for worker in cloud.workers %}

def ec2_tags_{{loop.index}} = []
{% for key, value in worker.ec2_tags|default([]) %}
ec2_tags_{{loop.index}}.add(new EC2Tag("{{ key }}", "{{ value }}"))
{% endfor %}

def worker_ami_{{loop.index}} = new SlaveTemplate(
  // String ami
  "{{ worker.ami_id }}",
  // String zone
  "{{ worker.zone | default('') }}",
  // SpotConfiguration spotConfig
  null,
  // String securityGroups
  "{{ worker.security_groups }}",
  // String remoteFS
  "{{ worker.remote_fs_root }}",
  // InstanceType type
  InstanceType.fromValue("{{ worker.instance_type }}"),
  // boolean ebsOptimized
  {{ worker.ebs_optimized | default(false)| lower() }},
  // String labelString
  "{{ worker.labels |default('') }}",
  // Node.Mode mode
  Node.Mode.{{ worker.mode | default('NORMAL')| upper() }},
  // String description
  "{{ worker.description }}",
  // String initScript
  """{{ worker.initscript | default('') }}""",
  // String tmpDir
  '{{ worker.tmpdir | default('') }}',
  // String userData
  """{{ worker.userdata | default('') }}""",
  // String numExecutors
  "{{ worker.executors | default(1) }}",
  // String remoteAdmin
  "{{ worker.remoteadmin | default('') }}",
  // AMITypeData amiType
  new UnixData(null, null, '22'),
  // String jvmopts
  '',
  // boolean stopOnTerminate
  false,
  // String subnetId
  "{{ worker.subnet_id | default('') }}",
  // List<EC2Tag> tags
  ec2_tags_{{loop.index}},
  // String idleTerminationMinutes
  '{{ worker.idle_terminate|default(30) }}',
  // boolean usePrivateDnsName
  {{ worker.usePrivateDns|default(false)|lower() }},
  // String instanceCapStr
  "{{ worker.instancecap | default(3) }}",
  // String iamInstanceProfile
  "{{ worker.profile_arn | default('') }}",
  // boolean deleteRootOntermination
  {{ worker.deleterootonterm|default(true)|bool|lower() }},
  // boolean useEphemeralDevices
  {{ worker.epehemeraldevices | default(false)| lower() }},
  // boolean useDedicatedTenancy
  {{ worker.dedicatedhw | default(false)| lower() }},
  // String launchTimeoutStr
  "{{ worker.launchtimeout | default('') }}",
  // boolean associatePublicIp
  {{ worker.associatepublicip | default(false)| lower() }},
  // String customDeviceMapping
  '',
  // boolean connectBySSHProcess
  {{ worker.connectsshprocess | default(false)| lower() }},
  // boolean connectUsingPublicIp
  {{ worker.connectpublicip | default(false)| lower() }},
)

worker_amis.add(worker_ami_{{loop.index}})
{% endfor %}


def new_cloud = new AmazonEC2Cloud(
  // String cloudName
  "{{ cloud.name }}",
  // boolean useInstanceProfileForCredentials
  false,
  // String credentialsId
  '{{ cloud.credential_id }}',
  // String region
  '{{ cloud.region }}',
  // String privateKey
  """{{ cloud.private_key }}""",
  // String instanceCapStr
  "{{ cloud.instance_cap }}",
  // List<? extends SlaveTemplate> templates
  worker_amis
)

def cloudList = instance.clouds

// avoid duplicate cloud provider on the cloud list
// pulled from https://gist.github.com/xbeta/e5edcf239fcdbe3f1672
if (cloudList.getByName("ec2-{{ cloud.name }}") ) {
    cloudList.remove(cloudList.getByName("ec2-{{ cloud.name }}"))
}
cloudList.add(new_cloud)

{% endfor %}
