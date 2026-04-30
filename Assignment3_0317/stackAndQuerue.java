import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Assignment 整合版
 * 1) Stack operations + time complexity
 * 2) Rooted Tree、Binary Tree
 * 3) Tree Traversal (Preorder/Inorder/Postorder)
 * 4) Divide and Conquer (Merge Sort、Binary Search)
 * 5) DFS vs BFS
 */
public class stackAndQuerue {

    // =========================
    // Assignment 1: Stack
    // =========================
    static class IntStack {
        private final int[] data;
        private int top;

        public IntStack(int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("capacity must be > 0");
            }
            this.data = new int[capacity];
            this.top = -1;
        }

        public void push(int value) {
            if (isFull()) {
                throw new IllegalStateException("Stack overflow");
            }
            data[++top] = value;
        }

        public int pop() {
            if (isEmpty()) {
                throw new IllegalStateException("Stack underflow");
            }
            return data[top--];
        }

        public int peek() {
            if (isEmpty()) {
                throw new IllegalStateException("Stack is empty");
            }
            return data[top];
        }

        public boolean isEmpty() {
            return top == -1;
        }

        public boolean isFull() {
            return top == data.length - 1;
        }

        public int size() {
            return top + 1;
        }

        public String snapshot() {
            if (isEmpty()) return "[]";
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i <= top; i++) {
                sb.append(data[i]);
                if (i < top) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    static class IntQueue {
        private final int[] data;
        private int front;
        private int rear;
        private int size;

        public IntQueue(int capacity) {
            if (capacity <= 0) {
                throw new IllegalArgumentException("capacity must be > 0");
            }
            this.data = new int[capacity];
            this.front = 0;
            this.rear = 0;
            this.size = 0;
        }

        public void enqueue(int value) {
            if (isFull()) {
                throw new IllegalStateException("Queue overflow");
            }
            data[rear] = value;
            rear = (rear + 1) % data.length;
            size++;
        }

        public int dequeue() {
            if (isEmpty()) {
                throw new IllegalStateException("Queue underflow");
            }
            int value = data[front];
            front = (front + 1) % data.length;
            size--;
            return value;
        }

        public int peek() {
            if (isEmpty()) {
                throw new IllegalStateException("Queue is empty");
            }
            return data[front];
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public boolean isFull() {
            return size == data.length;
        }

        public String snapshot() {
            if (isEmpty()) return "[]";
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < size; i++) {
                int idx = (front + i) % data.length;
                sb.append(data[idx]);
                if (i < size - 1) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    // =========================
    // Assignment 2/3: Tree + Traversal
    // =========================
    static class RootedTreeNode {
        int value;
        List<RootedTreeNode> children = new ArrayList<>();

        RootedTreeNode(int value) {
            this.value = value;
        }

        void addChild(RootedTreeNode child) {
            children.add(child);
        }
    }

    static class BinaryTreeNode {
        int value;
        BinaryTreeNode left;
        BinaryTreeNode right;

        BinaryTreeNode(int value) {
            this.value = value;
        }
    }

    static class TreeTraversal {

        // N-ary tree preorder (Root -> children)
        static void preorderRooted(RootedTreeNode root, List<Integer> out) {
            if (root == null) return;
            out.add(root.value);
            for (RootedTreeNode child : root.children) {
                preorderRooted(child, out);
            }
        }

        static void preorder(BinaryTreeNode root, List<Integer> out) {
            if (root == null) return;
            out.add(root.value);
            preorder(root.left, out);
            preorder(root.right, out);
        }

        static void inorder(BinaryTreeNode root, List<Integer> out) {
            if (root == null) return;
            inorder(root.left, out);
            out.add(root.value);
            inorder(root.right, out);
        }

        static void postorder(BinaryTreeNode root, List<Integer> out) {
            if (root == null) return;
            postorder(root.left, out);
            postorder(root.right, out);
            out.add(root.value);
        }

        // Stepwise traversals with depth info
        static void preorderWithSteps(BinaryTreeNode root) {
            System.out.println("\nPreorder (stepwise):");
            preorderWithSteps(root, 0, new int[]{1});
        }

        private static void preorderWithSteps(BinaryTreeNode node, int depth, int[] step) {
            if (node == null) return;
            System.out.println("Step " + (step[0]++) + " - visit " + node.value + " (depth=" + depth + ")");
            preorderWithSteps(node.left, depth + 1, step);
            preorderWithSteps(node.right, depth + 1, step);
        }

        static void inorderWithSteps(BinaryTreeNode root) {
            System.out.println("\nInorder (stepwise):");
            inorderWithSteps(root, 0, new int[]{1});
        }

        private static void inorderWithSteps(BinaryTreeNode node, int depth, int[] step) {
            if (node == null) return;
            inorderWithSteps(node.left, depth + 1, step);
            System.out.println("Step " + (step[0]++) + " - visit " + node.value + " (depth=" + depth + ")");
            inorderWithSteps(node.right, depth + 1, step);
        }

        static void postorderWithSteps(BinaryTreeNode root) {
            System.out.println("\nPostorder (stepwise):");
            postorderWithSteps(root, 0, new int[]{1});
        }

        private static void postorderWithSteps(BinaryTreeNode node, int depth, int[] step) {
            if (node == null) return;
            postorderWithSteps(node.left, depth + 1, step);
            postorderWithSteps(node.right, depth + 1, step);
            System.out.println("Step " + (step[0]++) + " - visit " + node.value + " (depth=" + depth + ")");
        }

        // Rooted tree preorder steps
        static void preorderRootedWithSteps(RootedTreeNode root) {
            System.out.println("\nRooted Tree Preorder (stepwise):");
            preorderRootedWithSteps(root, 0, new int[]{1});
        }

        private static void preorderRootedWithSteps(RootedTreeNode node, int depth, int[] step) {
            if (node == null) return;
            System.out.println("Step " + (step[0]++) + " - visit " + node.value + " (depth=" + depth + ")");
            for (RootedTreeNode child : node.children) {
                preorderRootedWithSteps(child, depth + 1, step);
            }
        }
    }

    // =========================
    // Assignment 4: Divide & Conquer
    // =========================
    static class DivideAndConquer {

        // Merge Sort: T(n) = 2T(n/2) + n
        static void mergeSort(int[] arr) {
            if (arr == null || arr.length <= 1) return;
            int[] temp = new int[arr.length];
            mergeSort(arr, 0, arr.length - 1, temp);
        }

        static void mergeSortWithSteps(int[] arr) {
            if (arr == null || arr.length <= 1) {
                System.out.println("陣列長度 <= 1，不需要排序");
                return;
            }
            int[] temp = new int[arr.length];
            mergeSortWithSteps(arr, 0, arr.length - 1, temp, 1);
        }

        private static void mergeSort(int[] arr, int left, int right, int[] temp) {
            if (left >= right) return;

            int mid = left + (right - left) / 2;
            mergeSort(arr, left, mid, temp);
            mergeSort(arr, mid + 1, right, temp);
            merge(arr, left, mid, right, temp);
        }

            private static int mergeSortWithSteps(int[] arr, int left, int right, int[] temp, int step) {
                if (left >= right) return step;

                int mid = left + (right - left) / 2;
                System.out.println("Step " + step++ + " - 分割: " + subArrayToString(arr, left, right)
                    + " => [" + left + ".." + mid + "] + [" + (mid + 1) + ".." + right + "]");

                step = mergeSortWithSteps(arr, left, mid, temp, step);
                step = mergeSortWithSteps(arr, mid + 1, right, temp, step);

                String leftPart = subArrayToString(arr, left, mid);
                String rightPart = subArrayToString(arr, mid + 1, right);
                merge(arr, left, mid, right, temp);
                System.out.println("Step " + step++ + " - 合併: " + leftPart + " + " + rightPart
                    + " => " + subArrayToString(arr, left, right));
                return step;
            }

        private static void merge(int[] arr, int left, int mid, int right, int[] temp) {
            int i = left;
            int j = mid + 1;
            int k = left;

            while (i <= mid && j <= right) {
                if (arr[i] <= arr[j]) {
                    temp[k++] = arr[i++];
                } else {
                    temp[k++] = arr[j++];
                }
            }

            while (i <= mid) temp[k++] = arr[i++];
            while (j <= right) temp[k++] = arr[j++];

            for (int idx = left; idx <= right; idx++) {
                arr[idx] = temp[idx];
            }
        }

        // Binary Search: T(n) = T(n/2) + 1
        static int binarySearch(int[] arr, int target) {
            return binarySearch(arr, target, 0, arr.length - 1);
        }

        static int binarySearchWithSteps(int[] arr, int target) {
            return binarySearchWithSteps(arr, target, 0, arr.length - 1, 1);
        }

        private static int binarySearch(int[] arr, int target, int left, int right) {
            if (left > right) return -1;

            int mid = left + (right - left) / 2;
            if (arr[mid] == target) return mid;
            if (arr[mid] > target) {
                return binarySearch(arr, target, left, mid - 1);
            }
            return binarySearch(arr, target, mid + 1, right);
        }

        private static int binarySearchWithSteps(int[] arr, int target, int left, int right, int step) {
            if (left > right) {
                System.out.println("Step " + step + " - 範圍為空，查找失敗");
                return -1;
            }

            int mid = left + (right - left) / 2;
            System.out.println("Step " + step + " - 檢查區間 [" + left + ".." + right + "]，mid=" + mid
                    + "，arr[mid]=" + arr[mid]);

            if (arr[mid] == target) return mid;
            if (arr[mid] > target) {
                return binarySearchWithSteps(arr, target, left, mid - 1, step + 1);
            }
            return binarySearchWithSteps(arr, target, mid + 1, right, step + 1);
        }

        private static String subArrayToString(int[] arr, int l, int r) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = l; i <= r; i++) {
                sb.append(arr[i]);
                if (i < r) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    // =========================
    // Assignment 5: DFS vs BFS
    // =========================
    static class Graph {
        private final List<List<Integer>> adj;

        Graph(int n) {
            adj = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                adj.add(new ArrayList<>());
            }
        }

        void addEdge(int u, int v) {
            adj.get(u).add(v);
            adj.get(v).add(u); // 無向圖
        }

        List<Integer> bfs(int start) {
            List<Integer> order = new ArrayList<>();
            boolean[] visited = new boolean[adj.size()];
            LinkedList<Integer> q = new LinkedList<>();

            visited[start] = true;
            q.offer(start);

            while (!q.isEmpty()) {
                int u = q.poll();
                order.add(u);

                for (int v : adj.get(u)) {
                    if (!visited[v]) {
                        visited[v] = true;
                        q.offer(v);
                    }
                }
            }
            return order;
        }

        List<Integer> bfsWithSteps(int start) {
            List<Integer> order = new ArrayList<>();
            boolean[] visited = new boolean[adj.size()];
            int[] level = new int[adj.size()];
            Arrays.fill(level, -1);
            LinkedList<Integer> q = new LinkedList<>();

            visited[start] = true;
            level[start] = 0;
            q.offer(start);

            int step = 1;
            while (!q.isEmpty()) {
                int u = q.poll();
                order.add(u);
                System.out.println("BFS Step " + step++ + " - visit " + u
                        + ", level=" + level[u] + ", queue=" + q);

                for (int v : adj.get(u)) {
                    if (!visited[v]) {
                        visited[v] = true;
                        level[v] = level[u] + 1;
                        q.offer(v);
                        System.out.println("  discover " + v + " (parent=" + u + ") -> queue=" + q);
                    }
                }
            }
            return order;
        }

        List<Integer> dfsRecursive(int start) {
            List<Integer> order = new ArrayList<>();
            boolean[] visited = new boolean[adj.size()];
            dfs(start, visited, order);
            return order;
        }

        private void dfs(int u, boolean[] visited, List<Integer> order) {
            visited[u] = true;
            order.add(u);

            for (int v : adj.get(u)) {
                if (!visited[v]) {
                    dfs(v, visited, order);
                }
            }
        }

        List<Integer> dfsIterative(int start) {
            List<Integer> order = new ArrayList<>();
            boolean[] visited = new boolean[adj.size()];
            ArrayDeque<Integer> stack = new ArrayDeque<>();

            stack.push(start);
            while (!stack.isEmpty()) {
                int u = stack.pop();
                if (visited[u]) continue;

                visited[u] = true;
                order.add(u);

                // 為了讓結果更直觀，逆序 push
                List<Integer> neighbors = adj.get(u);
                for (int i = neighbors.size() - 1; i >= 0; i--) {
                    int v = neighbors.get(i);
                    if (!visited[v]) {
                        stack.push(v);
                    }
                }
            }
            return order;
        }

        List<Integer> dfsIterativeWithSteps(int start) {
            List<Integer> order = new ArrayList<>();
            boolean[] visited = new boolean[adj.size()];
            int[] depth = new int[adj.size()];
            Arrays.fill(depth, -1);
            ArrayDeque<Integer> stack = new ArrayDeque<>();

            stack.push(start);
            depth[start] = 0;
            int step = 1;

            while (!stack.isEmpty()) {
                int u = stack.pop();
                if (visited[u]) continue;

                visited[u] = true;
                order.add(u);
                System.out.println("DFS Step " + step++ + " - visit " + u
                        + ", depth=" + depth[u] + ", stack=" + stack);

                List<Integer> neighbors = adj.get(u);
                for (int i = neighbors.size() - 1; i >= 0; i--) {
                    int v = neighbors.get(i);
                    if (!visited[v]) {
                        if (depth[v] == -1) depth[v] = depth[u] + 1;
                        stack.push(v);
                        System.out.println("  push " + v + " (from " + u + ") -> stack=" + stack);
                    }
                }
            }
            return order;
        }

        void printAdjacencyList() {
            for (int i = 0; i < adj.size(); i++) {
                System.out.println("  " + i + " -> " + adj.get(i));
            }
        }
    }

    // =========================
    // Demo Methods
    // =========================
    private static void printHeader(String title) {
        System.out.println("\n==================================================");
        System.out.println(title);
        System.out.println("==================================================");
    }

    private static void printRootedTree(RootedTreeNode node, String prefix, boolean isLast) {
        if (node == null) return;
        System.out.println(prefix + (isLast ? "└── " : "├── ") + node.value);
        for (int i = 0; i < node.children.size(); i++) {
            boolean childIsLast = i == node.children.size() - 1;
            printRootedTree(node.children.get(i), prefix + (isLast ? "    " : "│   "), childIsLast);
        }
    }

    private static void printBinaryTree(BinaryTreeNode node, String prefix, boolean isLeft) {
        if (node == null) return;
        System.out.println(prefix + (isLeft ? "├── " : "└── ") + node.value);
        printBinaryTree(node.left, prefix + (isLeft ? "│   " : "    "), true);
        printBinaryTree(node.right, prefix + (isLeft ? "│   " : "    "), false);
    }

    private static void runAssignment1() {
        printHeader("Assignment 1: Stack and Queue operations");
        IntStack stack = new IntStack(10);
        IntQueue queue = new IntQueue(10);
        int[] input = {10, 20, 30, 40};

        System.out.println("輸入資料: " + Arrays.toString(input));
        System.out.println("\n[Push 步驟]");
        for (int x : input) {
            stack.push(x);
            System.out.println("push(" + x + ") -> stack = " + stack.snapshot());
        }
        System.out.println("目前棧頂 top = " + stack.peek());

        System.out.println("\n[Pop 步驟 - LIFO]");
        while (!stack.isEmpty()) {
            int popValue = stack.pop();
            System.out.println("pop() = " + popValue + " -> stack = " + stack.snapshot());
        }

        System.out.println("\n[Enqueue 步驟]");
        for (int x : input) {
            queue.enqueue(x);
            System.out.println("enqueue(" + x + ") -> queue = " + queue.snapshot());
        }
        System.out.println("目前隊首 front = " + queue.peek());

        System.out.println("\n[Dequeue 步驟 - FIFO]");
        while (!queue.isEmpty()) {
            int dequeueValue = queue.dequeue();
            System.out.println("dequeue() = " + dequeueValue + " -> queue = " + queue.snapshot());
        }

        System.out.println("\nTime Complexity:");
        System.out.println("- Stack push / pop / peek / isEmpty : O(1)");
        System.out.println("- Queue enqueue / dequeue / peek    : O(1)");
        System.out.println("- 連續操作 n 次                      : O(n)");
    }

    private static void runAssignment2And3() {
        printHeader("Assignment 2 & 3: Tree generation + Traversal");

        // Rooted Tree
        RootedTreeNode r1 = new RootedTreeNode(1);
        RootedTreeNode r2 = new RootedTreeNode(2);
        RootedTreeNode r3 = new RootedTreeNode(3);
        RootedTreeNode r4 = new RootedTreeNode(4);
        RootedTreeNode r5 = new RootedTreeNode(5);
        r1.addChild(r2);
        r1.addChild(r3);
        r2.addChild(r4);
        r2.addChild(r5);

        System.out.println("[Rooted Tree 建立步驟]");
        System.out.println("1) 建立根節點 1");
        System.out.println("2) 1 的子節點加入 2, 3");
        System.out.println("3) 2 的子節點加入 4, 5");
        System.out.println("Rooted Tree 結構:");
        printRootedTree(r1, "", true);

        List<Integer> rootedPre = new ArrayList<>();
        TreeTraversal.preorderRooted(r1, rootedPre);
        System.out.println("Rooted Tree Preorder: " + rootedPre);
        // stepwise
        TreeTraversal.preorderRootedWithSteps(r1);

        // Binary Tree
        BinaryTreeNode b1 = new BinaryTreeNode(1);
        BinaryTreeNode b2 = new BinaryTreeNode(2);
        BinaryTreeNode b3 = new BinaryTreeNode(3);
        BinaryTreeNode b4 = new BinaryTreeNode(4);
        BinaryTreeNode b5 = new BinaryTreeNode(5);
        BinaryTreeNode b6 = new BinaryTreeNode(6);
        BinaryTreeNode b7 = new BinaryTreeNode(7);

        b1.left = b2;
        b1.right = b3;
        b2.left = b4;
        b2.right = b5;
        b3.left = b6;
        b3.right = b7;

        System.out.println("\n[Binary Tree 建立步驟]");
        System.out.println("1) 根節點 1");
        System.out.println("2) 1.left=2, 1.right=3");
        System.out.println("3) 2.left=4, 2.right=5");
        System.out.println("4) 3.left=6, 3.right=7");
        System.out.println("Binary Tree 結構:");
        printBinaryTree(b1, "", false);

        List<Integer> pre = new ArrayList<>();
        List<Integer> in = new ArrayList<>();
        List<Integer> post = new ArrayList<>();

        TreeTraversal.preorder(b1, pre);
        TreeTraversal.inorder(b1, in);
        TreeTraversal.postorder(b1, post);

        System.out.println("\nTraversal 結果:");
        System.out.println("- Preorder  (Root-Left-Right): " + pre);
        System.out.println("- Inorder   (Left-Root-Right): " + in);
        System.out.println("- Postorder (Left-Right-Root): " + post);
        // stepwise traversals
        TreeTraversal.preorderWithSteps(b1);
        TreeTraversal.inorderWithSteps(b1);
        TreeTraversal.postorderWithSteps(b1);
    }

    private static void runAssignment4() {
        printHeader("Assignment 4: Divide and Conquer");

        int[] arr = {8, 3, 6, 1, 9, 2, 7, 5, 4};
        System.out.println("原始陣列: " + Arrays.toString(arr));
        System.out.println("\n[Merge Sort 步驟]");
        DivideAndConquer.mergeSortWithSteps(arr);
        System.out.println("排序結果: " + Arrays.toString(arr));

        int target = 7;
        System.out.println("\n[Binary Search 步驟] target = " + target);
        int idx = DivideAndConquer.binarySearchWithSteps(arr, target);
        System.out.println("查找結果: index = " + idx);

        System.out.println("\nRecurrence:");
        System.out.println("- Merge Sort   : T(n)=2T(n/2)+n => O(n log n)");
        System.out.println("- Binary Search: T(n)=T(n/2)+1 => O(log n)");
    }

    private static void runAssignment5() {
        printHeader("Assignment 5: DFS vs BFS");

        Graph g = new Graph(7);
        System.out.println("圖形建立（無向圖）: ");
        g.addEdge(0, 1);
        System.out.println("- addEdge(0, 1)");
        g.addEdge(0, 2);
        System.out.println("- addEdge(0, 2)");
        g.addEdge(1, 3);
        System.out.println("- addEdge(1, 3)");
        g.addEdge(1, 4);
        System.out.println("- addEdge(1, 4)");
        g.addEdge(2, 5);
        System.out.println("- addEdge(2, 5)");
        g.addEdge(2, 6);
        System.out.println("- addEdge(2, 6)");

        System.out.println("\nAdjacency List:");
        g.printAdjacencyList();

        System.out.println("\n[BFS 過程] 起點 = 0");
        List<Integer> bfsOrder = g.bfsWithSteps(0);

        System.out.println("\n[DFS 過程 (Iterative)] 起點 = 0");
        List<Integer> dfsItrOrder = g.dfsIterativeWithSteps(0);

        List<Integer> dfsRecOrder = g.dfsRecursive(0);

        System.out.println("\nTraversal 從節點 0 開始:");
        System.out.println("- BFS           : " + bfsOrder);
        System.out.println("- DFS (遞迴)    : " + dfsRecOrder);
        System.out.println("- DFS (迭代)    : " + dfsItrOrder);

        System.out.println("\nComplexity (adjacency list):");
        System.out.println("- BFS: O(V+E)");
        System.out.println("- DFS: O(V+E)");
    }

    public static void main(String[] args) {
        System.out.println("============================================");
        System.out.println("   NPU Algorithm - Assignment 1~5 (Java)");
        System.out.println("============================================");

        runAssignment1();
        runAssignment2And3();
        runAssignment4();
        runAssignment5();

        System.out.println("\nAll assignments completed in one program.");
    }
}
