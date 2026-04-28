import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;

// Huffman 編碼主類別：負責建樹、編碼、解碼與結果輸出
public class Huffman {

	// 樹節點：葉節點存字元，內部節點以 '\0' 表示
	private static class Node implements Comparable<Node> {
		// 字元（葉節點使用）
		char ch;
		// 頻率
		int freq;
		// 左右子樹
		Node left;
		Node right;

		// 建立葉節點
		Node(char ch, int freq) {
			this.ch = ch;
			this.freq = freq;
		}

		// 建立內部節點
		Node(int freq, Node left, Node right) {
			this.ch = '\0';
			this.freq = freq;
			this.left = left;
			this.right = right;
		}

		// 是否為葉節點
		boolean isLeaf() {
			return left == null && right == null;
		}

		// 讓 PriorityQueue 依頻率由小到大排序
		@Override
		public int compareTo(Node other) {
			return Integer.compare(this.freq, other.freq);
		}
	}

	// Huffman 樹根節點
	private Node root;
	// 字元 -> Huffman 編碼
	private Map<Character, String> codes;
	// 字元 -> 出現頻率
	private Map<Character, Integer> frequencies;

	// 建構子：初始化資料結構
	public Huffman() {
		this.root = null;
		this.codes = new HashMap<>();
		this.frequencies = new HashMap<>();
	}

	// 對輸入文字建立完整 Huffman 結構（頻率表、樹、編碼表）
	public void build(String text) {
		if (text == null || text.isEmpty()) {
			throw new IllegalArgumentException("Input text must not be null or empty.");
		}

		this.frequencies = buildFrequencyMap(text);
		this.root = buildTree(this.frequencies);
		this.codes.clear();
		buildCodes(this.root, "");
	}

	// 統計每個字元出現次數
	private Map<Character, Integer> buildFrequencyMap(String text) {
		Map<Character, Integer> freqMap = new HashMap<>();
		for (char c : text.toCharArray()) {
			freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
		}
		return freqMap;
	}

	// 依頻率表建立 Huffman 樹
	private Node buildTree(Map<Character, Integer> freqMap) {
		PriorityQueue<Node> pq = new PriorityQueue<>();

		// 先將所有葉節點放入最小堆
		for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
			pq.offer(new Node(entry.getKey(), entry.getValue()));
		}

		// 特殊情況：只有一種字元時，建立一個父節點包住它
		if (pq.size() == 1) {
			Node only = pq.poll();
			return new Node(only.freq, only, null);
		}

		// 每次取出兩個最小頻率節點合併，再放回堆中
		while (pq.size() > 1) {
			Node left = pq.poll();
			Node right = pq.poll();
			Node merged = new Node(left.freq + right.freq, left, right);
			pq.offer(merged);
		}

		// 最後剩下的節點即為樹根
		return pq.poll();
	}

	// DFS 建立編碼表：左子樹加 0，右子樹加 1
	private void buildCodes(Node node, String code) {
		if (node == null) {
			return;
		}

		if (node.isLeaf()) {
			codes.put(node.ch, code.isEmpty() ? "0" : code);
			return;
		}

		buildCodes(node.left, code + "0");
		buildCodes(node.right, code + "1");
	}

	// 將原始文字轉成 Huffman bit 字串
	public String encode(String text) {
		if (text == null || text.isEmpty()) {
			throw new IllegalArgumentException("Text to encode must not be null or empty.");
		}

		// 若尚未建表，直接用此文字建立
		if (codes.isEmpty()) {
			build(text);
		}

		StringBuilder encoded = new StringBuilder();
		for (char c : text.toCharArray()) {
			String code = codes.get(c);
			if (code == null) {
				throw new IllegalStateException("Character '" + c + "' has no Huffman code.");
			}
			encoded.append(code);
		}

		return encoded.toString();
	}

	// 將 bit 字串依 Huffman 樹還原成文字
	public String decode(String bits) {
		if (bits == null || bits.isEmpty()) {
			throw new IllegalArgumentException("Bit string to decode must not be null or empty.");
		}

		if (root == null) {
			throw new IllegalStateException("Huffman tree is not built. Call build(...) first.");
		}

		StringBuilder decoded = new StringBuilder();
		Node current = root;

		for (int i = 0; i < bits.length(); i++) {
			char bit = bits.charAt(i);

			// 依 bit 走訪樹
			if (bit == '0') {
				current = current.left;
			} else if (bit == '1') {
				current = current.right;
			} else {
				throw new IllegalArgumentException("Invalid bit '" + bit + "' at index " + i + ".");
			}

			if (current == null) {
				throw new IllegalArgumentException("Invalid encoded bit string.");
			}

			// 走到葉節點就輸出字元，並回到根節點繼續
			if (current.isLeaf()) {
				decoded.append(current.ch);
				current = root;
			}
		}

		if (current != root) {
			throw new IllegalArgumentException("Invalid encoded bit string: incomplete final code.");
		}

		return decoded.toString();
	}

	// 印出 Huffman 編碼表（依字元排序）
	public void printCodes() {
		if (codes.isEmpty()) {
			System.out.println("No codes available. Build the Huffman tree first.");
			return;
		}

		System.out.println("字元 Huffman 編碼表：");
		System.out.println("---------------------------------");
		for (Map.Entry<Character, String> entry : new TreeMap<>(codes).entrySet()) {
			System.out.printf("%-8s -> %s%n", displayChar(entry.getKey()), entry.getValue());
		}
		System.out.println("---------------------------------");
	}

	// 印出字元頻率表（依字元排序）
	public void printFrequencyMap() {
		if (frequencies.isEmpty()) {
			System.out.println("尚未建立頻率表，請先 build(...)");
			return;
		}

		System.out.println("字元頻率表：");
		System.out.println("---------------------------------");
		for (Map.Entry<Character, Integer> entry : new TreeMap<>(frequencies).entrySet()) {
			System.out.printf("%-8s -> %d%n", displayChar(entry.getKey()), entry.getValue());
		}
		System.out.println("---------------------------------");
	}

	// 讓特殊字元顯示更易讀
	private static String displayChar(char c) {
		if (c == ' ') {
			return "[空白]";
		}
		if (c == '\n') {
			return "[換行]";
		}
		if (c == '\t') {
			return "[Tab]";
		}
		return String.valueOf(c);
	}

	// 輸出各步驟時間複雜度
	public static void printTimeComplexity(int n, int sigma) {
		System.out.println("時間複雜度分析（Huffman Coding）");
		System.out.println("n = 字串長度, sigma = 不同字元數量");
		System.out.println();

		System.out.println("1) 建立頻率表: O(n)");
		System.out.println("2) 建立最小堆: O(sigma)");
		System.out.println("3) 建 Huffman 樹（合併節點）: O(sigma log sigma)");
		System.out.println("4) DFS 產生編碼表: O(sigma)");
		System.out.println("5) 編碼: O(n)");
		System.out.println("6) 解碼: O(L), L 為編碼後 bit 長度");
		System.out.println();

		System.out.println("整體建樹複雜度: O(n + sigma log sigma)");
		System.out.println("整體編碼複雜度: O(n)");
		System.out.println("整體解碼複雜度: O(L)");
		System.out.println();

		System.out.println("當 n = " + n + ", sigma = " + sigma + " 時：");
		System.out.println("主導項為 sigma log sigma（另加一次 O(n) 掃描）。");
	}

	// 範例主程式
	public static void main(String[] args) {
		String text = "Huffman coding is a data compression algorithm.";

		Huffman huffman = new Huffman();
		huffman.build(text);

		System.out.println("================ Huffman 編碼示範 ================");
		System.out.println("原始字串：" + text);
		System.out.println();

		huffman.printFrequencyMap();
		System.out.println();
		huffman.printCodes();

		String encoded = huffman.encode(text);
		String decoded = huffman.decode(encoded);

		System.out.println();
		System.out.println("編碼結果：");
		System.out.println(encoded);
		System.out.println();
		System.out.println("解碼結果：");
		System.out.println(decoded);
		System.out.println();
		System.out.println("驗證：" + (text.equals(decoded) ? "✅ 解碼正確" : "❌ 解碼錯誤"));

		int sigma = (int) text.chars().distinct().count();
		System.out.println();
		printTimeComplexity(text.length(), sigma);
		System.out.println("===================================================");
	}
}
