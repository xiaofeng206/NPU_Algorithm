import java.util.Arrays;

public class Bubble {
	public static void bubbleSort(int[] arr) {
		int n = arr.length;
		int step = 1;

		for (int i = 0; i < n - 1; i++) {
			boolean swapped = false;
			System.out.println("\nPass " + (i + 1) + ":");

			for (int j = 0; j < n - 1 - i; j++) {
				System.out.println("  Step " + (step++) + " compare arr[" + j + "]=" + arr[j] + " and arr[" + (j + 1) + "]=" + arr[j + 1]);
				if (arr[j] > arr[j + 1]) {
					int temp = arr[j];
					arr[j] = arr[j + 1];
					arr[j + 1] = temp;
					swapped = true;
					System.out.println("    Swap -> " + Arrays.toString(arr));
				} else {
					System.out.println("    Keep -> " + Arrays.toString(arr));
				}
			}

			if (!swapped) {
				System.out.println("  No swap this pass, array already sorted.");
				break;
			}
		}
	}

	public static void main(String[] args) {
		int[] data = {38, 27, 43, 3, 9, 82, 10};

		System.out.println("Original array: " + Arrays.toString(data));
		bubbleSort(data);
		System.out.println("Sorted array:   " + Arrays.toString(data));

		System.out.println("Time Complexity: O(n) (Best), O(n^2) (Average/Worst)");
		System.out.println("Space Complexity: O(1)");
	}
}
