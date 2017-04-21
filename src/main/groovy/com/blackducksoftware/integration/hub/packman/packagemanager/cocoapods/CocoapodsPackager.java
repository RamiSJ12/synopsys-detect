/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.packman.packagemanager.cocoapods;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.bdio.simple.model.DependencyNode;
import com.blackducksoftware.integration.hub.bdio.simple.model.Forge;
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.ExternalId;
import com.blackducksoftware.integration.hub.bdio.simple.model.externalid.NameVersionExternalId;
import com.blackducksoftware.integration.hub.packman.InputStreamConverter;
import com.blackducksoftware.integration.hub.packman.OutputCleaner;
import com.blackducksoftware.integration.hub.packman.Packager;
import com.blackducksoftware.integration.hub.packman.packagemanager.cocoapods.model.PodLock;
import com.blackducksoftware.integration.hub.packman.packagemanager.cocoapods.model.Podfile;
import com.blackducksoftware.integration.hub.packman.packagemanager.cocoapods.model.Podspec;
import com.blackducksoftware.integration.hub.packman.packagemanager.cocoapods.parsers.PodLockParser;
import com.blackducksoftware.integration.hub.packman.packagemanager.cocoapods.parsers.PodfileParser;
import com.blackducksoftware.integration.hub.packman.packagemanager.cocoapods.parsers.PodspecParser;

public class CocoapodsPackager extends Packager {
    public static final String COMMENTS = "#";

    private final InputStreamConverter inputStreamConverter;

    private final OutputCleaner outputCleaner;

    private final InputStream podfileStream;

    private final InputStream podlockStream;

    private final InputStream podspecStream;

    public CocoapodsPackager(final InputStreamConverter inputStreamConverter, final OutputCleaner outputCleaner, final InputStream podfileStream,
            final InputStream podlockStream, final InputStream podspecStream) {
        this.inputStreamConverter = inputStreamConverter;
        this.outputCleaner = outputCleaner;
        this.podfileStream = podfileStream;
        this.podlockStream = podlockStream;
        this.podspecStream = podspecStream;
    }

    @Override
    public List<DependencyNode> makeDependencyNodes() {
        final List<DependencyNode> packages = new ArrayList<>();
        final Map<String, String> workspaceProjects = new HashMap<>();

        final PodfileParser podfileParser = new PodfileParser(outputCleaner);
        final BufferedReader podfileBufferedReader = inputStreamConverter.convertToBufferedReader(podlockStream);
        final Podfile podfile = podfileParser.parse(podfileBufferedReader);

        final PodLockParser podLockParser = new PodLockParser();
        final BufferedReader podLockBufferedReader = inputStreamConverter.convertToBufferedReader(podlockStream);
        final PodLock podLock = podLockParser.parse(podLockBufferedReader);

        final PodspecParser podspecParser = new PodspecParser(outputCleaner);
        Podspec podspec = null;
        if (podspecStream != null) {
            final BufferedReader podspecBufferedReader = inputStreamConverter.convertToBufferedReader(podspecStream);
            podspec = podspecParser.parse(podspecBufferedReader);
            for (final DependencyNode target : podfile.targets) {
                workspaceProjects.put(target.name, podspec.version);
            }
        }

        final Map<String, DependencyNode> allPods = getDependencies(podLock);

        for (final DependencyNode target : podfile.targets) {
            final List<DependencyNode> targetDependencies = new ArrayList<>();
            for (final DependencyNode dep : target.children) {
                final DependencyNode finalDependency = allPods.get(dep.name);
                if (finalDependency != null) {
                    targetDependencies.add(finalDependency);
                }
            }
            target.children = targetDependencies;
            final String versionFound = workspaceProjects.get(target.name);
            if (StringUtils.isNotBlank(versionFound)) {
                target.version = versionFound;
            }
            target.externalId = new NameVersionExternalId(Forge.cocoapods, target.name, target.version);
            packages.add(target);
        }

        return packages;
    }

    public Map<String, DependencyNode> getDependencies(final PodLock podLock) {
        final Map<String, DependencyNode> allPods = new HashMap<>();
        for (final DependencyNode pod : podLock.pods) {
            allPods.put(pod.name, pod);
        }

        // Fix pods dependencies
        for (final Entry<String, DependencyNode> pod : allPods.entrySet()) {
            final List<DependencyNode> pod_deps = new ArrayList<>();
            for (final DependencyNode dependency : pod.getValue().children) {
                pod_deps.add(allPods.get(dependency.name));
            }
            pod.getValue().children = pod_deps;
        }
        return allPods;
    }

    public static DependencyNode createPodNodeFromGroups(final Matcher regexMatcher, final int nameGroup, final int versionGroup) {
        DependencyNode node = null;
        if (regexMatcher.matches()) {
            try {
                final String name = regexMatcher.group(nameGroup).trim();
                final String version = regexMatcher.group(versionGroup).trim();
                final ExternalId externalId = new NameVersionExternalId(Forge.cocoapods, name, version);
                node = new DependencyNode(name, version, externalId, new ArrayList<DependencyNode>());
            } catch (final IllegalStateException e) {
                e.printStackTrace();
            } catch (final IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        return node;
    }

}
