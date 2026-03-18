/**
 * 簡單 Stack 類 - 後進先出 (LIFO)
 */
class Stack {
    private int[] arr;
    private int top;
    
    public Stack(int size) {
        arr = new int[size];
        top = -1;
    }
    
    public void push(int val) {
        if (top < arr.length - 1) {
            arr[++top] = val;
        }
    }
    
    public int pop() {
        if (top >= 0) {
            return arr[top--];
        }
        return -1;
    }
    
    public boolean isEmpty() {
        return top == -1;
    }
    
    public int peek() {
        return (top >= 0) ? arr[top] : -1;
    }
}

/**
 * 簡單 Queue 類 - 先進先出 (FIFO)
 */
class Queue {
    private int[] arr;
    private int front;
    private int rear;
    
    public Queue(int size) {
        arr = new int[size];
        front = 0;
        rear = -1;
    }
    
    public void enqueue(int val) {
        if (rear < arr.length - 1) {
            arr[++rear] = val;
        }
    }
    
    public int dequeue() {
        if (front <= rear) {
            return arr[front++];
        }
        return -1;
    }
    
    public boolean isEmpty() {
        return front > rear;
    }
    
    public int peek() {
        return (front <= rear) ? arr[front] : -1;
    }
}

/**
 * 主程式 - 簡單排序演示
 */
public class stackAndQuerue {
    
    // 使用 Stack 進行反轉排序
    static void sortByStack(int[] arr) {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║   用 STACK 進行反轉排序         ║");
        System.out.println("╚════════════════════════════════╝");
        
        System.out.print("\n📥 原始陣列: ");
        printArray(arr);
        
        Stack stack = new Stack(arr.length);
        System.out.println("\n步驟1️⃣  - 將所有元素 push 到 Stack:");
        for (int num : arr) {
            stack.push(num);
            System.out.print("  [" + num + "]→ ");
        }
        System.out.println("(棧頂)");
        
        System.out.println("\n步驟2️⃣  - 將元素依次 pop 出來 (後進先出):");
        int[] result = new int[arr.length];
        int idx = 0;
        while (!stack.isEmpty()) {
            result[idx] = stack.pop();
            System.out.print("  pop(" + result[idx] + ") ");
            idx++;
        }
        System.out.println();
        
        System.out.print("\n📤 反轉後的陣列: ");
        printArray(result);
    }
    
    // 使用 Queue 進行順序排序
    static void sortByQueue(int[] arr) {
        Queue queue = new Queue(arr.length);
        
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║   用 QUEUE 進行順序排序         ║");
        System.out.println("╚════════════════════════════════╝");
        
        System.out.print("\n📥 原始陣列: ");
        printArray(arr);
        
        System.out.println("\n步驟1️⃣  - 將所有元素 enqueue 到 Queue:");
        for (int num : arr) {
            queue.enqueue(num);
            System.out.print("  [" + num + "]→ ");
        }
        System.out.println("(隊尾)");
        
        System.out.println("\n步驟2️⃣  - 將元素依次 dequeue 出來 (先進先出):");
        int[] result = new int[arr.length];
        int idx = 0;
        while (!queue.isEmpty()) {
            result[idx] = queue.dequeue();
            System.out.print("  dequeue(" + result[idx] + ") ");
            idx++;
        }
        System.out.println();
        
        System.out.print("\n📤 輸出順序: ");
        printArray(result);
    }

    
    // 列印陣列
    static void printArray(int[] arr) {
        System.out.print("[");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1) System.out.print(", ");
        }
        System.out.println("]");
    }
    
    public static void main(String[] args) {
        System.out.println("\n" +
            "███████████████████████████████████████\n" +
            "    Stack 與 Queue 簡單排序演示\n" +
            "███████████████████████████████████████");
        
        int[] arr1 = {5, 2, 8, 1, 9};
        int[] arr2 = {5, 2, 8, 1, 9};
        
        // 演示1: Stack 反轉
        sortByStack(arr1);
        
        // 演示2: Queue 順序
        sortByQueue(arr2);
        
        // 時間複雜度統計
        System.out.println("\n" +
            "═══════════════════════════════════════\n" +
            "          Time Complexity\n" +
            "═══════════════════════════════════════");
        
        System.out.println("\nSTACK (n = 元素個數):");
        System.out.println("  push():");
        System.out.println("    最佳: O(1)  平均: O(1)  最壞: O(1)");
        System.out.println("  pop():");
        System.out.println("    最佳: O(1)  平均: O(1)  最壞: O(1)");
        System.out.println("  反轉 n 個元素:");
        System.out.println("    最佳: O(n)  平均: O(n)  最壞: O(n)");
        
        System.out.println("\nQUEUE (n = 元素個數):");
        System.out.println("  enqueue():");
        System.out.println("    最佳: O(1)  平均: O(1)  最壞: O(1)");
        System.out.println("  dequeue():");
        System.out.println("    最佳: O(1)  平均: O(1)  最壞: O(1)");
        System.out.println("  順序輸出 n 個元素:");
        System.out.println("    最佳: O(n)  平均: O(n)  最壞: O(n)");
        
        System.out.println("\n═══════════════════════════════════════\n");
    }
}
