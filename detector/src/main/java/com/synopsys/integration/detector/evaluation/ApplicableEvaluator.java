package com.synopsys.integration.detector.evaluation;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.detectable.Detectable;
import com.synopsys.integration.detectable.DetectableEnvironment;
import com.synopsys.integration.detectable.detectable.result.DetectableResult;
import com.synopsys.integration.detector.base.DetectorEvaluation;
import com.synopsys.integration.detector.base.DetectorEvaluationTree;
import com.synopsys.integration.detector.result.DetectorResult;
import com.synopsys.integration.detector.rule.DetectorRule;

public class ApplicableEvaluator extends Evaluator {
    private final Logger logger = LoggerFactory.getLogger(ApplicableEvaluator.class);
    private final DetectorRuleSetEvaluator detectorRuleSetEvaluator = new DetectorRuleSetEvaluator();

    public ApplicableEvaluator(DetectorEvaluationOptions evaluationOptions) {
        super(evaluationOptions);
    }

    @Override
    protected DetectorEvaluationTree performEvaluation(DetectorEvaluationTree rootEvaluation) {
        logger.info("Evaluating detectors. This may take a while.");
        searchAndApplicableEvaluation(rootEvaluation, new HashSet<>());
        return rootEvaluation;
    }

    public void searchAndApplicableEvaluation(DetectorEvaluationTree detectorEvaluationTree, Set<DetectorRule> appliedInParent) {
        logger.trace("Determining applicable detectors on the directory: {}", detectorEvaluationTree.getDirectory());

        Set<DetectorRule> appliedSoFar = new HashSet<>();

        for (DetectorEvaluation detectorEvaluation : detectorEvaluationTree.getOrderedEvaluations()) {
            getDetectorEvaluatorListener().ifPresent(it -> it.applicableStarted(detectorEvaluation));

            DetectorRule detectorRule = detectorEvaluation.getDetectorRule();
            logger.trace("Evaluating detector: {}", detectorRule.getDescriptiveName());

            SearchEnvironment searchEnvironment = new SearchEnvironment(
                detectorEvaluationTree.getDepthFromRoot(),
                getEvaluationOptions().getDetectorFilter(),
                getEvaluationOptions().isForceNested(),
                getEvaluationOptions().isFollowSymLinks(),
                appliedInParent,
                appliedSoFar
            );
            detectorEvaluation.setSearchEnvironment(searchEnvironment);

            DetectorResult searchableResult = detectorRuleSetEvaluator.evaluateSearchable(
                detectorEvaluationTree.getDetectorRuleSet(),
                detectorEvaluation.getDetectorRule(),
                searchEnvironment
            );
            detectorEvaluation.setSearchable(searchableResult);

            if (detectorEvaluation.isSearchable()) {
                logger.trace("Searchable passed, will continue evaluating.");
                //TODO: potential todo, this could be invoked as part of the rule - ie we make a DetectableEnvironmentCreatable and the file could be given to the creatable (detectorRule.createEnvironment(file)
                DetectableEnvironment detectableEnvironment = new DetectableEnvironment(detectorEvaluationTree.getDirectory());
                detectorEvaluation.setDetectableEnvironment(detectableEnvironment);

                Detectable detectable = detectorRule.createDetectable(detectableEnvironment);
                detectorEvaluation.setDetectable(detectable);

                DetectableResult applicable = detectable.applicable();
                DetectorResult applicableResult = new DetectorResult(
                    applicable.getPassed(),
                    applicable.toDescription(),
                    applicable.getClass(),
                    applicable.getExplanation(),
                    applicable.getRelevantFiles()
                );
                detectorEvaluation.setApplicable(applicableResult);

                if (detectorEvaluation.isApplicable()) {
                    logger.trace("Found applicable detector: {}", detectorRule.getDescriptiveName());
                    appliedSoFar.add(detectorRule);
                } else {
                    logger.trace("Applicable did not pass: {}", detectorEvaluation.getApplicabilityMessage());
                }
            } else {
                logger.trace("Searchable did not pass: {}", detectorEvaluation.getSearchabilityMessage());
            }

            getDetectorEvaluatorListener().ifPresent(it -> it.applicableEnded(detectorEvaluation));
        }

        if (!appliedSoFar.isEmpty()) {
            //TODO: Perfect log level also matters here. To little and we may appear stuck, but we may also be flooding the logs.
            logger.debug("Found ({}) applicable detectors in: {}", appliedSoFar.size(), detectorEvaluationTree.getDirectory());
        }

        Set<DetectorRule> nextAppliedInParent = new HashSet<>();
        nextAppliedInParent.addAll(appliedInParent);
        nextAppliedInParent.addAll(appliedSoFar);

        for (DetectorEvaluationTree childDetectorEvaluationTree : detectorEvaluationTree.getChildren()) {
            searchAndApplicableEvaluation(childDetectorEvaluationTree, nextAppliedInParent);
        }
    }
}
