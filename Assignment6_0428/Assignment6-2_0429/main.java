import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class main {
    static final int RB = 5, TB = 12;
    static final double R0 = 0.125, R1 = 2.0;

    public static void main(String[] args) throws Exception {
        File input = new File(args.length > 0 ? args[0] : "fox.jpg");
        BufferedImage image = ImageIO.read(input);
        if (image == null) throw new IllegalArgumentException("Cannot read " + input.getAbsolutePath());

        double[][] gray = gray(image);
        boolean[][] mask = clean(threshold(gray));
        Box fox = largestBox(components(mask));
        List<P> points = boundaryPoints(mask, fox);
        points = sample(points, 180);
        double[][] desc = shapeContexts(points);

        BufferedImage out = render(mask, fox, points, image.getWidth(), image.getHeight());
        ImageIO.write(out, "png", new File("fox_scanned.png"));

        System.out.println("points=" + points.size() + ", bins=" + (RB * TB));
        if (desc.length > 1) System.out.println("chi-square=" + String.format("%.6f", chiSquare(desc[0], desc[1])));
    }

    static double[][] gray(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        double[][] g = new double[h][w];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int rgb = img.getRGB(x, y);
            int r = (rgb >> 16) & 255, gg = (rgb >> 8) & 255, b = rgb & 255;
            g[y][x] = 0.299 * r + 0.587 * gg + 0.114 * b;
        }
        return g;
    }

    static boolean[][] threshold(double[][] g) {
        int h = g.length, w = g[0].length;
        int[] hist = new int[256];
        for (double[] row : g) for (double v : row) hist[(int) Math.round(v)]++;

        int total = w * h, sum = 0;
        for (int i = 0; i < 256; i++) sum += i * hist[i];
        int wb = 0, sb = 0, t = 127;
        double best = -1;
        for (int i = 0; i < 256; i++) {
            wb += hist[i];
            if (wb == 0) continue;
            int wf = total - wb;
            if (wf == 0) break;
            sb += i * hist[i];
            double mb = (double) sb / wb, mf = (double) (sum - sb) / wf;
            double v = wb * (double) wf * (mb - mf) * (mb - mf);
            if (v > best) {
                best = v;
                t = i;
            }
        }

        boolean[][] m = new boolean[h][w];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) m[y][x] = g[y][x] < t;
        return m;
    }

    static boolean[][] clean(boolean[][] m) {
        int h = m.length, w = m[0].length;
        boolean[][] d = new boolean[h][w];
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            boolean on = false;
            for (int yy = -1; yy <= 1 && !on; yy++) for (int xx = -1; xx <= 1; xx++) {
                int ny = y + yy, nx = x + xx;
                if (ny >= 0 && ny < h && nx >= 0 && nx < w && m[ny][nx]) { on = true; break; }
            }
            d[y][x] = on;
        }
        return d;
    }

    static List<Box> components(boolean[][] m) {
        int h = m.length, w = m[0].length;
        boolean[][] seen = new boolean[h][w];
        List<Box> boxes = new ArrayList<>();
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            if (!m[y][x] || seen[y][x]) continue;
            ArrayDeque<P> q = new ArrayDeque<>();
            q.add(new P(x, y));
            seen[y][x] = true;
            int minX = x, minY = y, maxX = x, maxY = y, area = 0;
            while (!q.isEmpty()) {
                P p = q.removeFirst();
                area++;
                if (p.x < minX) minX = p.x;
                if (p.y < minY) minY = p.y;
                if (p.x > maxX) maxX = p.x;
                if (p.y > maxY) maxY = p.y;
                for (int yy = -1; yy <= 1; yy++) for (int xx = -1; xx <= 1; xx++) {
                    if (xx == 0 && yy == 0) continue;
                    int nx = p.x + xx, ny = p.y + yy;
                    if (nx < 0 || ny < 0 || nx >= w || ny >= h || seen[ny][nx] || !m[ny][nx]) continue;
                    seen[ny][nx] = true;
                    q.add(new P(nx, ny));
                }
            }
            if (area > 50) boxes.add(new Box(minX, minY, maxX, maxY, area));
        }
        return boxes;
    }

    static Box largestBox(List<Box> boxes) {
        return boxes.stream().max(Comparator.comparingInt(b -> b.area)).orElseThrow();
    }

    static List<P> boundaryPoints(boolean[][] m, Box b) {
        List<P> pts = new ArrayList<>();
        for (int y = Math.max(0, b.y1); y <= Math.min(m.length - 1, b.y2); y++) {
            for (int x = Math.max(0, b.x1); x <= Math.min(m[0].length - 1, b.x2); x++) {
                if (!m[y][x]) continue;
                boolean edge = false;
                for (int yy = -1; yy <= 1 && !edge; yy++) for (int xx = -1; xx <= 1; xx++) {
                    if (xx == 0 && yy == 0) continue;
                    int nx = x + xx, ny = y + yy;
                    if (nx < 0 || ny < 0 || nx >= m[0].length || ny >= m.length || !m[ny][nx]) { edge = true; break; }
                }
                if (edge) pts.add(new P(x, y));
            }
        }
        return pts;
    }

    static List<P> sample(List<P> pts, int n) {
        if (pts.size() <= n) return pts;
        List<P> out = new ArrayList<>();
        double step = (double) pts.size() / n;
        for (int i = 0; i < n; i++) out.add(pts.get((int) (i * step)));
        return out;
    }

    static double[][] shapeContexts(List<P> pts) {
        int n = pts.size();
        double[][] d = new double[n][n];
        double sum = 0;
        int cnt = 0, fi = 0, fj = 0;
        for (int i = 0; i < n; i++) for (int j = i + 1; j < n; j++) {
            double v = dist(pts.get(i), pts.get(j));
            d[i][j] = d[j][i] = v;
            sum += v;
            cnt++;
            if (v > d[fi][fj]) { fi = i; fj = j; }
        }
        double mean = cnt == 0 ? 1.0 : sum / cnt;
        double base = Math.atan2(pts.get(fj).y - pts.get(fi).y, pts.get(fj).x - pts.get(fi).x);
        double[][] desc = new double[n][RB * TB];
        double[] rEdges = {R0, Math.pow(2, -1.5), Math.pow(2, -0.5), Math.pow(2, 0.5), Math.pow(2, 1.5), R1};
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) if (i != j) {
                double r = d[i][j] / mean;
                if (r < R0 || r > R1) continue;
                int rb = 0;
                while (rb < RB && r > rEdges[rb + 1]) rb++;
                double a = norm(Math.atan2(pts.get(j).y - pts.get(i).y, pts.get(j).x - pts.get(i).x) - base);
                int tb = (int) Math.floor(a / (2 * Math.PI / TB));
                if (tb < 0) tb = 0;
                if (tb >= TB) tb = TB - 1;
                desc[i][rb * TB + tb]++;
            }
            double s = 0;
            for (double v : desc[i]) s += v;
            if (s > 0) for (int k = 0; k < desc[i].length; k++) desc[i][k] /= s;
        }
        return desc;
    }

    static double chiSquare(double[] h1, double[] h2) {
        double s = 0;
        for (int k = 0; k < h1.length; k++) {
            double a = h1[k], b = h2[k], d = a + b;
            if (d > 0) s += (a - b) * (a - b) / d;
        }
        return 0.5 * s;
    }

    static BufferedImage render(boolean[][] m, Box b, List<P> pts, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) img.setRGB(x, y, Color.BLACK.getRGB());
        for (int y = 0; y < m.length; y++) for (int x = 0; x < m[0].length; x++) if (m[y][x]) img.setRGB(x, y, Color.WHITE.getRGB());
        for (int x = b.x1; x <= b.x2; x++) { if (b.y1 >= 0 && b.y1 < h) img.setRGB(x, b.y1, Color.GREEN.getRGB()); if (b.y2 >= 0 && b.y2 < h) img.setRGB(x, b.y2, Color.GREEN.getRGB()); }
        for (int y = b.y1; y <= b.y2; y++) { if (b.x1 >= 0 && b.x1 < w) img.setRGB(b.x1, y, Color.GREEN.getRGB()); if (b.x2 >= 0 && b.x2 < w) img.setRGB(b.x2, y, Color.GREEN.getRGB()); }
        for (P p : pts) img.setRGB(p.x, p.y, Color.RED.getRGB());
        return img;
    }

    static double dist(P a, P b) { return Math.hypot(a.x - b.x, a.y - b.y); }
    static double norm(double a) { a %= 2 * Math.PI; return a < 0 ? a + 2 * Math.PI : a; }

    static class P { final int x, y; P(int x, int y) { this.x = x; this.y = y; } }
    static class Box { final int x1, y1, x2, y2, area; Box(int x1, int y1, int x2, int y2, int area) { this.x1 = x1; this.y1 = y1; this.x2 = x2; this.y2 = y2; this.area = area; } }
}