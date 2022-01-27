package com.synopsys.integration.detectable.detectables.cargo.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.detectable.util.NameOptionalVersion;

class CargoDependencyLineParserTest {

    @Test
    void formatDependencyName() {
        CargoDependencyLineParser lineParser = new CargoDependencyLineParser();
        Optional<NameOptionalVersion> nameOptionalVersion = lineParser.formatDependencyName("abscissa_derive 0.5.0 (registry+https://github.com/rust-lang/crates.io-index)");

        assertTrue(nameOptionalVersion.isPresent());
        assertEquals("abscissa_derive", nameOptionalVersion.get().getName());
        assertTrue(nameOptionalVersion.get().getVersion().isPresent());
        assertEquals("0.5.0", nameOptionalVersion.get().getVersion().get());
    }

    @Test
    void formatDependencyNameNoVersion() {
        CargoDependencyLineParser lineParser = new CargoDependencyLineParser();
        Optional<NameOptionalVersion> nameOptionalVersion = lineParser.formatDependencyName("abscissa_derive");

        assertTrue(nameOptionalVersion.isPresent());
        assertEquals("abscissa_derive", nameOptionalVersion.get().getName());
        assertFalse(nameOptionalVersion.get().getVersion().isPresent());
    }

    @Test
    void formatDependencyNoData() {
        CargoDependencyLineParser lineParser = new CargoDependencyLineParser();
        Optional<NameOptionalVersion> nameOptionalVersion = lineParser.formatDependencyName("");

        assertFalse(nameOptionalVersion.isPresent());
    }
}