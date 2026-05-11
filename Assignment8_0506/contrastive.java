import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class contrastive {

    // 影像輸入大小，會被縮放成固定尺寸後再抽特徵
    static final int SIZE = 32;

    // Sobel 邊緣特徵的維度：只保留內圈像素，所以是 (SIZE - 2) * (SIZE - 2)
    static final int FEATURE_DIM = (SIZE - 2) * (SIZE - 2);

    // embedding 向量維度
    static final int EMBEDDING_DIM = 8;

    // 學習率
    static final double LR = 0.001;

    // 訓練回合數
    static final int EPOCHS = 800;

    // 負樣本相似度門檻，超過時才會拉遠
    static final double MARGIN = 0.5;

    // 隨機數：用於權重初始化與資料增強
    Random random = new Random();

    // 單一圖片樣本：名稱、影像與最後的 embedding
    static class Sample {

        String name;

        BufferedImage image;

        double[] embedding;

        Sample(String name, BufferedImage image) {
            this.name = name;
            this.image = image;
        }
    }

    List<Sample> samples = new ArrayList<>();

    // 線性 encoder 的權重與偏差
    double[][] W = new double[EMBEDDING_DIM][FEATURE_DIM];

    double[] b = new double[EMBEDDING_DIM];

    public contrastive() {

        loadSamples();

        initWeights();
    }

    void loadSamples() {

        // 依序載入使用者提供的三張圖片
        samples.add(load("img1.jpg"));
        samples.add(load("img2.jpg"));
        samples.add(load("img3.jpg"));
    }

    Sample load(String path) {

        try {
            return new Sample(path, ImageIO.read(new File(path)));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void initWeights() {

        // 用小隨機值初始化 encoder 權重
        for (int i = 0; i < EMBEDDING_DIM; i++) {
            for (int j = 0; j < FEATURE_DIM; j++) {

                W[i][j] = random.nextGaussian() * 0.01;
            }
        }
    }

    // =====================================
    // Feature Extraction
    // =====================================

    double[] extract(BufferedImage image, boolean augment) {

        // 先縮放到固定大小，方便後續特徵抽取
        BufferedImage resized = new BufferedImage(
                SIZE,
                SIZE,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = resized.createGraphics();

        if (augment) {

            // 簡單資料增強：隨機旋轉幾度
            double angle = Math.toRadians(random.nextInt(20) - 10);

            g.rotate(angle, SIZE / 2.0, SIZE / 2.0);
        }

        g.drawImage(image, 0, 0, SIZE, SIZE, null);

        g.dispose();

        double[][] gray = new double[SIZE][SIZE];

        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {

                // 轉成灰階，降低色彩差異的影響
                Color c = new Color(resized.getRGB(x, y));

                gray[y][x] = (
                        c.getRed() * 0.299 +
                        c.getGreen() * 0.587 +
                        c.getBlue() * 0.114
                ) / 255.0;
            }
        }

        // Sobel X 與 Sobel Y kernel，用來擷取邊緣
        int[][] gx = {
                {-1,0,1},
                {-2,0,2},
                {-1,0,1}
        };

        int[][] gy = {
                {-1,-2,-1},
                {0,0,0},
                {1,2,1}
        };

        double[] feature = new double[FEATURE_DIM];

        int idx = 0;

        for (int y = 1; y < SIZE - 1; y++) {
            for (int x = 1; x < SIZE - 1; x++) {

                // 計算該位置的 Sobel 邊緣強度
                double sx = 0;
                double sy = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {

                        sx += gray[y + ky][x + kx] * gx[ky + 1][kx + 1];
                        sy += gray[y + ky][x + kx] * gy[ky + 1][kx + 1];
                    }
                }

                feature[idx++] = Math.sqrt(sx * sx + sy * sy);
            }
        }

        normalize(feature);

        return feature;
    }

    // =====================================
    // Encoder
    // =====================================

    double[] encode(double[] x) {

        // 線性投影 + tanh，形成低維 embedding
        double[] z = new double[EMBEDDING_DIM];

        for (int i = 0; i < EMBEDDING_DIM; i++) {

            double sum = b[i];

            for (int j = 0; j < FEATURE_DIM; j++) {

                sum += W[i][j] * x[j];
            }

            z[i] = Math.tanh(sum);
        }

        // 把 embedding 做 L2 正規化，方便 cosine similarity 比較
        l2(z);

        return z;
    }

    // =====================================
    // Cosine Similarity
    // =====================================

    double cosine(double[] a, double[] b) {

        // 計算兩個 embedding 的 cosine similarity
        double dot = 0;
        double na = 0;
        double nb = 0;

        for (int i = 0; i < a.length; i++) {

            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }

        return dot / (Math.sqrt(na) * Math.sqrt(nb) + 1e-8);
    }

    // =====================================
    // Training
    // =====================================

    void train() {

        for (int epoch = 0; epoch < EPOCHS; epoch++) {

            double totalLoss = 0;

            // positive pairs
            for (Sample sample : samples) {

                // 同一張圖做兩次增強，當成正樣本對
                double[] x1 = extract(sample.image, true);
                double[] x2 = extract(sample.image, true);

                double[] z1 = encode(x1);
                double[] z2 = encode(x2);

                double sim = cosine(z1, z2);

                // 正樣本希望相似度越高越好
                double loss = 1.0 - sim;

                totalLoss += loss;

                // 拉近正樣本 embedding
                backprop(x1, z1, z2, true);
            }

            // negative pairs
            for (int i = 0; i < samples.size(); i++) {
                for (int j = i + 1; j < samples.size(); j++) {

                    // 不同圖片當作負樣本對
                    double[] x1 = extract(samples.get(i).image, true);
                    double[] x2 = extract(samples.get(j).image, true);

                    double[] z1 = encode(x1);
                    double[] z2 = encode(x2);

                    double sim = cosine(z1, z2);

                    if (sim > MARGIN) {

                        // 相似度太高時才把負樣本拉開
                        double loss = sim - MARGIN;

                        totalLoss += loss;

                        // 推遠負樣本 embedding
                        backprop(x1, z1, z2, false);
                    }
                }
            }

            // 每 50 個 epoch 印一次訓練狀態
            if (epoch % 50 == 0) {

                System.out.printf(
                        "Epoch %d | Loss = %.6f%n",
                        epoch,
                        totalLoss
                );
            }
        }
    }

    // =====================================
    // Backprop
    // =====================================

    void backprop(
            double[] x,
            double[] z1,
            double[] z2,
            boolean positive
    ) {

        // 非完整反向傳播，只做簡化版權重更新
        for (int i = 0; i < EMBEDDING_DIM; i++) {

            double grad;

            if (positive) {
                grad = (z1[i] - z2[i]);
            }
            else {
                grad = -(z1[i] - z2[i]);
            }

            for (int j = 0; j < FEATURE_DIM; j++) {

                W[i][j] -= LR * grad * x[j];
            }

            b[i] -= LR * grad;
        }
    }

    // =====================================
    // Utility
    // =====================================

    void normalize(double[] x) {

        // 標準化：平均數為 0、標準差為 1
        double mean = 0;

        for (double v : x)
            mean += v;

        mean /= x.length;

        double std = 0;

        for (double v : x)
            std += (v - mean) * (v - mean);

        std = Math.sqrt(std / x.length) + 1e-8;

        for (int i = 0; i < x.length; i++) {
            x[i] = (x[i] - mean) / std;
        }
    }

    void l2(double[] x) {

        // L2 normalize：把向量長度壓成 1
        double norm = 0;

        for (double v : x)
            norm += v * v;

        norm = Math.sqrt(norm) + 1e-8;

        for (int i = 0; i < x.length; i++) {
            x[i] /= norm;
        }
    }

    // =====================================
    // Result
    // =====================================

    void showResult() {

        // 印出每張圖片最後學到的 embedding
        System.out.println("\n=== Embeddings ===");

        for (Sample s : samples) {

            s.embedding = encode(extract(s.image, false));

            System.out.println(s.name);
            System.out.println(Arrays.toString(s.embedding));
            System.out.println();
        }

        System.out.println("=== Similarity ===");

        // 印出兩兩圖片之間的相似度
        for (int i = 0; i < samples.size(); i++) {
            for (int j = i + 1; j < samples.size(); j++) {

                double sim = cosine(
                        samples.get(i).embedding,
                        samples.get(j).embedding
                );

                System.out.printf(
                        "%s <-> %s = %.6f%n",
                        samples.get(i).name,
                        samples.get(j).name,
                        sim
                );
            }
        }
    }

    public static void main(String[] args) {

        contrastive model = new contrastive();

        model.train();

        model.showResult();
    }
}
