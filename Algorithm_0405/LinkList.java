public class LinkList {

	static class Node {
		int data;
		Node next;

		Node(int data) {
			this.data = data;
		}
	}

	private Node head;

	// 在開頭插入節點，時間複雜度 O(1)
	public void insertAtHead(int data) {
		Node newNode = new Node(data);
		newNode.next = head;
		head = newNode;
	}

	// 在尾端插入節點，時間複雜度 O(n)
	public void insertAtTail(int data) {
		Node newNode = new Node(data);
		if (head == null) {
			head = newNode;
			return;
		}

		Node temp = head;
		while (temp.next != null) {
			temp = temp.next;
		}
		temp.next = newNode;
	}

	// 刪除第一個符合數值的節點，時間複雜度 O(n)
	public void delete(int key) {
		if (head == null) {
			return;
		}

		if (head.data == key) {
			head = head.next;
			return;
		}

		Node temp = head;
		while (temp.next != null && temp.next.data != key) {
			temp = temp.next;
		}

		if (temp.next != null) {
			temp.next = temp.next.next;
		}
	}

	// 搜尋數值是否存在，時間複雜度 O(n)
	public boolean search(int key) {
		Node temp = head;
		while (temp != null) {
			if (temp.data == key) {
				return true;
			}
			temp = temp.next;
		}
		return false;
	}

	// 印出串列內容，時間複雜度 O(n)
	public void display() {
		Node temp = head;
		while (temp != null) {
			System.out.print(temp.data + " -> ");
			temp = temp.next;
		}
		System.out.println("null");
	}

	// 時間複雜度總整理
	public void printTimeComplexity() {
		System.out.println("\nTime Complexity:");
		System.out.println("insertAtHead: O(1)");
		System.out.println("insertAtTail: O(n)");
		System.out.println("delete: O(n)");
		System.out.println("search: O(n)");
		System.out.println("display: O(n)");
	}

	public static void main(String[] args) {
		LinkList list = new LinkList();

		list.insertAtHead(20);
		list.insertAtHead(10);
		list.insertAtTail(30);
		list.insertAtTail(40);

		System.out.print("Original list: ");
		list.display();

		System.out.println("Search 30: " + list.search(30));
		System.out.println("Search 99: " + list.search(99));

		list.delete(20);
		System.out.print("After delete 20: ");
		list.display();

		list.printTimeComplexity();
	}
}
