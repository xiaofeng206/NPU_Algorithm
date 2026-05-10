import javax.swing.*;
import java.awt.*;

/**
 * main.java
 *
 * 簡要說明：
 * 本檔案實作一個 Red-Black Tree（紅黑樹）資料結構，並提供一個簡單的 Swing
 * GUI 來執行與顯示基本操作（插入、刪除、搜尋、載入範例、清除、驗證）。
 *
 * 設計重點：
 * - 插入時新節點為紅色，呼叫 fixInsert 進行顏色與旋轉調整以恢復紅黑樹性質。
 * - 刪除時若移除的是黑節點，呼叫 fixDelete 處理雙重黑（double-black）情況。
 * - 提供 validate() 方法檢查紅黑樹的基本不變性（root 為黑、無紅父子、黑高一致）。
 */

public class main {

	private static final boolean RED = true;
	private static final boolean BLACK = false;


	/**
	 * Node：紅黑樹節點結構
	 * - key: 節點儲存的整數鍵值
	 * - color: 節點顏色（RED 或 BLACK）
	 * - left/right/parent: 樹的連結
	 */
	private static class Node {
		int key;             // 節點鍵值
		boolean color;      // 節點顏色：RED / BLACK
		Node left;          // 左子節點
		Node right;         // 右子節點
		Node parent;        // 父節點引用

		/**
		 * 建構子：建立新節點，預設為紅色（符合插入演算法約定）
		 */
		Node(int key) {
			this.key = key;
			this.color = RED;
		}
	}

	/**
	 * RedBlackTree：紅黑樹的核心實作
	 *
	 * 提供的功能：
	 * - insert/delete: 變更樹形並在必要時呼叫 fixInsert/fixDelete 恢復性質
	 * - rotateLeft/rotateRight: 基本旋轉操作
	 * - search/inorder/print: 輔助輸出與搜尋功能
	 * - validate: 驗證紅黑樹不變性（開發/除錯用途）
	 */
	private static class RedBlackTree {
		private Node root;

		/**
		 * 插入：把新節點作為紅節點插入（BST 規則），然後呼叫 fixInsert
		 * @param key 要插入的整數鍵
		 */
		public void insert(int key) {
			Node node = new Node(key);
			Node parent = null;
			Node current = root;

			while (current != null) {
				parent = current;
				if (node.key < current.key) {
					current = current.left;
				} else if (node.key > current.key) {
					current = current.right;
				} else {
					return;
				}
			}

			node.parent = parent;
			if (parent == null) {
				root = node;
			} else if (node.key < parent.key) {
				parent.left = node;
			} else {
				parent.right = node;
			}

			fixInsert(node);

			int v = validate();
			if (v != 0) {
				System.err.println("Red-Black property violated after insert of " + key + ": code=" + v);
			}
		}

		public boolean contains(int key) {
			return searchNode(key) != null;
		}

		/**
		 * 刪除：從樹中移除鍵為 key 的節點（若存在）
		 * - 若被移除節點有兩個子節點，使用後繼（右子樹的最小值）取代
		 * - 若被移除或被取代的節點原先為黑，呼叫 fixDelete 處理黑高不平衡
		 * @param key 要刪除的整數鍵
		 */
		public void delete(int key) {
			Node z = searchNode(key);
			if (z == null) {
				return;
			}

			Node y = z;
			boolean yOriginalColor = y.color;
			Node x;
			Node xParent;

			if (z.left == null) {
				x = z.right;
				xParent = z.parent;
				transplant(z, z.right);
			} else if (z.right == null) {
				x = z.left;
				xParent = z.parent;
				transplant(z, z.left);
			} else {
				y = minimum(z.right);
				yOriginalColor = y.color;
				x = y.right;
				if (y.parent == z) {
					xParent = y;
				} else {
					xParent = y.parent;
					transplant(y, y.right);
					y.right = z.right;
					if (y.right != null) {
						y.right.parent = y;
					}
				}
				transplant(z, y);
				y.left = z.left;
				if (y.left != null) {
					y.left.parent = y;
				}
				y.color = z.color;
				if (x != null) {
					x.parent = xParent;
				}
			}

			if (yOriginalColor == BLACK) {
				fixDelete(x, xParent);
			}

			int v = validate();
			if (v != 0) {
				System.err.println("Red-Black property violated after delete of " + key + ": code=" + v);
			}
		}

		public void inorder() {
			inorder(root);
			System.out.println();
		}

		public void printTree() {
			printTree(root, 0);
		}

		public void clear() {
			root = null;
		}

		public String inorderString() {
			StringBuilder sb = new StringBuilder();
			buildInorderString(root, sb);
			return sb.toString().trim();
		}

		public Node getRoot() {
			return root;
		}

		private void inorder(Node node) {
			if (node == null) {
				return;
			}
			inorder(node.left);
			System.out.print(node.key + (node.color == RED ? "R" : "B") + " ");
			inorder(node.right);
		}

		private void printTree(Node node, int depth) {
			if (node == null) {
				return;
			}
			printTree(node.right, depth + 1);
			for (int i = 0; i < depth; i++) {
				System.out.print("    ");
			}
			System.out.println(node.key + (node.color == RED ? "(R)" : "(B)"));
			printTree(node.left, depth + 1);
		}

		private void buildInorderString(Node node, StringBuilder sb) {
			if (node == null) {
				return;
			}
			buildInorderString(node.left, sb);
			sb.append(node.key).append(node.color == RED ? "R" : "B").append(' ');
			buildInorderString(node.right, sb);
		}

		private Node searchNode(int key) {
			Node current = root;
			while (current != null) {
				if (key < current.key) {
					current = current.left;
				} else if (key > current.key) {
					current = current.right;
				} else {
					return current;
				}
			}
			return null;
		}

		private Node minimum(Node node) {
			Node current = node;
			while (current != null && current.left != null) {
				current = current.left;
			}
			return current;
		}

		/**
		 * 左旋（Left Rotate）
		 * - 將節點 x 的右子 y 提升至 x 的位置，x 成為 y 的左子
		 * - 此操作維持 BST 性質，但會改變高度與父子關係，用於修正不平衡
		 */
		private void rotateLeft(Node x) {
			Node y = x.right;
			x.right = y.left;
			if (y.left != null) {
				y.left.parent = x;
			}
			y.parent = x.parent;
			if (x.parent == null) {
				root = y;
			} else if (x == x.parent.left) {
				x.parent.left = y;
			} else {
				x.parent.right = y;
			}
			y.left = x;
			x.parent = y;
		}

		/**
		 * 右旋（Right Rotate）
		 * - 將節點 y 的左子 x 提升至 y 的位置，y 成為 x 的右子
		 * - 為左旋的對稱操作
		 */
		private void rotateRight(Node y) {
			Node x = y.left;
			y.left = x.right;
			if (x.right != null) {
				x.right.parent = y;
			}
			x.parent = y.parent;
			if (y.parent == null) {
				root = x;
			} else if (y == y.parent.left) {
				y.parent.left = x;
			} else {
				y.parent.right = x;
			}
			x.right = y;
			y.parent = x;
		}

		/**
		 * fixInsert：修正插入所造成的違規（尤其是連續兩紅的情形）
		 * 演算法概要：
		 * - 若父節點為紅（違規），根據叔父節點顏色分成 recolor 或 rotate 的情況處理
		 * - 在某些情況先做旋轉把問題轉成另一種型態，最後把父設為黑並對祖父旋轉
		 */
		private void fixInsert(Node z) {
			while (z.parent != null && z.parent.color == RED) {
				if (z.parent == z.parent.parent.left) {
					Node uncle = z.parent.parent.right;
					if (uncle != null && uncle.color == RED) {
						z.parent.color = BLACK;
						uncle.color = BLACK;
						z.parent.parent.color = RED;
						z = z.parent.parent;
					} else {
						if (z == z.parent.right) {
							z = z.parent;
							rotateLeft(z);
						}
						z.parent.color = BLACK;
						z.parent.parent.color = RED;
						rotateRight(z.parent.parent);
					}
				} else {
					Node uncle = z.parent.parent.left;
					if (uncle != null && uncle.color == RED) {
						z.parent.color = BLACK;
						uncle.color = BLACK;
						z.parent.parent.color = RED;
						z = z.parent.parent;
					} else {
						if (z == z.parent.left) {
							z = z.parent;
							rotateRight(z);
						}
						z.parent.color = BLACK;
						z.parent.parent.color = RED;
						rotateLeft(z.parent.parent);
					}
				}
			}
			root.color = BLACK;
		}

		/**
		 * fixDelete：修正刪除後可能出現的 "double-black" 問題
		 * - 當被刪除或被替代的節點原先為黑，可能導致某條路徑的黑高減少
		 * - 透過檢查兄弟節點顏色與子節點顏色，進行 recolor 或旋轉，直到恢復性質
		 * @param x 被視為替代位置的節點（可能為 null，代表黑高遞減的一端）
		 * @param parent x 的父節點（因 x 可能為 null，因此父節點由外部提供）
		 */
		private void fixDelete(Node x, Node parent) {
			Node current = x;
			Node currentParent = parent;

			while (current != root && colorOf(current) == BLACK) {
				if (currentParent == null) {
					break;
				}

				if (current == leftOf(currentParent)) {
					Node sibling = rightOf(currentParent);
					if (colorOf(sibling) == RED) {
						sibling.color = BLACK;
						currentParent.color = RED;
						rotateLeft(currentParent);
						sibling = rightOf(currentParent);
					}

					if (colorOf(leftOf(sibling)) == BLACK && colorOf(rightOf(sibling)) == BLACK) {
						if (sibling != null) {
							sibling.color = RED;
						}
						current = currentParent;
						currentParent = parentOf(current);
					} else {
						if (colorOf(rightOf(sibling)) == BLACK) {
							if (leftOf(sibling) != null) {
								leftOf(sibling).color = BLACK;
							}
							if (sibling != null) {
								sibling.color = RED;
								rotateRight(sibling);
							}
							sibling = rightOf(currentParent);
						}
						if (sibling != null) {
							sibling.color = currentParent.color;
						}
						currentParent.color = BLACK;
						if (rightOf(sibling) != null) {
							rightOf(sibling).color = BLACK;
						}
						rotateLeft(currentParent);
						current = root;
						break;
					}
				} else {
					Node sibling = leftOf(currentParent);
					if (colorOf(sibling) == RED) {
						sibling.color = BLACK;
						currentParent.color = RED;
						rotateRight(currentParent);
						sibling = leftOf(currentParent);
					}

					if (colorOf(rightOf(sibling)) == BLACK && colorOf(leftOf(sibling)) == BLACK) {
						if (sibling != null) {
							sibling.color = RED;
						}
						current = currentParent;
						currentParent = parentOf(current);
					} else {
						if (colorOf(leftOf(sibling)) == BLACK) {
							if (rightOf(sibling) != null) {
								rightOf(sibling).color = BLACK;
							}
							if (sibling != null) {
								sibling.color = RED;
								rotateLeft(sibling);
							}
							sibling = leftOf(currentParent);
						}
						if (sibling != null) {
							sibling.color = currentParent.color;
						}
						currentParent.color = BLACK;
						if (leftOf(sibling) != null) {
							leftOf(sibling).color = BLACK;
						}
						rotateRight(currentParent);
						current = root;
						break;
					}
				}
			}

			if (current != null) {
				current.color = BLACK;
			}
		}

		private void transplant(Node u, Node v) {
			if (u.parent == null) {
				root = v;
			} else if (u == u.parent.left) {
				u.parent.left = v;
			} else {
				u.parent.right = v;
			}
			if (v != null) {
				v.parent = u.parent;
			}
		}

		private boolean colorOf(Node node) {
			return node == null ? BLACK : node.color;
		}

		private Node parentOf(Node node) {
			return node == null ? null : node.parent;
		}

		private Node leftOf(Node node) {
			return node == null ? null : node.left;
		}

		private Node rightOf(Node node) {
			return node == null ? null : node.right;
		}

		/*
		 * Validator for Red-Black properties.
		 * Returns 0 if valid.
		 * Returns 1 if root is not black.
		 * Returns 2 if a red node has a red child.
		 * Returns 3 if black-height differs between subtrees.
		 */

		/**
		 * validate：檢查紅黑樹三個主要不變性
		 * - root 為黑
		 * - 不存在連續的紅節點（紅節點不可有紅子節點）
		 * - 任一節點左右子樹的黑高相等
		 *
		 * 回傳值說明（簡單錯誤代碼，用於除錯）：
		 * 0 = 驗證通過
		 * 1 = root 不是黑色
		 * 3 = 黑高不一致（或其他檢查失敗）
		 */
		public int validate() {
			if (root == null) return 0;
			if (root.color != BLACK) return 1;
			int bh = checkNode(root);
			return bh == -1 ? 3 : 0;
		}

		/**
		 * 檢查子樹並回傳黑高（若發現違規回傳 -1）
		 * - 空節點視為黑高 1（此為實作選擇，僅在相對比較時有意義）
		 */
		private int checkNode(Node node) {
			if (node == null) return 1; // nulls count as black-height 1
			// 檢查紅節點是否有紅子節點（違規）
			if (node.color == RED) {
				if ((node.left != null && node.left.color == RED) || (node.right != null && node.right.color == RED)) {
					return -1; // red violation
				}
			}
			int left = checkNode(node.left);
			if (left == -1) return -1;
			int right = checkNode(node.right);
			if (right == -1) return -1;
			// 左右子樹黑高必須一致
			if (left != right) return -1;
			// 回傳此子樹的黑高：若節點為黑則加 1
			return left + (node.color == BLACK ? 1 : 0);
		}
	}

	private static void printTimeComplexity() {
		System.out.println("Time Complexity:");
		System.out.println("Insert: O(log n)");
		System.out.println("Delete: O(log n)");
		System.out.println("Search: O(log n)");
		System.out.println("Traversal: O(n)");
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			RedBlackTreeFrame frame = new RedBlackTreeFrame();
			frame.setVisible(true);
		});
	}

	/**
	 * RedBlackTreeFrame：Swing GUI 主視窗
	 * - 左上方為控制按鈕（Key 輸入、Insert/Delete/Search/Load/Clear/Validate）
	 * - 中間為可捲動的 `TreePanel`，負責把樹以節點與線段方式繪出
	 * - 下方為輸出區，顯示中序走訪結果與時間複雜度摘要
	 */
	private static class RedBlackTreeFrame extends JFrame {
		private final RedBlackTree tree = new RedBlackTree();
		private final TreePanel treePanel = new TreePanel(tree);
		private final JTextArea outputArea = new JTextArea();
		private final JTextField keyField = new JTextField(10);

		RedBlackTreeFrame() {
			super("Red-Black Tree GUI");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setLayout(new BorderLayout(10, 10));
			setSize(1200, 800);
			setLocationRelativeTo(null);

			JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JButton insertButton = new JButton("Insert");
			JButton deleteButton = new JButton("Delete");
			JButton searchButton = new JButton("Search");
			JButton sampleButton = new JButton("Load Sample");
			JButton clearButton = new JButton("Clear");
			JButton validateButton = new JButton("Validate");

			controlPanel.add(new JLabel("Key:"));
			controlPanel.add(keyField);
			controlPanel.add(insertButton);
			controlPanel.add(deleteButton);
			controlPanel.add(searchButton);
			controlPanel.add(sampleButton);
			controlPanel.add(clearButton);
			controlPanel.add(validateButton);

			outputArea.setEditable(false);
			outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
			outputArea.setRows(8);
			outputArea.setText(buildStatusText("Ready."));

			JScrollPane outputScroll = new JScrollPane(outputArea);
			outputScroll.setPreferredSize(new Dimension(300, 180));

			JScrollPane treeScroll = new JScrollPane(treePanel);
			treeScroll.setPreferredSize(new Dimension(900, 600));

			add(controlPanel, BorderLayout.NORTH);
			add(treeScroll, BorderLayout.CENTER);
			add(outputScroll, BorderLayout.SOUTH);

			insertButton.addActionListener(e -> {
				Integer key = readKey();
				if (key == null) return;
				tree.insert(key);
				refresh("Inserted " + key + ".");
			});

			deleteButton.addActionListener(e -> {
				Integer key = readKey();
				if (key == null) return;
				tree.delete(key);
				refresh("Deleted " + key + ".");
			});

			searchButton.addActionListener(e -> {
				Integer key = readKey();
				if (key == null) return;
				boolean found = tree.contains(key);
				refresh("Search " + key + ": " + (found ? "found" : "not found") + ".");
			});

			sampleButton.addActionListener(e -> {
				tree.clear();
				int[] values = {20, 15, 25, 10, 5, 1, 30, 22, 27, 17};
				for (int value : values) {
					tree.insert(value);
				}
				refresh("Loaded sample tree.");
			});

			clearButton.addActionListener(e -> {
				tree.clear();
				refresh("Tree cleared.");
			});

			validateButton.addActionListener(e -> {
				int code = tree.validate();
				String msg;
				switch (code) {
					case 0: msg = "Tree is a valid Red-Black Tree."; break;
					case 1: msg = "Violation: root is not black."; break;
					case 2: msg = "Violation: red node has red child."; break;
					case 3: msg = "Violation: black-height differs between subtrees."; break;
					default: msg = "Unknown validation result: " + code; break;
				}
				refresh(msg);
			});
		}

		private Integer readKey() {
			String text = keyField.getText().trim();
			if (text.isEmpty()) {
				outputArea.setText(buildStatusText("Please enter a key."));
				return null;
			}
			try {
				return Integer.parseInt(text);
			} catch (NumberFormatException ex) {
				outputArea.setText(buildStatusText("Invalid integer: " + text));
				return null;
			}
		}

		private void refresh(String message) {
			outputArea.setText(buildStatusText(message));
			treePanel.repaint();
		}

		private String buildStatusText(String message) {
			StringBuilder sb = new StringBuilder();
			sb.append(message).append('\n').append('\n');
			sb.append("Inorder traversal:\n");
			sb.append(tree.inorderString().isEmpty() ? "(empty)" : tree.inorderString()).append('\n').append('\n');
			sb.append("Time Complexity:\n");
			sb.append("Insert: O(log n)\n");
			sb.append("Delete: O(log n)\n");
			sb.append("Search: O(log n)\n");
			sb.append("Traversal: O(n)\n");
			return sb.toString();
		}
	}

	/**
	 * TreePanel：負責在畫布上繪製紅黑樹
	 * - 使用遞迴方式計算節點座標，並繪製節點圓形與鍵值文字
	 * - 節點顏色以紅/深灰表示
	 */
	private static class TreePanel extends JPanel {
		private final RedBlackTree tree;

		TreePanel(RedBlackTree tree) {
			this.tree = tree;
			setBackground(Color.WHITE);
			setPreferredSize(new Dimension(1600, 1000));
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			Node root = tree.getRoot();
			if (root == null) {
				g2.setColor(Color.DARK_GRAY);
				g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
				g2.drawString("Tree is empty. Use Load Sample or insert keys.", 40, 50);
				g2.dispose();
				return;
			}

			drawTree(g2, root, getWidth() / 2, 50, Math.max(getWidth() / 4, 120));
			g2.dispose();
		}

		private void drawTree(Graphics2D g2, Node node, int x, int y, int offset) {
			if (node == null) {
				return;
			}

			int childY = y + 80;
			int childOffset = Math.max(offset / 2, 40);

			if (node.left != null) {
				int childX = x - offset;
				g2.setColor(Color.GRAY);
				g2.drawLine(x, y, childX, childY);
				drawTree(g2, node.left, childX, childY, childOffset);
			}

			if (node.right != null) {
				int childX = x + offset;
				g2.setColor(Color.GRAY);
				g2.drawLine(x, y, childX, childY);
				drawTree(g2, node.right, childX, childY, childOffset);
			}

			int radius = 22;
			g2.setColor(node.color == RED ? new Color(190, 40, 40) : new Color(40, 40, 40));
			g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
			g2.setColor(Color.WHITE);
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
			String text = String.valueOf(node.key);
			FontMetrics fm = g2.getFontMetrics();
			int textX = x - fm.stringWidth(text) / 2;
			int textY = y + fm.getAscent() / 2 - 2;
			g2.drawString(text, textX, textY);

			g2.setColor(Color.BLACK);
			g2.drawOval(x - radius, y - radius, radius * 2, radius * 2);
		}
	}
}
