package com.necom.demo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Bao phủ từng lệnh (statement coverage) của {@link ScoreAnalyzer}.
 */
class ScoreAnalyzerStatementCoverageTest {

    @Test
    void nullScores_isEmpty() {
        assertEquals(ResultLabel.EMPTY, ScoreAnalyzer.classify(null));
    }

    @Test
    void emptyArray_isEmpty() {
        assertEquals(ResultLabel.EMPTY, ScoreAnalyzer.classify(new int[0]));
    }

    @Test
    void singleValidHighScore_isPass() {
        assertEquals(ResultLabel.PASS, ScoreAnalyzer.classify(new int[]{10}));
    }

    @Test
    void singleValidLowScore_isFail() {
        assertEquals(ResultLabel.FAIL, ScoreAnalyzer.classify(new int[]{4}));
    }

    @Test
    void negativeScore_isInvalid() {
        assertEquals(ResultLabel.INVALID, ScoreAnalyzer.classify(new int[]{-1}));
    }

    @Test
    void scoreAbove100_isInvalid() {
        assertEquals(ResultLabel.INVALID, ScoreAnalyzer.classify(new int[]{101}));
    }

    @Test
    void multipleIterations_validScores_averagePass() {
        assertEquals(ResultLabel.PASS, ScoreAnalyzer.classify(new int[]{5, 5}));
    }

    @Test
    void multipleIterations_validScores_averageFail() {
        assertEquals(ResultLabel.FAIL, ScoreAnalyzer.classify(new int[]{4, 5}));
    }
}
