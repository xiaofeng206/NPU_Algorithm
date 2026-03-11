import java.util.Arrays;

public class Merge {
	private static int step = 1;

	public static void mergeSort(int[] arr, int left, int right) {
		if (left >= right) {
			return;
		}

		int mid = left + (right - left) / 2;
		mergeSort(arr, left, mid);
		mergeSort(arr, mid + 1, right);
		merge(arr, left, mid, right);
	}

	private static void merge(int[] arr, int left, int mid, int right) {
		int n1 = mid - left + 1;
		int n2 = right - mid;

		int[] leftPart = new int[n1];
		int[] rightPart = new int[n2];

		for (int i = 0; i < n1; i++) {
			leftPart[i] = arr[left + i];
		}
		for (int j = 0; j < n2; j++) {
			rightPart[j] = arr[mid + 1 + j];
		}

		System.out.println("\nStep " + (step++) + ": merge range [" + left + ", " + right + "]");
		System.out.println("  Left : " + Arrays.toString(leftPart));
		System.out.println("  Right: " + Arrays.toString(rightPart));

		int i = 0;
		int j = 0;
		int k = left;

		while (i < n1 && j < n2) {
			if (leftPart[i] <= rightPart[j]) {
				arr[k++] = leftPart[i++];
			} else {
				arr[k++] = rightPart[j++];
			}
		}

		while (i < n1) {
			arr[k++] = leftPart[i++];
		}

		while (j < n2) {
			arr[k++] = rightPart[j++];
		}

		System.out.println("  Merged result: " + Arrays.toString(Arrays.copyOfRange(arr, left, right + 1)));
		System.out.println("  Array status : " + Arrays.toString(arr));
	}

	public static void main(String[] args) {
		int[] data = {38, 27, 43, 3, 9, 82, 10};

		System.out.println("Original array: " + Arrays.toString(data));
		mergeSort(data, 0, data.length - 1);
		System.out.println("Sorted array:   " + Arrays.toString(data));

		System.out.println("Time Complexity: O(n log n) (Best / Average / Worst)");
		System.out.println("Space Complexity: O(n)");
	}
}
 