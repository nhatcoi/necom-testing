package com.necom.demo;

/**
 * Phân loại kết quả dựa trên danh sách điểm: có vòng lặp duyệt mảng và nhiều nhánh.
 */
public final class ScoreAnalyzer {

    private ScoreAnalyzer() {
    }

    /**
     * @param scores danh sách điểm (0–100), không được null
     * @return nhãn kết quả: {@code EMPTY}, {@code INVALID}, {@code FAIL}, {@code PASS}
     */
    public static ResultLabel classify(int[] scores) {
        if (scores == null || scores.length == 0) {
            return ResultLabel.EMPTY;
        }

        int sum = 0;
        boolean hasInvalid = false;

        for (int score : scores) {
            if (score < 0 || score > 100) {
                hasInvalid = true;
            } else {
                sum += score;
            }
        }

        if (hasInvalid) {
            return ResultLabel.INVALID;
        }

        double average = (double) sum / scores.length;

        if (average >= 5.0) {
            return ResultLabel.PASS;
        }

        return ResultLabel.FAIL;
    }
}
