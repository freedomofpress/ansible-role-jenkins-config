import hudson.model.*
import jenkins.model.*;
import javaposse.jobdsl.plugin.*;
import javaposse.jobdsl.dsl.DslScriptLoader;


def jenkins = Jenkins.getInstance();
def seedfile = new File( '{{ jenkins_deploy_seed_master }}' );

jm = new JenkinsJobManagement(System.out, [:], new File('.'))
dslScriptLoader = new DslScriptLoader(jm)
dslScriptLoader.runScript(seedfile.text)
