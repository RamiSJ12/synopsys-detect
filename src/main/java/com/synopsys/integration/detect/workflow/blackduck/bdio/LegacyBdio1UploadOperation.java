package com.synopsys.integration.detect.workflow.blackduck.bdio;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationData;
import com.synopsys.integration.blackduck.codelocation.bdiolegacy.BdioUploadService;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.synopsys.integration.exception.IntegrationException;

public class LegacyBdio1UploadOperation extends BdioUploadOperation {
    private final BdioUploadService bdioUploadService;

    public LegacyBdio1UploadOperation(BdioUploadService bdioUploadService) {
        this.bdioUploadService = bdioUploadService;
    }

    @Override
    protected CodeLocationCreationData<UploadBatchOutput> executeUpload(UploadBatch uploadBatch) throws IntegrationException {
        return bdioUploadService.uploadBdio(uploadBatch);
    }
}
