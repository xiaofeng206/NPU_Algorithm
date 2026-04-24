import java.util.Random;

public class sortComplexity {

    public static void main(String[] args) {
        int n = 10000; // 你可以修改這個數字來觀察時間變化
        int[] data = generateData(n);

        System.out.println("測試資料量 n = " + n);
        System.out.println("==============================");

        // 1. 測量 Linear Scan (O(n))
        long start = System.nanoTime();
        long linearSum = doLinearScan(data);
        long linearTime = System.nanoTime() - start;
        printResult("Linear Scan", "O(n)", linearTime);
        System.out.println("Linear Scan 加總結果 = " + linearSum);
        System.out.println("------------------------------");

        // 2. 測量 Insertion Sort (O(n^2))
        int[] dataForInsertion = data.clone();
        start = System.nanoTime();
        doInsertionSort(dataForInsertion);
        long insertionTime = System.nanoTime() - start;
        printResult("Insertion Sort", "O(n^2)", insertionTime);

        // 3. 測量 Merge Sort (O(n log n))
        int[] dataForMerge = data.clone();
        start = System.nanoTime();
        doMergeSort(dataForMerge, 0, n - 1);
        long mergeTime = System.nanoTime() - start;
        printResult("Merge Sort", "O(n log n)", mergeTime);
    }

    public static void printResult(String algorithmName, String timeComplexity, long elapsedTime) {
        System.out.println(algorithmName + ":");
        System.out.println("Time Complexity = " + timeComplexity);
        System.out.println("實際耗時 = " + elapsedTime + " ns");
        System.out.println("------------------------------");
    }

    // --- 演算法實作 ---

    // O(n): 只跑一個迴圈
    public static long doLinearScan(int[] arr) {
        long sum = 0;
        for (int i = 0; i < arr.length; i++) {
            sum += arr[i];
        }
        return sum;
    }

    // O(n^2): 雙重迴圈，把小的數字往左邊插
    public static void doInsertionSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int target = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > target) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = target;
        }
    }

    // O(n log n): 用「切一半」的方式遞迴處理 (Merge Sort)
    public static void doMergeSort(int[] arr, int left, int right) {
        if (left >= right) return;
        int mid = (left + right) / 2;
        doMergeSort(arr, left, mid);
        doMergeSort(arr, mid + 1, right);
        merge(arr, left, mid, right);
    }

    private static void merge(int[] arr, int left, int mid, int right) {
        int[] temp = new int[right - left + 1];
        int i = left, j = mid + 1, k = 0;
        while (i <= mid && j <= right) {
            temp[k++] = (arr[i] < arr[j]) ? arr[i++] : arr[j++];
        }
        while (i <= mid) temp[k++] = arr[i++];
        while (j <= right) temp[k++] = arr[j++];
        for (int p = 0; p < temp.length; p++) arr[left + p] = temp[p];
    }

    // 輔助工具：產生隨機數字
    public static int[] generateData(int n) {
        int[] arr = new int[n];
        Random r = new Random();
        for (int i = 0; i < n; i++) arr[i] = r.nextInt(1000);
        return arr;
    }
}