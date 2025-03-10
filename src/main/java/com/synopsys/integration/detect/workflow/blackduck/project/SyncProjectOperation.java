package com.synopsys.integration.detect.workflow.blackduck.project;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectCloneCategoriesType;
import com.synopsys.integration.blackduck.exception.BlackDuckApiException;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.model.ProjectSyncModel;
import com.synopsys.integration.blackduck.service.model.ProjectVersionWrapper;
import com.synopsys.integration.detect.configuration.DetectUserFriendlyException;
import com.synopsys.integration.detect.workflow.blackduck.project.options.CloneFindResult;
import com.synopsys.integration.detect.workflow.blackduck.project.options.ProjectGroupFindResult;
import com.synopsys.integration.detect.workflow.blackduck.project.options.ProjectSyncOptions;
import com.synopsys.integration.detect.workflow.blackduck.project.options.ProjectVersionLicenseFindResult;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.NameVersion;

public class SyncProjectOperation {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProjectService projectService;

    public SyncProjectOperation(ProjectService projectService) {
        this.projectService = projectService;
    }

    public ProjectVersionWrapper sync(
        NameVersion projectNameVersion,
        ProjectGroupFindResult projectGroupFindResult,
        CloneFindResult cloneFindResult,
        ProjectVersionLicenseFindResult projectVersionLicensesFindResult,
        ProjectSyncOptions projectSyncOptions
    ) throws DetectUserFriendlyException, IntegrationException {
        ProjectSyncModel projectSyncModel = createProjectSyncModel(
            projectNameVersion,
            projectGroupFindResult,
            cloneFindResult,
            projectVersionLicensesFindResult,
            projectSyncOptions
        );
        boolean forceUpdate = projectSyncOptions.isForceProjectVersionUpdate();

        //TODO- remove try-catch once there is a mechanism for validating property values against BD versions
        try {
            return projectService.syncProjectAndVersion(projectSyncModel, forceUpdate);
        } catch (BlackDuckApiException e) {
            if (projectSyncOptions.getCloneCategories().contains(ProjectCloneCategoriesType.DEEP_LICENSE)) {
                String message = "Attempt to create or update project failed.  If you are using a Black Duck earlier than 2022.2.0, this may be because you passed DEEP_LICENSE as a project clone category, either explicitly or by passing ALL.";
                throw new IntegrationException(message, e);
            }
            throw e;
        }
    }

    public ProjectSyncModel createProjectSyncModel(
        NameVersion projectNameVersion,
        ProjectGroupFindResult projectGroupFindResult,
        CloneFindResult cloneFindResult,
        ProjectVersionLicenseFindResult projectVersionLicensesFindResult,
        ProjectSyncOptions projectSyncOptions
    ) {
        ProjectSyncModel projectSyncModel = ProjectSyncModel.createWithDefaults(projectNameVersion.getName(), projectNameVersion.getVersion());

        // TODO: Handle a boolean property not being set in detect configuration - ie need to determine if this property actually exists in the ConfigurableEnvironment - just omit this one?
        projectSyncModel.setProjectLevelAdjustments(projectSyncOptions.getProjectLevelAdjustments());

        Optional.ofNullable(projectSyncOptions.getProjectVersionPhase()).ifPresent(projectSyncModel::setPhase);
        Optional.ofNullable(projectSyncOptions.getProjectVersionDistribution()).ifPresent(projectSyncModel::setDistribution);

        Integer projectTier = projectSyncOptions.getProjectTier();
        if (null != projectTier && projectTier >= 1 && projectTier <= 5) {
            projectSyncModel.setProjectTier(projectTier);
        }

        String description = projectSyncOptions.getProjectDescription();
        if (StringUtils.isNotBlank(description)) {
            projectSyncModel.setDescription(description);
        }

        String releaseComments = projectSyncOptions.getProjectVersionNotes();
        if (StringUtils.isNotBlank(releaseComments)) {
            projectSyncModel.setReleaseComments(releaseComments);
        }

        List<ProjectCloneCategoriesType> cloneCategories = projectSyncOptions.getCloneCategories();
        projectSyncModel.setCloneCategories(cloneCategories);

        String nickname = projectSyncOptions.getProjectVersionNickname();
        if (StringUtils.isNotBlank(nickname)) {
            projectSyncModel.setNickname(nickname);
        }

        if (cloneFindResult.getCloneUrl().isPresent()) {
            logger.debug("Cloning project version from release url: {}", cloneFindResult.getCloneUrl().get());
            projectSyncModel.setCloneFromReleaseUrl(cloneFindResult.getCloneUrl().get().string());
        }

        projectGroupFindResult.getProjectGroup().ifPresent(projectGroupUrl -> {
            logger.debug("Setting project group to url: {}", projectGroupUrl);
            projectSyncModel.setProjectGroup(projectGroupUrl.string());
        });

        projectVersionLicensesFindResult.getLicenseUrl().ifPresent(projectSyncModel::setVersionLicenseUrl);

        return projectSyncModel;
    }
}
