package com.synopsys.integration.detect.boot;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.common.util.Bds;
import com.synopsys.integration.detect.configuration.connection.BlackDuckConnectionDetails;
import com.synopsys.integration.detect.configuration.enumeration.BlackduckScanMode;
import com.synopsys.integration.detect.lifecycle.boot.decision.BlackDuckDecision;
import com.synopsys.integration.detect.lifecycle.boot.decision.ProductDecider;
import com.synopsys.integration.detect.tool.signaturescanner.BlackDuckSignatureScannerOptions;
import com.synopsys.integration.detect.workflow.bdio.BdioOptions;

class ProductDeciderTest {
    private final String VALID_URL = "http://example";

    @Test
    public void shouldRunBlackDuckOfflineWhenOverride() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, null);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(null, null);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.INTELLIGENT,
            createBdioOptions(false, true)
        );

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertTrue(productDecision.isOffline());
    }

    @Test
    public void shouldRunOfflineEvenWhenUrlProvided() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, "http://example.com");
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(null, null);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.INTELLIGENT,
            createBdioOptions(false, true)
        );

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertTrue(productDecision.isOffline());
    }

    @Test
    public void shouldRunBlackDuckOfflineWhenInstallUrl() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, null);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(null, VALID_URL);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.INTELLIGENT,
            createBdioOptions(false, true)
        );

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertTrue(productDecision.isOffline());
    }

    @Test
    public void shouldRunBlackDuckOfflineWhenInstallPath() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, null);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(Mockito.mock(Path.class), null);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.INTELLIGENT,
            createBdioOptions(false, true)
        );

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertTrue(productDecision.isOffline());
    }

    @Test
    public void shouldNotRunBlackDuckOfflineWhenUserProvidedHostUrl() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, null);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(Mockito.mock(Path.class), VALID_URL);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.INTELLIGENT,
            createBdioOptions(false, true)
        );

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertTrue(productDecision.isOffline());
    }

    @Test
    public void shouldRunBlackDuckOnline() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(null, null);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.INTELLIGENT,
            createBdioOptions(true, true)
        );

        Assertions.assertTrue(productDecision.shouldRun());
        Assertions.assertFalse(productDecision.isOffline());
    }

    @Test
    public void shouldNotRunBlackduckRapidModeAndOffline() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(true, VALID_URL);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(null, null);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.RAPID,
            createBdioOptions(false, true)
        );

        Assertions.assertFalse(productDecision.shouldRun());
    }

    @Test
    public void shouldNotRunBlackduckRapidModeAndBDIO2Disabled() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(null, null);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.RAPID,
            createBdioOptions(false, true)
        );

        Assertions.assertFalse(productDecision.shouldRun());
    }

    @Test
    public void shouldNotRunBlackduckIntelligentModeAndBDIO2Disabled() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(null, null);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.INTELLIGENT,
            createBdioOptions(false, false)
        );

        Assertions.assertFalse(productDecision.shouldRun());
    }

    @Test
    public void shouldRunBlackduckRapidModeAndBDIO2Enabled() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(null, null);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.RAPID,
            createBdioOptions(true, true)
        );

        Assertions.assertTrue(productDecision.shouldRun());
    }

    @Test
    public void shouldRunBlackduckIntelligentModeAndBDIO2Enabled() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(null, null);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.INTELLIGENT,
            createBdioOptions(true, false)
        );

        Assertions.assertTrue(productDecision.shouldRun());
    }

    @Test
    public void shouldRunBlackduckLegacyEnabledAndIntelligentModeAndBDIO2Disabled() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, VALID_URL);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(null, null);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.INTELLIGENT,
            createBdioOptions(false, true)
        );

        Assertions.assertTrue(productDecision.shouldRun());
    }

    @Test
    public void shouldNotRunBlackduckURLMissing() {
        BlackDuckConnectionDetails blackDuckConnectionDetails = blackDuckConnectionDetails(false, null);
        BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions = blackDuckSignatureScannerOptions(null, null);
        BlackDuckDecision productDecision = new ProductDecider().decideBlackDuck(
            blackDuckConnectionDetails,
            blackDuckSignatureScannerOptions,
            BlackduckScanMode.RAPID,
            createBdioOptions(true, true)
        );

        Assertions.assertFalse(productDecision.shouldRun());
    }

    private BdioOptions createBdioOptions(boolean useBdio2, boolean enableLegacyUpload) {
        return new BdioOptions(useBdio2, null, null, enableLegacyUpload);
    }

    private BlackDuckSignatureScannerOptions blackDuckSignatureScannerOptions(Path offlineScannerInstallPath, String userProvidedScannerInstallUrl) {
        return new BlackDuckSignatureScannerOptions(Bds.listOf(), Bds.listOf(), null, 1024, 1, false, null, false, null, null, null, 1, null, false, false, true,
            true
        );
    }

    private BlackDuckConnectionDetails blackDuckConnectionDetails(boolean offline, String blackduckUrl) {
        return new BlackDuckConnectionDetails(offline, blackduckUrl, null, null, null);
    }
}
