/*
 * synopsys-detect
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.detect.workflow.blackduck.project.options;

public class ParentProjectMapOptions {
    private final String parentProjectName;
    private final String parentProjectVersionName;

    public ParentProjectMapOptions(String parentProjectName, String parentProjectVersionName) {
        this.parentProjectName = parentProjectName;
        this.parentProjectVersionName = parentProjectVersionName;
    }

    public String getParentProjectName() {
        return parentProjectName;
    }

    public String getParentProjectVersionName() {
        return parentProjectVersionName;
    }
}
