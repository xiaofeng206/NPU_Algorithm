class Main {

	public static void main(String[] args) {
		// Hamming Distance demo
		String s1 = "karolin";
		String s2 = "kathrin";
		int distance = hammingDistance(s1, s2);

		System.out.println("Hamming Distance");
		System.out.println("String 1: " + s1);
		System.out.println("String 2: " + s2);
		System.out.println("Distance: " + distance);
		System.out.println("Time Complexity: O(n)");
		System.out.println("Space Complexity: O(1)");
		System.out.println();

		// Integral Image demo
		int[][] image = {
			{1, 2, 3},
			{4, 5, 6},
			{7, 8, 9}
		};

		int[][] integral = buildIntegralImage(image);
		int regionSum = querySum(integral, 0, 0, 1, 1);

		System.out.println("Integral Image");
		System.out.println("Original Image:");
		printMatrix(image);
		System.out.println("Integral Image:");
		printMatrix(integral);
		System.out.println("Sum of region (0,0) to (1,1): " + regionSum);
		System.out.println("Build Time Complexity: O(rows * cols)");
		System.out.println("Build Space Complexity: O(rows * cols)");
		System.out.println("Query Time Complexity: O(1)");
	}

	public static int hammingDistance(String a, String b) {
		if (a == null || b == null) {
			throw new IllegalArgumentException("Inputs must not be null.");
		}
		if (a.length() != b.length()) {
			throw new IllegalArgumentException("Strings must have the same length.");
		}

		int distance = 0;
		for (int i = 0; i < a.length(); i++) {
			if (a.charAt(i) != b.charAt(i)) {
				distance++;
			}
		}
		return distance;
	}

	public static int[][] buildIntegralImage(int[][] image) {
		validateMatrix(image);

		int rows = image.length;
		int cols = image[0].length;
		int[][] integral = new int[rows][cols];

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				int above = (r > 0) ? integral[r - 1][c] : 0;
				int left = (c > 0) ? integral[r][c - 1] : 0;
				int aboveLeft = (r > 0 && c > 0) ? integral[r - 1][c - 1] : 0;
				integral[r][c] = image[r][c] + above + left - aboveLeft;
			}
		}

		return integral;
	}

	public static int querySum(int[][] integral, int top, int left, int bottom, int right) {
		validateMatrix(integral);

		if (top < 0 || left < 0 || bottom < top || right < left) {
			throw new IllegalArgumentException("Invalid region coordinates.");
		}
		if (bottom >= integral.length || right >= integral[0].length) {
			throw new IllegalArgumentException("Region is out of bounds.");
		}

		int total = integral[bottom][right];
		int above = (top > 0) ? integral[top - 1][right] : 0;
		int leftSide = (left > 0) ? integral[bottom][left - 1] : 0;
		int aboveLeft = (top > 0 && left > 0) ? integral[top - 1][left - 1] : 0;

		return total - above - leftSide + aboveLeft;
	}

	private static void validateMatrix(int[][] matrix) {
		if (matrix == null || matrix.length == 0 || matrix[0] == null || matrix[0].length == 0) {
			throw new IllegalArgumentException("Matrix must not be null or empty.");
		}
		int cols = matrix[0].length;
		for (int r = 1; r < matrix.length; r++) {
			if (matrix[r] == null || matrix[r].length != cols) {
				throw new IllegalArgumentException("Matrix must be rectangular.");
			}
		}
	}

	private static void printMatrix(int[][] matrix) {
		for (int[] row : matrix) {
			for (int value : row) {
				System.out.print(value + " ");
			}
			System.out.println();
		}
	}
}
