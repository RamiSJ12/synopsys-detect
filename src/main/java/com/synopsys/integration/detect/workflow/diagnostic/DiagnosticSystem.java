package com.synopsys.integration.detect.workflow.diagnostic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.detect.configuration.DetectInfo;
import com.synopsys.integration.detect.workflow.DetectRunId;
import com.synopsys.integration.detect.workflow.event.EventSystem;
import com.synopsys.integration.detect.workflow.file.DirectoryManager;

public class DiagnosticSystem {
    private static final String FAILED_TO_FINISH = "Failed to finish.";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PropertyConfiguration propertyConfiguration;
    private DiagnosticReportHandler diagnosticReportHandler;
    private DiagnosticLogSystem diagnosticLogSystem;
    private DiagnosticExecutableCapture diagnosticExecutableCapture;
    private DiagnosticFileCapture diagnosticFileCapture;
    private final DetectRunId detectRunId;
    private final DetectInfo detectInfo;
    private final DirectoryManager directoryManager;
    private final EventSystem eventSystem;

    public DiagnosticSystem(
        boolean isExtendedMode,
        PropertyConfiguration propertyConfiguration,
        DetectRunId detectRunId,
        DetectInfo detectInfo,
        DirectoryManager directoryManager,
        EventSystem eventSystem,
        SortedMap<String, String> maskedRawPropertyValues,
        Set<String> propertyKeys
    ) {
        this.propertyConfiguration = propertyConfiguration;
        this.detectRunId = detectRunId;
        this.detectInfo = detectInfo;
        this.directoryManager = directoryManager;
        this.eventSystem = eventSystem;

        init(isExtendedMode, maskedRawPropertyValues, propertyKeys);
    }

    private void init(boolean isExtendedMode, SortedMap<String, String> maskedRawPropertyValues, Set<String> propertyKeys) {
        System.out.println();
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println("Diagnostic mode on.");
        System.out.println("A zip file will be created with logs and relevant Detect output files.");
        System.out.println("It is generally not recommended to leave diagnostic mode on as you must manually clean up the zip.");
        if (!isExtendedMode) {
            System.out.println(
                "Additional relevant files such as lock files can be collected automatically in extended diagnostics (--detect.diagnostic.extended=true) but will not be in this run.");
        }
        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.println();

        logger.info("Initializing diagnostic components.");
        try {
            diagnosticReportHandler = new DiagnosticReportHandler(directoryManager.getReportOutputDirectory(), detectRunId.getRunId(), eventSystem);
            diagnosticLogSystem = new DiagnosticLogSystem(directoryManager.getLogOutputDirectory(), eventSystem);
            diagnosticExecutableCapture = new DiagnosticExecutableCapture(directoryManager.getExecutableOutputDirectory(), eventSystem);
            if (isExtendedMode) {
                diagnosticFileCapture = new DiagnosticFileCapture(directoryManager.getRelevantOutputDirectory(), eventSystem);
            }
        } catch (Exception e) {
            logger.error("Failed to process.", e);
        }

        logger.info("Creating configuration diagnostics reports.");

        diagnosticReportHandler.configurationsReport(detectInfo, propertyConfiguration, maskedRawPropertyValues, propertyKeys);

        logger.info("Diagnostics system is ready (extended mode: {}).", isExtendedMode);
    }

    public Map<String, String> getAdditionalDockerProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("logging.level.com.synopsys", "TRACE");
        return properties;
    }

    public void finish() {
        logger.info("Finishing diagnostic mode.");

        try {
            logger.info("Finishing reports.");
            diagnosticReportHandler.finish();
        } catch (Exception e) {
            logger.error(FAILED_TO_FINISH, e);
        }

        try {
            logger.info("Finishing logging.");
            diagnosticLogSystem.finish();
        } catch (Exception e) {
            logger.error(FAILED_TO_FINISH, e);
        }

        try {
            logger.info("Finishing executable capture.");
            diagnosticExecutableCapture.finish();
        } catch (Exception e) {
            logger.error(FAILED_TO_FINISH, e);
        }

        if (diagnosticFileCapture != null) {
            try {
                logger.info("Finishing file capture.");
                diagnosticFileCapture.finish();
            } catch (Exception e) {
                logger.error(FAILED_TO_FINISH, e);
            }
        }

        logger.info("Creating diagnostics zip.");
        boolean zipCreated = false;
        try {
            zipCreated = createZip();
        } catch (Exception e) {
            logger.error("Failed to create diagnostic zip. Cleanup will not occur.", e);
        }

        if (!zipCreated) {
            logger.error("Diagnostic mode failed to create zip. Cleanup will not occur.");
        }

        logger.info("Diagnostic mode has completed.");
    }

    private boolean createZip() {
        List<File> directoriesToCompress = new ArrayList<>();
        directoriesToCompress.add(directoryManager.getRunHomeDirectory());

        DiagnosticZipCreator zipper = new DiagnosticZipCreator();
        return zipper.createDiagnosticZip(detectRunId.getRunId(), directoryManager.getRunsOutputDirectory(), directoriesToCompress);
    }
}
