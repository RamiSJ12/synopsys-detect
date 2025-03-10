package com.synopsys.integration.detect.tool.detector.inspectors.projectinspector;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.util.OperatingSystemType;

public class ProjectInspectorExecutableLocator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DetectInfo detectInfo;

    public ProjectInspectorExecutableLocator(DetectInfo detectInfo) {
        this.detectInfo = detectInfo;
    }

    @Nullable
    public File findExecutable(File extractedZip) throws DetectableException {
        return findExecutable(extractedZip, determineExecutableNameFromOS());
    }

    @NotNull
    public String determineExecutableNameFromOS() {
        if (detectInfo.getCurrentOs() == OperatingSystemType.WINDOWS) {
            return "project-inspector.exe";
        } else if (detectInfo.getCurrentOs() == OperatingSystemType.MAC) {
            return "project-inspector";
        } else {
            return "project-inspector";
        }
    }

    @Nullable
    public File findExecutable(File extractedZip, String executableName) throws DetectableException {
        logger.debug("Looking for '" + executableName + "' in " + extractedZip.toString());
        File bin = new File(extractedZip, "bin");
        File executable = new File(bin, executableName);
        if (executable.exists()) {
            logger.debug("Found it: " + executable);
            if (!executable.canExecute()) {
                if (!executable.setExecutable(true)) {
                    throw new DetectableException("Unable to set project inspector to executable: " + executable);
                }
            }
            return executable;
        } else {
            logger.debug("Could not find executable: " + executable);
            return null;
        }
    }
}
