import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class contrastive extends JPanel {

    static class DataPoint {
        double[] embedding;
        String label;

        public DataPoint(String label, double[] embedding) {
            this.label = label;
            this.embedding = embedding;
        }
    }

    DataPoint A, B, C;

    double lr = 0.01;
    double margin = 5.0;

    public contrastive() {
        A = new DataPoint("A", extractFeature("img1.jpg"));
        B = new DataPoint("B", extractFeature("img2.jpg"));
        C = new DataPoint("C", extractFeature("img3.jpg"));
    }

    // 🔥 特徵提取（16x16 downsample）
    double[] extractFeature(String path) {
        try {
            BufferedImage img = ImageIO.read(new File(path));

            int size = 16;
            BufferedImage resized = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.drawImage(img, 0, 0, size, size, null);
            g.dispose();

            double[] feature = new double[size * size];

            int idx = 0;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    int rgb = resized.getRGB(i, j);
                    Color c = new Color(rgb);

                    double gray = (c.getRed() + c.getGreen() + c.getBlue()) / 3.0;
                    feature[idx++] = gray / 255.0;
                }
            }

            return feature;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[256];
    }

    // L2 distance
    double distance(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        return Math.sqrt(sum);
    }

    // 🔥 Contrastive 更新
    void updatePair(DataPoint p1, DataPoint p2, int y) {
        double dist = distance(p1.embedding, p2.embedding);

        for (int i = 0; i < p1.embedding.length; i++) {
            double diff = p1.embedding[i] - p2.embedding[i];

            if (y == 1) {
                // 拉近
                p1.embedding[i] -= lr * diff;
                p2.embedding[i] += lr * diff;
            } else {
                // 推遠（margin內）
                if (dist < margin) {
                    double grad = (margin - dist);
                    double direction = diff / (dist + 1e-6);

                    p1.embedding[i] += lr * grad * direction;
                    p2.embedding[i] -= lr * grad * direction;
                }
            }
        }
    }

    // 🔥 訓練邏輯（👉 這裡你可以自己改）
    void trainStep() {

        // ✨ 你可以自由定義關係

        // 假設 A 和 B 要靠近（同類）
        updatePair(A, B, 1);

        // A, C 不同
        updatePair(A, C, 0);

        // B, C 不同
        updatePair(B, C, 0);
    }

    // 🎨 視覺化（取前兩維）
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawPoint(g, A, Color.BLUE);
        drawPoint(g, B, Color.BLUE);
        drawPoint(g, C, Color.RED);
    }

    void drawPoint(Graphics g, DataPoint p, Color color) {
        int scale = 200;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        int x = centerX + (int)(p.embedding[0] * scale);
        int y = centerY - (int)(p.embedding[1] * scale);

        g.setColor(color);
        g.fillOval(x, y, 10, 10);
        g.drawString(p.label, x + 5, y - 5);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Contrastive Learning (Advanced)");

        contrastive panel = new contrastive();

        frame.add(panel);
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        new Timer(100, e -> {
            panel.trainStep();
            panel.repaint();
        }).start();
    }
}