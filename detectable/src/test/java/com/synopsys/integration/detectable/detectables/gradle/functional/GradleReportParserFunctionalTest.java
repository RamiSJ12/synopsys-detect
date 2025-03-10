package com.synopsys.integration.detectable.detectables.gradle.functional;

import java.io.File;
import java.util.Optional;

import org.apache.commons.lang3.SystemUtils;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.bdio.graph.DependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.annotations.UnitTest;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.util.EnumListFilter;
import com.synopsys.integration.detectable.detectables.gradle.inspection.GradleConfigurationType;
import com.synopsys.integration.detectable.detectables.gradle.inspection.model.GradleReport;
import com.synopsys.integration.detectable.detectables.gradle.inspection.parse.GradleReportParser;
import com.synopsys.integration.detectable.detectables.gradle.inspection.parse.GradleReportTransformer;
import com.synopsys.integration.detectable.util.FunctionalTestFiles;
import com.synopsys.integration.detectable.util.graph.GraphAssert;
import com.synopsys.integration.detectable.util.graph.MavenGraphAssert;

@UnitTest
public class GradleReportParserFunctionalTest {

    @Test
    void extractCodeLocationTest() throws JSONException {
        Assumptions.assumeFalse(SystemUtils.IS_OS_WINDOWS); //Does not work on windows due to path issues.

        GradleReportParser gradleReportParser = new GradleReportParser();
        Optional<GradleReport> gradleReport = gradleReportParser.parseReport(FunctionalTestFiles.asFile("/gradle/dependencyGraph.txt"));
        Assertions.assertTrue(gradleReport.isPresent());
        GradleReportTransformer transformer = new GradleReportTransformer(new ExternalIdFactory(), EnumListFilter.excludeNone());
        CodeLocation codeLocation = transformer.transform(gradleReport.get());
        Assertions.assertNotNull(codeLocation);

        Assertions.assertEquals("hub-detect", gradleReport.get().getProjectName());
        Assertions.assertEquals("2.0.0-SNAPSHOT", gradleReport.get().getProjectVersionName());

        String actual = new Gson().toJson(codeLocation);

        JSONAssert.assertEquals(FunctionalTestFiles.asString("/gradle/dependencyGraph-expected.json"), actual, false);
    }

    @Test
    void complexTest() {
        Optional<CodeLocation> codeLocation = buildCodeLocation("/gradle/complex_dependencyGraph.txt", true);
        Assertions.assertTrue(codeLocation.isPresent());
        DependencyGraph graph = codeLocation.get().getDependencyGraph();

        MavenGraphAssert graphAssert = new MavenGraphAssert(graph);
        graphAssert.hasDependency("non-project:with-nested:1.0.0");
        graphAssert.hasDependency("solo:component:4.12");
        graphAssert.hasDependency("some.group:child:2.2.2");
        graphAssert.hasDependency("terminal:child:6.2.3");

        // TODO: Get this to work in a sensible way
        //        graphAssert.noDependency("child-project");
        //        graphAssert.noDependency("nested-parent");
        //        graphAssert.noDependency("spring-webflux");
        //        graphAssert.noDependency("spring-beans");
        //        graphAssert.noDependency("spring-core");
        //        graphAssert.noDependency("spring-web");
        //        graphAssert.noDependency("should-suppress");

        graphAssert.hasRootDependency("solo:component:4.12");
        graphAssert.hasRootDependency("non-project:with-nested:1.0.0");
        graphAssert.hasRootDependency("some.group:parent:5.0.0");
        graphAssert.hasRootDependency("terminal:child:6.2.3");

        ExternalId parent = graphAssert.hasDependency("some.group:parent:5.0.0");
        ExternalId child = graphAssert.hasDependency("some.group:child:2.2.2");
        graphAssert.hasParentChildRelationship(parent, child);
    }

    private Optional<CodeLocation> buildCodeLocation(String resource, boolean includeUnresolvedConfigurations) {
        File file = FunctionalTestFiles.asFile(resource);
        GradleReportParser gradleReportParser = new GradleReportParser();
        EnumListFilter<GradleConfigurationType> enumListFilter = EnumListFilter.excludeNone();
        if (!includeUnresolvedConfigurations) {
            enumListFilter = EnumListFilter.fromExcluded(GradleConfigurationType.UNRESOLVED);
        }
        GradleReportTransformer gradleReportTransformer = new GradleReportTransformer(new ExternalIdFactory(), enumListFilter);

        return gradleReportParser.parseReport(file)
            .map(gradleReportTransformer::transform);
    }

    @Test
    void testImplementationsGraph() {
        Optional<CodeLocation> codeLocation = buildCodeLocation("/gradle/gradle_implementations_dependencyGraph.txt", true);
        Assertions.assertTrue(codeLocation.isPresent());

        DependencyGraph dependencyGraph = codeLocation.get().getDependencyGraph();
        GraphAssert graphAssert = new GraphAssert(Forge.MAVEN, dependencyGraph);
        graphAssert.hasRootSize(7);

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(codeLocation.get()));
    }

    @Test
    void testUnresolvedConfigurations() {
        Optional<CodeLocation> codeLocation = buildCodeLocation("/gradle/gradle_implementations_dependencyGraph.txt", false);
        Assertions.assertTrue(codeLocation.isPresent());

        DependencyGraph dependencyGraph = codeLocation.get().getDependencyGraph();
        GraphAssert graphAssert = new GraphAssert(Forge.MAVEN, dependencyGraph);
        graphAssert.hasRootSize(0);

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(codeLocation.get()));
    }
}
