package com.synopsys.integration.detect.lifecycle.shutdown;

import java.io.File;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.configuration.config.PropertyConfiguration;
import com.synopsys.integration.detect.configuration.DetectProperties;
import com.synopsys.integration.detect.lifecycle.boot.DetectBootResult;
import com.synopsys.integration.detect.lifecycle.run.data.BlackDuckRunData;
import com.synopsys.integration.detect.lifecycle.run.data.ProductRunData;
import com.synopsys.integration.detect.workflow.diagnostic.DiagnosticSystem;
import com.synopsys.integration.detect.workflow.phonehome.PhoneHomeManager;

public class ShutdownDecider {
    public ShutdownDecision decideShutdown(DetectBootResult detectBootResult) {
        DiagnosticSystem diagnosticSystem = detectBootResult.getDiagnosticSystem()
            .orElse(null);

        PhoneHomeManager phoneHomeManager = detectBootResult.getProductRunData()
            .filter(ProductRunData::shouldUseBlackDuckProduct)
            .map(ProductRunData::getBlackDuckRunData)
            .flatMap(BlackDuckRunData::getPhoneHomeManager)
            .orElse(null);

        CleanupDecision cleanupDecision = decideCleanup(
            detectBootResult.getDetectConfiguration().orElse(null),
            detectBootResult.getProductRunData().orElse(null),
            detectBootResult.getAirGapZip().orElse(null)
        );
        return new ShutdownDecision(phoneHomeManager, diagnosticSystem, cleanupDecision);
    }

    public CleanupDecision decideCleanup(@Nullable PropertyConfiguration detectConfiguration, @Nullable ProductRunData productRunData, @Nullable File airGapZip) {
        if (detectConfiguration == null || !detectConfiguration.getValue(DetectProperties.DETECT_CLEANUP)) {
            return CleanupDecision.skip();
        }
        boolean dryRun = detectConfiguration.getValue(DetectProperties.DETECT_BLACKDUCK_SIGNATURE_SCANNER_DRY_RUN);

        boolean offline = false;
        if (productRunData != null && productRunData.shouldUseBlackDuckProduct()) {
            offline = !productRunData.getBlackDuckRunData().isOnline();
        }

        boolean preserveScan = dryRun || offline;
        boolean preserveBdio = offline;
        boolean preserveAirGap = airGapZip != null;
        return new CleanupDecision(true, preserveScan, preserveBdio, preserveAirGap, airGapZip);
    }
}
