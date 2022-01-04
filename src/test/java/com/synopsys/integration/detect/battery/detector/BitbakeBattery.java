package com.synopsys.integration.detect.battery.detector;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.detect.battery.util.DetectorBatteryTestRunner;
import com.synopsys.integration.detect.configuration.DetectProperties;

@Tag("battery")
public class BitbakeBattery {

    @Test
    void testBitbakeDependencyDetector() {
        DetectorBatteryTestRunner test = new DetectorBatteryTestRunner("bitbake-orig-poky", "bitbake/orig/poky");
        test.sourceFileFromResource("oe-init-build-env");
        test.sourceFileFromResource("task-depends.dot");
        test.executableFromResourceFiles(DetectProperties.DETECT_BASH_PATH.getProperty(), "bitbake-g.xout", "bitbake-layers-show-recipes.xout");
        test.property("detect.bitbake.package.names", "core-image-sato");
        test.expectBdioResources();
        test.run();
    }

    @Test
    void testBitbakeManifestDetectorByLayer() {
        DetectorBatteryTestRunner test = new DetectorBatteryTestRunner("bitbake-manifest-bylayer", "bitbake/manifest/bylayer");
        test.sourceFileFromResource("oe-init-build-env");
        test.sourceFileFromResource("task-depends.dot");
        test.executableFromResourceFiles(DetectProperties.DETECT_BASH_PATH.getProperty(), "bitbake-g.xout", "bitbake-layers-show-recipes.xout");
        test.property("detect.bitbake.package.names", "core-image-sato");
        test.property("detect.bitbake.algorithm", "BYLAYER");
        test.property("detect.bitbake.license.manifest.file.path", "src/test/resources/battery/bitbake/manifest/bylayer/license.manifest");
        test.expectBdioResources();
        test.run();
    }

    @Test
    void testBitbakeManifestDetectorBiLevel() {
        DetectorBatteryTestRunner test = new DetectorBatteryTestRunner("bitbake-manifest-bilevel", "bitbake/manifest/bilevel");
        test.sourceFileFromResource("oe-init-build-env");
        test.sourceFileFromResource("task-depends.dot");
        test.executableFromResourceFiles(DetectProperties.DETECT_BASH_PATH.getProperty(), "bitbake-g.xout", "bitbake-layers-show-recipes.xout");
        test.property("detect.bitbake.package.names", "core-image-sato");
        test.property("detect.bitbake.algorithm", "BILEVEL");
        test.property("detect.bitbake.license.manifest.file.path", "src/test/resources/battery/bitbake/manifest/bilevel/license.manifest");
        test.expectBdioResources();
        test.run();
    }
}
