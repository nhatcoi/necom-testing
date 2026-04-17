package com.necom.demo;

/**
 * Chạy thử {@link ScoreAnalyzer} từ dòng lệnh.
 */
public final class DemoApp {

    public static void main(String[] args) {
        int[][] samples = {
                {},
                {8, 9},
                {3, 4},
                {-1, 10},
                {50, 60}
        };

        for (int i = 0; i < samples.length; i++) {
            ResultLabel label = ScoreAnalyzer.classify(samples[i]);
            System.out.println("Mẫu " + (i + 1) + ": " + java.util.Arrays.toString(samples[i]) + " -> " + label);
        }
    }
}
