package com.synopsys.integration.detect.configuration.help.json;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.configuration.property.Property;
import com.synopsys.integration.configuration.property.deprecation.PropertyRemovalDeprecationInfo;
import com.synopsys.integration.configuration.util.Group;
import com.synopsys.integration.detect.configuration.enumeration.ExitCodeType;
import com.synopsys.integration.detector.base.DetectorStatusCode;

public class HelpJsonWriter {
    private final Logger logger = LoggerFactory.getLogger(HelpJsonWriter.class);

    private final Gson gson;

    public HelpJsonWriter(Gson gson) {
        this.gson = gson;
    }

    public void writeGsonDocument(String filename, List<Property> detectOptions, List<HelpJsonDetector> buildDetectors, List<HelpJsonDetector> buildlessDetectors) {
        HelpJsonData data = new HelpJsonData();

        data.getOptions().addAll(detectOptions.stream().map(this::convertOption).collect(Collectors.toList()));
        data.getExitCodes().addAll(Stream.of(ExitCodeType.values()).map(this::convertExitCode).collect(Collectors.toList()));
        data.getDetectorStatusCodes().addAll(Stream.of(DetectorStatusCode.values()).map(this::convertDetectorStatusCode).collect(Collectors.toList()));
        data.setBuildlessDetectors(buildlessDetectors);
        data.setBuildDetectors(buildDetectors);

        try {
            try (Writer writer = new FileWriter(filename)) {
                gson.toJson(data, writer);
            }

            logger.info(filename + " was created in your current directory.");
        } catch (IOException e) {
            logger.error("There was an error when creating the html file", e);
        }
    }

    public HelpJsonDetectorStatusCode convertDetectorStatusCode(DetectorStatusCode statusCode) {
        HelpJsonDetectorStatusCode helpJsonDetectorStatusCode = new HelpJsonDetectorStatusCode();
        helpJsonDetectorStatusCode.setStatusCode(statusCode.name());
        helpJsonDetectorStatusCode.setStatusCodeDescription(statusCode.getDescription());
        return helpJsonDetectorStatusCode;
    }

    public HelpJsonExitCode convertExitCode(ExitCodeType exitCodeType) {
        HelpJsonExitCode helpJsonExitCode = new HelpJsonExitCode();
        helpJsonExitCode.setExitCodeKey(exitCodeType.name());
        helpJsonExitCode.setExitCodeValue(exitCodeType.getExitCode());
        helpJsonExitCode.setExitCodeDescription(exitCodeType.getDescription());
        return helpJsonExitCode;
    }

    public HelpJsonOption convertOption(Property property) {
        if (property.getPropertyGroupInfo() == null) {
            throw new IllegalArgumentException("The property \"" + property.getKey() + "\" has no group information. Group is required.");
        }

        HelpJsonOption helpJsonOption = new HelpJsonOption();

        helpJsonOption.setPropertyName(property.getName());
        helpJsonOption.setPropertyKey(property.getKey());
        helpJsonOption.setPropertyType(property.describeType() == null ? "None" : property.describeType());
        helpJsonOption.setAddedInVersion(property.getFromVersion());
        helpJsonOption.setDefaultValue(property.describeDefault());

        helpJsonOption.setGroup(property.getPropertyGroupInfo().getPrimaryGroup().getName());
        String superGroupName = "";
        if (property.getPropertyGroupInfo().getPrimaryGroup().getSuperGroup().isPresent()) {
            superGroupName = property.getPropertyGroupInfo().getPrimaryGroup().getSuperGroup().get().getName();
        }
        helpJsonOption.setSuperGroup(superGroupName);
        helpJsonOption.setAdditionalGroups(property.getPropertyGroupInfo().getAdditionalGroups().stream().map(Group::getName).collect(Collectors.toList()));
        helpJsonOption.setCategory(property.getCategory() == null ? "" : property.getCategory().getName());
        helpJsonOption.setDescription(property.getPropertyHelpInfo().getShortText());
        helpJsonOption.setDetailedDescription(property.getPropertyHelpInfo().getLongText() == null ? "" : property.getPropertyHelpInfo().getLongText());
        if (property.getPropertyDeprecationInfo().getRemovalInfo().isPresent()) {
            PropertyRemovalDeprecationInfo removalInfo = property.getPropertyDeprecationInfo().getRemovalInfo().get();
            helpJsonOption.setDeprecatedDescription(removalInfo.getDescription());
            helpJsonOption.setDeprecatedRemoveInVersion(removalInfo.getRemoveInVersion().getDisplayValue());
            helpJsonOption.setDeprecated(true);
        } else {
            helpJsonOption.setDeprecated(false);
        }
        helpJsonOption.setDeprecatedValues(property.getPropertyDeprecationInfo().getDeprecatedValues().stream()
            .map(value -> new HelpJsonOptionDeprecatedValue(value.getValueDescription(), value.getReason()))
            .collect(Collectors.toList()));

        helpJsonOption.setStrictValues(property.isOnlyExampleValues());
        helpJsonOption.setCaseSensitiveValues(property.isCaseSensitive());
        helpJsonOption.setAcceptableValues(property.listExampleValues().stream().map(Objects::toString).collect(Collectors.toList()));
        helpJsonOption.setHasAcceptableValues(property.listExampleValues().size() > 0);
        helpJsonOption.setCommaSeparatedList(property.isCommaSeparated());
        helpJsonOption.setExample(property.getExample());
        return helpJsonOption;
    }
}
