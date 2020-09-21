package com.amadeus.jenkins.plugins.workflow.libs;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Job;
import org.jenkinsci.plugins.workflow.libs.LibraryConfiguration;
import org.jenkinsci.plugins.workflow.libs.LibraryResolver;
import org.jenkinsci.plugins.workflow.libs.LibraryRetriever;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//  mvn clean package
// mvn hpi:run
// mvnDebug hpi:run
//
//@Library(['ecommerce-library@18.0.3']) _
//        import com.amadeus.swb.acs.IAASParameters
//        import com.amadeus.swb.FileHelper
//
//        execEnv() {
//
//        checkoutGit()
//
//        IAASParameters NCE_PAAS = new IAASParameters("acs-server-aere-ci.in.int-a.nce1.paas.amadeus.net", 443, "openshift.int-a.nce1.paas.amadeus.net", 8443, "nce1")
//        FileHelper.getExtension('toto.txt')
//
//        }

@Extension(ordinal = 1000)
public class DependencyLibraryResolver extends LibraryResolver {

    @Override
    public boolean isTrusted() {
        return true;
    }

    // What is problematic and made us hack the existing code (org.jenkinsci.plugins.workflow.libs.LibraryAdder.add)
    // (1) We need to extract again the list of LibraryConfiguration. It would be better if the main code was providing it to plugins
    // (2) We would like plugins to be able to output a list of LibraryRecord ourselves. Instead we need to cheat and
    // provide a fake LibraryConfiguration with a default version set to the version of the library we want to use


    @Override
    public Collection<LibraryConfiguration> forJob(Job<?, ?> job, Map<String, String> libraryVersions) {
        List<LibraryConfiguration> libs = new ArrayList<>();

        // (1)
        Map<String, LibraryConfiguration> configurations = buildConfigurations(job, libraryVersions);

        for (Map.Entry<String, String> entry : libraryVersions.entrySet()) {
            String libraryName = entry.getKey();
            String libraryVersion = entry.getValue();

            for (LibraryConfiguration cfg : configurations.values()) {

                if (libraryName != null && libraryName.equals(cfg.getName())) {
                    if (libraryVersion == null) {
                        libraryVersion = cfg.getDefaultVersion();
                    }
                    if (libraryVersion == null) {
                        //throw new AbortException("No version specified for library " + libraryName);
                        // LOG
                    }
                    LibraryRetriever retriever = cfg.getRetriever();
                    if (retriever instanceof HttpRetriever) {
                        HttpRetriever httpRetriever = (HttpRetriever) retriever;
                        Map<String, String> dependencies = httpRetriever.retrieveDependencies(libraryVersion, libraryName); // where the magic happens
                        for (Map.Entry<String, String> dependency : dependencies.entrySet()) {
                            String dependencyName = dependency.getKey();
                            String dependencyVersion = dependency.getValue();
                            LibraryConfiguration dependencyConfiguration = configurations.get(dependencyName);
                            //TODO EXPLAIN
                            // (2)
                            LibraryConfiguration libraryAdaptedConfiguration = new LibraryConfiguration(dependencyName, dependencyConfiguration.getRetriever());
                            libraryAdaptedConfiguration.setDefaultVersion(dependencyVersion);
                            libs.add(libraryAdaptedConfiguration);
                        }
                    }
                }
            }

            /*
            if (entry.getKey().matches("github[.]com/([^/]+)/([^/]+)")) {
                String name = entry.getKey();
                // Currently GitHubSCMSource offers no particular advantage here over GitSCMSource.
                LibraryConfiguration lib = new LibraryConfiguration(name, new SCMSourceRetriever(new GitSCMSource(null, "https://" + name + ".git", "", "*", "", true)));
                lib.setDefaultVersion("master");
                libs.add(lib);
            }

             */
        }
        return libs;
    }

    private Map<String, LibraryConfiguration> buildConfigurations(Job<?, ?> job, Map<String, String> libraryVersions) {
        Map<String, LibraryConfiguration> toReturn = new HashMap<>();
        for (LibraryResolver libraryResolver : ExtensionList.lookup(LibraryResolver.class)) {
            if (!(libraryResolver instanceof DependencyLibraryResolver)) {
                for (LibraryConfiguration cfg : libraryResolver.forJob(job, libraryVersions)) {
                    toReturn.put(cfg.getName(), cfg);
                }
            }
        }
        return toReturn;
    }

}
