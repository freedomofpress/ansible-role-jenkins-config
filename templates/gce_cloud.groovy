import hudson.model.*
import jenkins.model.*
import com.google.jenkins.plugins.computeengine.AcceleratorConfiguration
import com.google.jenkins.plugins.computeengine.ComputeEngineCloud
import com.google.jenkins.plugins.computeengine.InstanceConfiguration
import com.google.jenkins.plugins.computeengine.NetworkConfiguration

def instance = Jenkins.getInstance()

{% for cloud in jenkins_deploy_gce_clouds %}

def worker_instances = []

{% for worker in cloud.workers %}

// I'm doing something here which Jenkins security doesnt like and had to whitelist
// Ideally I'd like to find a proper work-around
// See - https://jenkins.io/blog/2018/03/15/jep-200-lts/
class GCENetConf extends NetworkConfiguration {
    public GCENetConf(String network, String subnetwork) {
        super(network, subnetwork)
    }
}

def network_conf = new GCENetConf(
    // network
    "{{ worker.network|default('default') }}",
    // subnetwork
    "{{ worker.subnetwork|default('default') }}"
)

// Dont need to support this now personally
// It's up to the internetz to implement
AcceleratorConfiguration gpu_type = null

// https://github.com/jenkinsci/google-compute-engine-plugin/blob/master/src/main/java/com/google/jenkins/plugins/computeengine/InstanceConfiguration.java
def worker_gce = new InstanceConfiguration(
  // instance prefix name
  "{{ worker.prefix|default('jenkins-') }}",
  // region
  "{{ worker.region }}",
  // zone
  "{{ worker.zone }}",
  // machine type
  "{{ worker.type }}",
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
  "{{ worker.disktype | default('pd-standard') }}",
  // bootdisk autodelete
  {{ worker.autodelete|default(true)|bool|lower }},
  // String source image name
  "{{ worker.image_name }}",
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
if (cloudList.getByName("{{ cloud.name }}") ) {
    cloudList.remove(cloudList.getByName("{{ cloud.name }}"))
}
cloudList.add(new_cloud)

{% endfor %}
