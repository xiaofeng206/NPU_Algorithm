import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import javax.imageio.ImageIO;

class Main {
	private static final int CLASS_COUNT = 2; // foreground/background
	private static final int BINS = 256;

	public static void main(String[] args) throws Exception {
		String inputPath = args.length > 0 ? args[0] : "tiger.jepg";
		File inputFile = resolveInputFile(inputPath);

		BufferedImage image = ImageIO.read(inputFile);
		if (image == null) {
			throw new IOException("無法讀取圖片: " + inputFile.getAbsolutePath());
		}

		int width = image.getWidth();
		int height = image.getHeight();

		// 1) histogram h(i)
		int[] h = new int[BINS];
		int[] intensity = new int[width * height];

		int idx = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int gray = toGray(image.getRGB(x, y));
				intensity[idx++] = gray;
				h[gray]++;
			}
		}

		// 2) cumulative sum P(i), and weighted cumulative sum M(i)
		PrefixStats prefix = buildPrefixStats(h);

		// 3) BFS 搜尋最佳 threshold 組合
		int[] bestThresholds = findBestThresholdsBFS(prefix, CLASS_COUNT);

		// 6) 根據 threshold 做影像分割（foreground/background）
		BufferedImage segmented = segmentForegroundBackground(width, height, intensity, bestThresholds);
		String outputName = "foreground_background.png";
		ImageIO.write(segmented, "png", new File(outputName));

		// 5) 最佳 threshold 組合
		System.out.println("輸入圖片: " + inputFile.getAbsolutePath());
		System.out.println("最佳 threshold 組合: " + Arrays.toString(bestThresholds));
		System.out.println("輸出檔案: " + outputName);

		// 7) 複雜度
		System.out.println("時間複雜度(此版本): " + complexityDescription(BINS, CLASS_COUNT));
	}

	private static File resolveInputFile(String preferred) throws IOException {
		File f = new File(preferred);
		if (f.exists()) {
			return f;
		}

		// 使用者可能輸入 tiger.jepg，若不存在則自動嘗試常見副檔名
		String[] candidates = {"tiger.jpeg", "tiger.jpg", "tiger.jepg"};
		for (String c : candidates) {
			File cf = new File(c);
			if (cf.exists()) {
				return cf;
			}
		}

		throw new IOException("找不到輸入圖片: " + f.getAbsolutePath());
	}

	private static PrefixStats buildPrefixStats(int[] h) {
		double total = 0.0;
		for (int count : h) {
			total += count;
		}
		if (total <= 0.0) {
			throw new IllegalArgumentException("histogram 為空，無法做分割");
		}

		double[] p = new double[BINS];
		double[] P = new double[BINS];
		double[] M = new double[BINS];

		for (int i = 0; i < BINS; i++) {
			p[i] = h[i] / total;
		}

		P[0] = p[0];
		M[0] = 0.0;
		for (int i = 1; i < BINS; i++) {
			P[i] = P[i - 1] + p[i];
			M[i] = M[i - 1] + i * p[i];
		}

		return new PrefixStats(P, M, M[BINS - 1]);
	}

	private static int[] findBestThresholdsBFS(PrefixStats prefix, int classCount) {
		int thresholdCount = classCount - 1;
		if (thresholdCount <= 0) {
			return new int[0];
		}

		Queue<ThresholdState> q = new ArrayDeque<>();
		q.add(new ThresholdState(new int[0], 0));

		double bestScore = -1.0;
		int[] best = evenlySpacedThresholds(classCount);

		while (!q.isEmpty()) {
			ThresholdState s = q.poll();
			int depth = s.thresholds.length;

			if (depth == thresholdCount) {
				// 4) 每個區間用 cumulative sum 算統計量
				double score = evaluateThresholds(prefix, s.thresholds);
				if (score > bestScore) {
					bestScore = score;
					best = s.thresholds;
				}
				continue;
			}

			int remaining = thresholdCount - depth;
			int maxT = (BINS - 2) - (remaining - 1);
			for (int t = s.nextStart; t <= maxT; t++) {
				int[] next = append(s.thresholds, t);
				q.add(new ThresholdState(next, t + 1));
			}
		}

		return best;
	}

	private static double evaluateThresholds(PrefixStats prefix, int[] thresholds) {
		int left = 0;
		double score = 0.0;

		for (int i = 0; i <= thresholds.length; i++) {
			int right = (i < thresholds.length) ? thresholds[i] : (BINS - 1);

			double w = rangeSum(prefix.P, left, right);
			if (w <= 0.0) {
				return -1.0;
			}

			double mean = rangeSum(prefix.M, left, right) / w;
			score += w * square(mean - prefix.globalMean);
			left = right + 1;
		}

		return score;
	}

	private static BufferedImage segmentForegroundBackground(
			int width, int height, int[] intensity, int[] thresholds) {
		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int t = thresholds.length > 0 ? thresholds[0] : 127;

		int i = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int gray = intensity[i++];
				boolean isForeground = gray > t;
				out.setRGB(x, y, (isForeground ? Color.WHITE : Color.BLACK).getRGB());
			}
		}
		return out;
	}

	private static String complexityDescription(int bins, int classCount) {
		int k = classCount - 1;
		return "O(C(" + (bins - 1) + ", " + k + ") * " + classCount
				+ ")，foreground/background 時為 O(" + (bins - 1) + ")";
	}

	private static int[] evenlySpacedThresholds(int classCount) {
		int k = classCount - 1;
		int[] t = new int[k];
		for (int i = 0; i < k; i++) {
			t[i] = (int) Math.round((i + 1) * (255.0 / classCount));
		}
		return t;
	}

	private static int[] append(int[] arr, int value) {
		int[] out = new int[arr.length + 1];
		System.arraycopy(arr, 0, out, 0, arr.length);
		out[arr.length] = value;
		return out;
	}

	private static double rangeSum(double[] prefix, int left, int right) {
		if (left > right) {
			return 0.0;
		}
		return (left == 0) ? prefix[right] : (prefix[right] - prefix[left - 1]);
	}

	private static int toGray(int rgb) {
		int r = (rgb >> 16) & 0xFF;
		int g = (rgb >> 8) & 0xFF;
		int b = rgb & 0xFF;
		return clampToByte((int) Math.round(0.299 * r + 0.587 * g + 0.114 * b));
	}

	private static int clampToByte(int value) {
		return Math.max(0, Math.min(255, value));
	}

	private static double square(double x) {
		return x * x;
	}

	private static class PrefixStats {
		final double[] P;
		final double[] M;
		final double globalMean;

		PrefixStats(double[] P, double[] M, double globalMean) {
			this.P = P;
			this.M = M;
			this.globalMean = globalMean;
		}
	}

	private static class ThresholdState {
		final int[] thresholds;
		final int nextStart;

		ThresholdState(int[] thresholds, int nextStart) {
			this.thresholds = thresholds;
			this.nextStart = nextStart;
		}
	}
}
