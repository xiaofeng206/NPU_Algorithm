import java.util.Arrays;

public class Insertion {
	public static void insertionSort(int[] arr) {
		int step = 1;

		for (int i = 1; i < arr.length; i++) {
			int key = arr[i];
			int j = i - 1;

			System.out.println("\nStep " + (step++) + ": insert key=" + key + " at index " + i);
			System.out.println("  Before: " + Arrays.toString(arr));

			while (j >= 0 && arr[j] > key) {
				arr[j + 1] = arr[j];
				j--;
				System.out.println("  Shift -> " + Arrays.toString(arr));
			}

			arr[j + 1] = key;
			System.out.println("  Place key -> " + Arrays.toString(arr));
		}
	}

	public static void main(String[] args) {
		int[] data = {38, 27, 43, 3, 9, 82, 10};

		System.out.println("Original array: " + Arrays.toString(data));
		insertionSort(data);
		System.out.println("Sorted array:   " + Arrays.toString(data));

		System.out.println("Time Complexity: O(n) (Best), O(n^2) (Average/Worst)");
		System.out.println("Space Complexity: O(1)");
	}
}
