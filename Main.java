package com.example.imageedgescanner;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

public class Main {
    public static void main(String[] args) {
        Path inputDir = args.length > 0 ? Paths.get(args[0]) : Paths.get("Input");
        Path outputDir = args.length > 1 ? Paths.get(args[1]) : Paths.get("Output");

        try {
            Files.createDirectories(inputDir);
            Files.createDirectories(outputDir);

            List<Path> pngFiles;
            try (Stream<Path> pathStream = Files.list(inputDir)) {
                pngFiles = pathStream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png"))
                        .collect(Collectors.toList());
            }

            if (pngFiles.isEmpty()) {
                System.out.println("Input 資料夾沒有找到 .png 檔案: " + inputDir.toAbsolutePath());
                return;
            }

            for (Path pngFile : pngFiles) {
                processSingleImage(pngFile, outputDir);
            }

            System.out.println("完成：共處理 " + pngFiles.size() + " 張圖片，輸出於 " + outputDir.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("處理失敗: " + e.getMessage());
        }
    }

    private static void processSingleImage(Path inputPng, Path outputDir) {
        try {
            BufferedImage source = ImageIO.read(inputPng.toFile());
            if (source == null) {
                System.err.println("無法讀取圖片: " + inputPng);
                return;
            }

            BufferedImage segmentedImage = multiThresholdSegmentation(source);
            Path outputPath = outputDir.resolve(inputPng.getFileName().toString());
            ImageIO.write(segmentedImage, "png", outputPath.toFile());

            System.out.println("已輸出: " + outputPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("處理圖片失敗 " + inputPng + ": " + e.getMessage());
        }
    }

    /**
     * 使用 Multi-threshold (Otsu's method) 進行圖像分割
     * 將前景和背景分離
     */
    private static BufferedImage multiThresholdSegmentation(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // 轉為灰度圖
        int[][] gray = toGrayscale(image);

        // 計算直方圖
        int[] histogram = computeHistogram(gray);

        // 使用 Otsu's method 找到最佳閾值
        int threshold = otsuThreshold(histogram);

        System.out.println("自動計算的最佳閾值: " + threshold);

        // 根據閾值創建二值化圖像
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int grayValue = gray[y][x];
                // 前景 (grayValue >= threshold) 為黑色，背景 為白色
                int rgb = grayValue >= threshold ? 0x000000 : 0xFFFFFF;
                output.setRGB(x, y, rgb);
            }
        }

        return output;
    }

    /**
     * 計算灰度直方圖
     */
    private static int[] computeHistogram(int[][] gray) {
        int[] histogram = new int[256];
        for (int[] row : gray) {
            for (int pixelValue : row) {
                histogram[pixelValue]++;
            }
        }
        return histogram;
    }

    /**
     * Otsu's method: 自動計算最佳閾值
     * 通過最大化類間方差來找到最佳分割點
     */
    private static int otsuThreshold(int[] histogram) {
        int totalPixels = 0;
        for (int count : histogram) {
            totalPixels += count;
        }

        double sumTotal = 0;
        for (int i = 0; i < 256; i++) {
            sumTotal += i * histogram[i];
        }

        double sumBackground = 0;
        int countBackground = 0;
        double maxVariance = 0;
        int optimalThreshold = 0;

        for (int t = 0; t < 256; t++) {
            countBackground += histogram[t];
            if (countBackground == 0) continue;

            int countForeground = totalPixels - countBackground;
            if (countForeground == 0) break;

            sumBackground += t * histogram[t];

            double meanBackground = sumBackground / countBackground;
            double meanForeground = (sumTotal - sumBackground) / countForeground;

            double weightBackground = (double) countBackground / totalPixels;
            double weightForeground = (double) countForeground / totalPixels;

            double variance = weightBackground * weightForeground * 
                            (meanBackground - meanForeground) * (meanBackground - meanForeground);

            if (variance > maxVariance) {
                maxVariance = variance;
                optimalThreshold = t;
            }
        }

        return optimalThreshold;
    }

    private static int[][] toGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[][] gray = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                gray[y][x] = luminance;
            }
        }

        return gray;
    }
}
