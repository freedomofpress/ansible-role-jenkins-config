import hudson.model.*
import jenkins.model.*
import com.google.jenkins.plugins.computeengine.AcceleratorConfiguration
import com.google.jenkins.plugins.computeengine.AutofilledNetworkConfiguration
import com.google.jenkins.plugins.computeengine.ComputeEngineCloud
import com.google.jenkins.plugins.computeengine.InstanceConfiguration
import com.google.jenkins.plugins.computeengine.NetworkConfiguration

def instance = Jenkins.getInstance()
{% for cloud in jenkins_deploy_gce_clouds %}

def worker_instances = []

{% for worker in cloud.workers %}

{% set API_ROOT = google_compute_api_root + '/' + cloud.project %}
{% set API_ZONE_ROOT = API_ROOT + '/zones/' + worker.zone %}

def network_conf = new AutofilledNetworkConfiguration(
    // network
    "{{ API_ROOT }}/global/networks/default",
    // subnetwork
    "default"
)

// Dont need to support this now personally
// It's up to the internetz to implement
AcceleratorConfiguration gpu_type = null

// https://github.com/jenkinsci/google-compute-engine-plugin/blob/master/src/main/java/com/google/jenkins/plugins/computeengine/InstanceConfiguration.java
def worker_gce = new InstanceConfiguration(
  // instance prefix name
  "{{ worker.prefix|default('jenkins-') }}",
  // region
  "{{ API_ROOT }}/regions/{{ worker.region }}",
  // zone
  "{{ API_ZONE_ROOT }}",
  // machine type
  "{{ API_ZONE_ROOT }}/machineTypes/{{ worker.type }}",
  // number of executors
  "{{ worker.executors|default(1) }}",
  // startupscript
  """{{ worker.startup_script|default('') }}""",
  // preemptible VM ? (cheaper but can be terminated at will)
  {{ worker.preemptible|default(false)|bool|lower }},
  // labels (for jenkins internal usage)
  "{{ worker.labels|default([])|join(" ") }}",
  // description
  "{{ worker.description }}",
  // bootdisk type
  "{{ API_ZONE_ROOT }}/diskTypes/{{ worker.disktype | default('pd-standard') }}",
  // bootdisk autodelete
  {{ worker.autodelete|default(true)|bool|lower }},
  // String source image name
  "{{ API_ROOT }}/global/images/{{ worker.image_name }}",
  // String source image project
  "{{ worker.image_project }}",
  // Bootdisk GB size
  "{{ worker.disk_size|default(10)|int }}",
  // network configuration
  network_conf,
  // external address ?
  {{ worker.ext_addr|default(true)|bool|lower }},
  // use internal external address for communication?
  // this will work even if you activate an external address
  {{ worker.int_addr|default(false)|bool|lower }},
  // network tags
  "{{ worker.net_tags|default([])|join(" ") }}",
  // service account (email) that will be associated with the node
  "{{ worker.service_email | default('') }}",
  // retention time in minutes
  "{{ worker.retention |default(30) }}",
  // launch timeout in seconds
  // (The number of seconds a new node has to provision and come online. )
  "{{ worker.launchtimeout |default(300) }}",
  // Node.Mode mode (NORMAL or EXCLUSIVE)
  // NORMAL == as much as possible
  // EXCLUSIVE == only for jobs that match this builder label type
  Node.Mode.{{ worker.mode | default('NORMAL')| upper() }},
  // GPU type
  gpu_type,
  // Run as user
  "{{ worker.runasuser | default('jenkins') }}"
)
worker_instances.add(worker_gce)
{% endfor %}


def new_cloud = new ComputeEngineCloud(
  // String cloudName
  "{{ cloud.name }}",
  // project id name
  "{{ cloud.project }}",
  // String credentialsId
  "{{ cloud.credential_id }}",
  // max number of instances to launch
  "{{ cloud.instance_max|default('1') }}",
  // List of instance configurations
  worker_instances
)

def cloudList = instance.clouds

// avoid duplicate cloud provider on the cloud list
// pulled from https://gist.github.com/xbeta/e5edcf239fcdbe3f1672
if (cloudList.getByName("gce-{{ cloud.name }}") ) {
    cloudList.remove(cloudList.getByName("gce-{{ cloud.name }}"))
}
cloudList.add(new_cloud)

{% endfor %}
