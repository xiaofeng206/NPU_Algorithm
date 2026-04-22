import java.util.*;

// Advanced 類別：包含區間排程的貪心演算法與加權動態規劃實作
public class Advanced {

    // Interval 類別：表示一個區間 (id, start, finish, weight)
    static class Interval implements Comparable<Interval> {
        int id;      // 區間識別碼
        int start;   // 開始時間
        int finish;  // 結束時間
        int weight;  // 權重（用於加權排程）

        // 建構子：初始化區間屬性
        Interval(int id, int start, int finish, int weight) {
            this.id = id;
            this.start = start;
            this.finish = finish;
            this.weight = weight;
        }

        // 依據 finish 時間排序（升冪）: 用於貪心選擇與 DP 前置排序
        @Override
        public int compareTo(Interval o) {
            return Integer.compare(this.finish, o.finish);
        }

        @Override
        public String toString() {
            return String.format("[%d: %d-%d w=%d]", id, start, finish, weight);
        }
    }

    // ScheduleResult：封裝排程結果（被選到的區間、總權重、與時間複雜度說明）
    static class ScheduleResult {
        List<Interval> selected; // 被選中的區間列表（按 finish 排序）
        long totalWeight;        // 選中區間的權重總和
        String complexity;       // 描述時間複雜度的字串

        ScheduleResult(List<Interval> selected, long totalWeight, String complexity) {
            this.selected = selected;
            this.totalWeight = totalWeight;
            this.complexity = complexity;
        }
    }

    // ===== 貪心演算法（無權重） =====
    // 選擇依據：最早結束時間優先（Earliest Finish Time）
    // 輸入：一個區間列表（會被排序）
    // 輸出：ScheduleResult 包含被選中的區間與總權重（若提供權重則仍會計算）
    public static ScheduleResult greedyIntervalScheduling(List<Interval> intervals) {
        // 以 finish 時間排序，耗費 O(n log n)
        Collections.sort(intervals);
        List<Interval> result = new ArrayList<>();
        int lastFinish = Integer.MIN_VALUE; // 上一個選到的區間結束時間
        for (Interval iv : intervals) {
            // 若區間 iv 開始時間不與已選區間重疊，則選取之
            if (iv.start >= lastFinish) {
                result.add(iv);
                lastFinish = iv.finish;
            }
        }
        // 計算被選中區間的權重總和（方便比較）
        long total = 0;
        for (Interval iv : result) total += iv.weight;
        return new ScheduleResult(result, total, "O(n log n) (sorting) + O(n) selection");
    }

    // ===== 加權區間排程（Weighted Interval Scheduling） =====
    // 使用動態規劃 + binary search 計算 p(j)（最後一個不衝突的區間），時間複雜度 O(n log n)
    public static ScheduleResult weightedIntervalScheduling(List<Interval> intervals) {
        // 先依 finish 排序（以便 binary search 找到相容的前一個區間）
        Collections.sort(intervals);
        int n = intervals.size();

        // p[j]（1-based）：對於第 j 個區間（排序後），p[j] 為最後一個不衝突的區間索引（若無則為 0）
        int[] p = new int[n + 1];
        for (int j = 1; j <= n; j++) {
            p[j] = binarySearchLastNonConflicting(intervals, j);
        }

        // M[j] 儲存考慮前 j 個區間時的最優總權重（M[0] = 0）
        long[] M = new long[n + 1];
        M[0] = 0;
        for (int j = 1; j <= n; j++) {
            long incl = intervals.get(j - 1).weight + M[p[j]]; // 選第 j 個區間
            long excl = M[j - 1]; // 不選第 j 個區間
            M[j] = Math.max(incl, excl);
        }

        // 重建選擇的區間集合
        List<Interval> selected = new ArrayList<>();
        findSolution(intervals, p, M, n, selected);
        Collections.reverse(selected); // 目前結果為反向，翻轉回正序
        long optimal = M[n];
        return new ScheduleResult(selected, optimal, "O(n log n) (sorting + binary search for p) + O(n) DP");
    }

    // binarySearchLastNonConflicting：對於排序後的第 j 個區間，找最後一個 finish <= start_j 的區間
    // 回傳 1-based 索引（若無則回傳 0）
    private static int binarySearchLastNonConflicting(List<Interval> intervals, int j) {
        int low = 0; // 0-based
        int high = j - 2; // 在 intervals[0..j-2] 搜尋
        int res = -1;
        int target = intervals.get(j - 1).start; // 第 j 個區間的開始時間
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (intervals.get(mid).finish <= target) {
                res = mid; // mid 為一個可行不衝突的候選
                low = mid + 1; // 向右找更靠近 j 的候選
            } else {
                high = mid - 1;
            }
        }
        return res + 1; // 轉為 1-based（res=-1 -> 回傳 0 表示沒有相容區間）
    }

    // findSolution：遞迴重建 DP 的最優解集合
    // 使用 M 與 p：若包含 j 的權重 (w_j + M[p[j]]) > M[j-1]，則選 j，並繼續走到 p[j]
    private static void findSolution(List<Interval> intervals, int[] p, long[] M, int j, List<Interval> selected) {
        if (j == 0) return; // 沒有區間可選
        long incl = intervals.get(j - 1).weight + M[p[j]];
        if (incl > M[j - 1]) {
            selected.add(intervals.get(j - 1));
            findSolution(intervals, p, M, p[j], selected);
        } else {
            findSolution(intervals, p, M, j - 1, selected);
        }
    }

    // 計算一組區間的總權重（未被使用但保留作為工具函式）
    private static int totalWeight(List<Interval> list) {
        int s = 0;
        for (Interval iv : list) s += iv.weight;
        return s;
    }

    // printSchedule：以整齊格式列印一個 ScheduleResult 的內容（標題、複雜度、每個區間、總權重）
    private static void printSchedule(String title, ScheduleResult res) {
        System.out.println("--- " + title + " ---");
        System.out.println("Time complexity: " + res.complexity);
        if (res.selected.isEmpty()) {
            System.out.println("(no intervals selected)");
        } else {
            for (Interval iv : res.selected) {
                System.out.printf("- id=%d: %d..%d  weight=%d%n", iv.id, iv.start, iv.finish, iv.weight);
            }
        }
        System.out.println("Total weight = " + res.totalWeight);
    }

    // compareSchedules：比較貪心與 DP 的結果，列出共同、僅在貪心、僅在 DP 的區間，並顯示權重差異
    private static void compareSchedules(ScheduleResult greedy, ScheduleResult dp) {
        System.out.println();
        System.out.println("--- Comparison: Greedy vs DP ---");
        Set<Integer> gIds = new HashSet<>();
        for (Interval iv : greedy.selected) gIds.add(iv.id);
        Set<Integer> dIds = new HashSet<>();
        for (Interval iv : dp.selected) dIds.add(iv.id);

        Set<Integer> both = new TreeSet<>(gIds);
        both.retainAll(dIds);
        Set<Integer> onlyG = new TreeSet<>(gIds);
        onlyG.removeAll(dIds);
        Set<Integer> onlyD = new TreeSet<>(dIds);
        onlyD.removeAll(gIds);

        System.out.println("Common intervals (both): " + both);
        System.out.println("Only greedy: " + onlyG);
        System.out.println("Only DP: " + onlyD);

        System.out.println();
        System.out.println("Greedy total weight = " + greedy.totalWeight);
        System.out.println("DP total weight     = " + dp.totalWeight);
        if (greedy.totalWeight == dp.totalWeight) {
            System.out.println("Result: equal total weight");
        } else if (dp.totalWeight > greedy.totalWeight) {
            System.out.println("Result: DP is better by " + (dp.totalWeight - greedy.totalWeight));
        } else {
            System.out.println("Result: Greedy is better by " + (greedy.totalWeight - dp.totalWeight));
        }
    }

    // main 示範：包含一組會讓貪心與 DP 結果不同的範例
    public static void main(String[] args) {
        // 範例資料（每個區間：id, start, finish, weight）
        List<Interval> demoIntervals = Arrays.asList(
            new Interval(1, 1, 2, 1),
            new Interval(2, 2, 3, 1),
            new Interval(3, 3, 4, 1),
            new Interval(4, 4, 5, 1),
            new Interval(5, 1, 5, 5), // 長期但權重較大
            new Interval(6, 5, 7, 2),
            new Interval(7, 6, 9, 4)
        );

        // 使用貪心與 DP 分別求解並列印結果與比較
        ScheduleResult greedyRes = greedyIntervalScheduling(new ArrayList<>(demoIntervals));
        printSchedule("Greedy Unweighted Interval Scheduling", greedyRes);
        System.out.println();
        ScheduleResult weightedRes = weightedIntervalScheduling(new ArrayList<>(demoIntervals));
        printSchedule("Weighted Interval Scheduling (DP)", weightedRes);

        compareSchedules(greedyRes, weightedRes);
    }
}
