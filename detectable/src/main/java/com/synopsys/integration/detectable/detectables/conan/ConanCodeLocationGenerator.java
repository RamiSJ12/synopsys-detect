package com.synopsys.integration.detectable.detectables.conan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.bdio.graph.MutableMapDependencyGraph;
import com.synopsys.integration.bdio.model.Forge;
import com.synopsys.integration.bdio.model.dependency.Dependency;
import com.synopsys.integration.bdio.model.externalid.ExternalId;
import com.synopsys.integration.bdio.model.externalid.ExternalIdFactory;
import com.synopsys.integration.detectable.detectable.codelocation.CodeLocation;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;
import com.synopsys.integration.detectable.detectable.util.EnumListFilter;
import com.synopsys.integration.detectable.detectables.conan.cli.config.ConanDependencyType;
import com.synopsys.integration.detectable.detectables.conan.graph.ConanGraphNode;
import com.synopsys.integration.detectable.detectables.conan.graph.ConanNode;

public class ConanCodeLocationGenerator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Forge conanForge = new Forge("/", "conan");
    private final EnumListFilter<ConanDependencyType> dependencyTypeFilter;
    private final boolean preferLongFormExternalIds;

    public ConanCodeLocationGenerator(EnumListFilter<ConanDependencyType> dependencyTypeFilter, boolean preferLongFormExternalIds) {
        this.dependencyTypeFilter = dependencyTypeFilter;
        this.preferLongFormExternalIds = preferLongFormExternalIds;
    }

    @NotNull
    public ConanDetectableResult generateCodeLocationFromNodeMap(ExternalIdFactory externalIdFactory, Map<String, ConanNode<String>> nodes) throws DetectableException {
        logger.debug("Generating code location from {} dependencies", nodes.keySet().size());
        Optional<ConanNode<String>> rootNode = getRoot(nodes.values());
        if (!rootNode.isPresent()) {
            throw new DetectableException("No root node found");
        }
        ConanGraphNode rootGraphNode = new ConanGraphNode(rootNode.get());
        populateGraphUnderNode(rootGraphNode, nodes);
        MutableMapDependencyGraph dependencyGraph = new MutableMapDependencyGraph();
        CodeLocation codeLocation = generateCodeLocationFromConanGraph(externalIdFactory, dependencyGraph, rootGraphNode);
        return new ConanDetectableResult(
            rootGraphNode.getConanNode().getName().orElse(null),
            rootGraphNode.getConanNode().getVersion().orElse(null),
            codeLocation
        );
    }

    private void populateGraphUnderNode(ConanGraphNode curGraphNode, Map<String, ConanNode<String>> graphNodes) throws DetectableException {
        Set<String> dependencyRefs = new HashSet<>(curGraphNode.getConanNode().getRequiresRefs().orElse(new ArrayList<>(0)));
        dependencyTypeFilter.ifShouldInclude(ConanDependencyType.BUILD, curGraphNode.getConanNode().getBuildRequiresRefs(), dependencyRefs::addAll);

        for (String childRef : dependencyRefs) {
            ConanNode<String> childNode = graphNodes.get(childRef);
            if (childNode == null) {
                throw new DetectableException(String.format("%s requires non-existent node %s", curGraphNode.getConanNode().getRef(), childRef));
            }
            ConanGraphNode childGraphNode = new ConanGraphNode(childNode);
            populateGraphUnderNode(childGraphNode, graphNodes);
            curGraphNode.addChild(childGraphNode);
        }
    }

    @NotNull
    private CodeLocation generateCodeLocationFromConanGraph(ExternalIdFactory externalIdFactory, MutableMapDependencyGraph dependencyGraph, ConanGraphNode rootNode)
        throws DetectableException {
        addNodeChildrenUnderNode(externalIdFactory, dependencyGraph, 0, rootNode, null);
        return new CodeLocation(dependencyGraph);
    }

    private void addNodeChildrenUnderNode(
        ExternalIdFactory externalIdFactory,
        MutableMapDependencyGraph dependencyGraph,
        int depth,
        ConanGraphNode currentNode,
        Dependency currentDep
    ) throws DetectableException {
        Consumer<Dependency> childAdder;
        if (depth == 0) {
            childAdder = dependencyGraph::addChildToRoot;
        } else {
            childAdder = childDep -> dependencyGraph.addChildWithParent(childDep, currentDep);
        }
        for (ConanGraphNode childNode : currentNode.getChildren()) {
            Dependency childDep = generateDependency(externalIdFactory, childNode);
            childAdder.accept(childDep);
            addNodeChildrenUnderNode(externalIdFactory, dependencyGraph, depth + 1, childNode, childDep);
        }
    }

    @NotNull
    private Dependency generateDependency(ExternalIdFactory externalIdFactory, ConanGraphNode graphNode) throws DetectableException {
        String depName = graphNode.getConanNode().getName().orElseThrow(
            () -> new DetectableException(String.format("Missing dependency name: %s", graphNode.getConanNode()))
        );
        String fullVersion = ConanExternalIdVersionGenerator.generateExternalIdVersionString(graphNode.getConanNode(), preferLongFormExternalIds);
        ExternalId externalId = externalIdFactory.createNameVersionExternalId(conanForge, depName, fullVersion);
        logger.trace("Generated Dependency for {}/{} with externalID: {}", depName, fullVersion, externalId.getExternalIdPieces());
        return new Dependency(
            depName,
            graphNode.getConanNode().getVersion().orElseThrow(
                () -> new DetectableException(String.format("Missing dependency version: %s", graphNode.getConanNode()))
            ),
            externalId
        );
    }

    @NotNull
    private Optional<ConanNode<String>> getRoot(Collection<ConanNode<String>> graphNodes) {
        return graphNodes.stream().filter(ConanNode::isRootNode).findFirst();
    }
}
