import java.util.Arrays;

public class Quick {
	private static int step = 1;

	public static void quickSort(int[] arr, int low, int high) {
		if (low < high) {
			int pivotIndex = partition(arr, low, high);
			quickSort(arr, low, pivotIndex - 1);
			quickSort(arr, pivotIndex + 1, high);
		}
	}

	private static int partition(int[] arr, int low, int high) {
		int pivot = arr[high];
		int i = low - 1;

		System.out.println("\nStep " + (step++) + ": partition range [" + low + ", " + high + "] pivot=" + pivot);
		System.out.println("  Before: " + Arrays.toString(arr));

		for (int j = low; j < high; j++) {
			if (arr[j] <= pivot) {
				i++;
				swap(arr, i, j);
				System.out.println("  arr[" + j + "] <= pivot, swap index " + i + " and " + j + " -> " + Arrays.toString(arr));
			} else {
				System.out.println("  arr[" + j + "] > pivot, no swap -> " + Arrays.toString(arr));
			}
		}

		swap(arr, i + 1, high);
		System.out.println("  Place pivot -> " + Arrays.toString(arr));
		return i + 1;
	}

	private static void swap(int[] arr, int i, int j) {
		int temp = arr[i];
		arr[i] = arr[j];
		arr[j] = temp;
	}

	public static void main(String[] args) {
		int[] data = {38, 27, 43, 3, 9, 82, 10};

		System.out.println("Original array: " + Arrays.toString(data));
		quickSort(data, 0, data.length - 1);
		System.out.println("Sorted array:   " + Arrays.toString(data));

		System.out.println("Time Complexity: O(n log n) (Best/Average), O(n^2) (Worst)");
		System.out.println("Space Complexity: O(log n) average recursion stack, O(n) worst");
	}
}
