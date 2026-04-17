package com.necom.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Bao phủ các đường đi độc lập chính (path / nhánh kết thúc) qua {@link ScoreAnalyzer}.
 */
class ScoreAnalyzerPathCoverageTest {

    @Test
    @DisplayName("Đường đi: mảng rỗng hoặc null → EMPTY")
    void path_emptyInput() {
        assertEquals(ResultLabel.EMPTY, ScoreAnalyzer.classify(null));
        assertEquals(ResultLabel.EMPTY, ScoreAnalyzer.classify(new int[0]));
    }

    @Test
    @DisplayName("Đường đi: có điểm ngoài [0,100] → INVALID (không xét trung bình)")
    void path_invalidScores() {
        assertEquals(ResultLabel.INVALID, ScoreAnalyzer.classify(new int[]{50, 101}));
        assertEquals(ResultLabel.INVALID, ScoreAnalyzer.classify(new int[]{0, -3}));
    }

    @Test
    @DisplayName("Đường đi: toàn điểm hợp lệ, trung bình ≥ 5 → PASS")
    void path_allValid_passBranch() {
        assertEquals(ResultLabel.PASS, ScoreAnalyzer.classify(new int[]{5}));
        assertEquals(ResultLabel.PASS, ScoreAnalyzer.classify(new int[]{10, 0}));
    }

    @Test
    @DisplayName("Đường đi: toàn điểm hợp lệ, trung bình < 5 → FAIL")
    void path_allValid_failBranch() {
        assertEquals(ResultLabel.FAIL, ScoreAnalyzer.classify(new int[]{4}));
        assertEquals(ResultLabel.FAIL, ScoreAnalyzer.classify(new int[]{0, 0, 9}));
    }

    @Test
    @DisplayName("Đường đi trong vòng lặp: xen kẽ hợp lệ / không hợp lệ rồi INVALID")
    void path_loopBranches_leadToInvalid() {
        assertEquals(ResultLabel.INVALID, ScoreAnalyzer.classify(new int[]{100, -1, 50}));
    }
}
