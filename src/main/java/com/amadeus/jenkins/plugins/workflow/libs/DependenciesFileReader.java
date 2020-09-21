package com.amadeus.jenkins.plugins.workflow.libs;

import org.apache.commons.io.FilenameUtils;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class to read the dependencies file of this plugin
 */
public class DependenciesFileReader {

    private DependenciesFileReader() {
        // No constructor for utility class
    }

    public static final String LIBRARY_NAME_VERSION_PLACEHOLDER = "${library.NAME.version}";

    /**
     * Reads the provided by the template and version, and returns the map <dependency name, dependency version>
     * @param dependenciesFileUrl: the url of the dependencies file.
     *                           Example: dependencies.yaml
     *                           --------------------------
     *                           dependencies:
     *                           - lib1@version1
     *                           - lib2@version2
     *
     * @return the map <dependency name, dependency version>
     */
    public static Map<String, String> read(String dependenciesFileUrl) {

        // read http file

        // parse yaml

        // get name and version

        return new HashMap<String, String>();
    }



}
