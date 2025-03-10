package com.synopsys.integration.detectable.detectables.go.gomod;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.synopsys.integration.detectable.ExecutableTarget;
import com.synopsys.integration.detectable.ExecutableUtils;
import com.synopsys.integration.detectable.detectable.executable.DetectableExecutableRunner;
import com.synopsys.integration.detectable.detectable.executable.ExecutableFailedException;

public class GoModCommandRunner {
    private static final String VERSION_COMMAND = "version";
    private static final String MOD_COMMAND = "mod";
    private static final String MOD_WHY_SUBCOMMAND = "why";
    private static final String MOD_GRAPH_SUBCOMMAND = "graph";
    private static final String LIST_COMMAND = "list";
    private static final String JSON_OUTPUT_FLAG = "-json";
    private static final String MODULE_OUTPUT_FLAG = "-m";
    private static final String VENDOR_OUTPUT_FLAG = "-vendor";
    private static final String LIST_READONLY_FLAG = "-mod=readonly";
    private static final String MODULE_NAME = "all";

    private final DetectableExecutableRunner executableRunner;

    public GoModCommandRunner(DetectableExecutableRunner executableRunner) {
        this.executableRunner = executableRunner;
    }

    // Excludes the "all" argument to return the root project modules
    public List<String> runGoList(File directory, ExecutableTarget goExe) throws ExecutableFailedException {
        return executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(directory, goExe, LIST_COMMAND, MODULE_OUTPUT_FLAG, JSON_OUTPUT_FLAG))
            .getStandardOutputAsList();
    }

    // TODO: Utilize the fields "Main": true, and "Indirect": true, fields from the JSON output to avoid running go list twice. Before switching to json output we needed to run twice. JM-01/2022
    public List<String> runGoListAll(File directory, ExecutableTarget goExe, GoVersion goVersion) throws ExecutableFailedException {
        List<String> goListCommand = new LinkedList<>();
        goListCommand.add(LIST_COMMAND);
        if (goVersion.getMajorVersion() > 1 || goVersion.getMinorVersion() >= 14) {
            // Providing a readonly flag prevents the command from modifying customer's source.
            goListCommand.add(LIST_READONLY_FLAG);
        }
        goListCommand.addAll(Arrays.asList(MODULE_OUTPUT_FLAG, JSON_OUTPUT_FLAG, MODULE_NAME));

        return executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(directory, goExe, goListCommand))
            .getStandardOutputAsList();
    }

    public String runGoVersion(File directory, ExecutableTarget goExe) throws ExecutableFailedException {
        List<String> goVersionOutput = executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(directory, goExe, VERSION_COMMAND))
            .getStandardOutputAsList();
        return goVersionOutput.get(0);
    }

    public List<String> runGoModGraph(File directory, ExecutableTarget goExe) throws ExecutableFailedException {
        return executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(directory, goExe, MOD_COMMAND, MOD_GRAPH_SUBCOMMAND))
            .getStandardOutputAsList();
    }

    public List<String> runGoModWhy(File directory, ExecutableTarget goExe, boolean vendorResults) throws ExecutableFailedException {
        // executing this command helps produce more accurate results. Parse the output to create a module exclusion list.
        List<String> commands = new LinkedList<>(Arrays.asList(MOD_COMMAND, MOD_WHY_SUBCOMMAND, MODULE_OUTPUT_FLAG));
        if (vendorResults) {
            commands.add(VENDOR_OUTPUT_FLAG);
        }
        commands.add(MODULE_NAME);
        return executableRunner.executeSuccessfully(ExecutableUtils.createFromTarget(directory, goExe, commands))
            .getStandardOutputAsList();
    }

}
