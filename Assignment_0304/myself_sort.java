import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class myself_sort {

	public static List<Integer> jumpPivotSort(List<Integer> arr) {
		return jumpPivotSort(arr, 0);
	}

	private static List<Integer> jumpPivotSort(List<Integer> arr, int depth) {
		// 基本情況：如果長度小於等於 1，直接回傳
		if (arr.size() <= 1) {
			printIndent(depth);
			System.out.println("完成: " + arr);
			return new ArrayList<>(arr);
		}

		// 1. 獨特的樞紐選取法 (Jump selection)
		int n = arr.size();
		int jumpStep = Math.max(1, (int) Math.sqrt(n));

		printIndent(depth);
		System.out.println("目前: " + arr + " | step=" + jumpStep);

		List<Integer> samples = new ArrayList<>();
		for (int i = 0; i < n; i += jumpStep) {
			samples.add(arr.get(i));
		}

		printIndent(depth);
		System.out.println("步驟 " + depth + ": 取樣元素 = " + samples);

		double pivot = 0.0;
		for (int value : samples) {
			pivot += value;
		}
		pivot /= samples.size();

		printIndent(depth);
		System.out.println("樞紐: " + pivot);

		// 2. 分割子數列
		List<Integer> left = new ArrayList<>();
		List<Integer> middle = new ArrayList<>();
		List<Integer> right = new ArrayList<>();

		for (int x : arr) {
			if (x < pivot) {
				left.add(x);
			} else if (x > pivot) {
				right.add(x);
			} else {
				middle.add(x);
			}
		}

		printIndent(depth);
		System.out.println("分割: L=" + left + ", M=" + middle + ", R=" + right);

		// 這裡處理一個邊界情況：如果所有元素都一樣，避免無限遞迴
		if (left.isEmpty() && right.isEmpty()) {
			printIndent(depth);
			System.out.println("完成: " + middle);
			return middle;
		}

		// 3. 遞迴排序並合併
		List<Integer> result = new ArrayList<>();
		result.addAll(jumpPivotSort(left, depth + 1));
		result.addAll(middle);
		result.addAll(jumpPivotSort(right, depth + 1));

		printIndent(depth);
		System.out.println("合併: " + result);
		return result;
	}

	private static void printIndent(int depth) {
		for (int i = 0; i < depth; i++) {
			System.out.print("  ");
		}
	}

	public static void main(String[] args) {
		List<Integer> data = Arrays.asList(29, 10, 14, 37, 13, 2, 25, 8, 19);
		List<Integer> sortedData = jumpPivotSort(data);

		System.out.println("原始數列: " + data);
		System.out.println("排序後:   " + sortedData);
		System.out.println();
		System.out.println("時間複雜度分析：");
		System.out.println("最佳情況：O(n log n)");
		System.out.println("平均情況：O(n log n)");
		System.out.println("最壞情況：O(n^2)");
		System.out.println("說明：每次都要掃描整個陣列做分割，若每次 pivot 都能大致平均分成兩半，遞迴層數約為 log n，因此是 O(n log n)；若 pivot 很差，造成一邊幾乎不縮小，就會退化成 O(n^2)。");
	}
}
