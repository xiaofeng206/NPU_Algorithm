class Node:
	def __init__(self, val, next_node=None):
		self.val = val
		self.next = next_node


# Create linked list step by step: 1 -> 2 -> 3 -> 4
print("=== Create Singly Linked List ===")
n4 = Node(4)
print("Create node n4 with value 4")

n3 = Node(3, n4)
print("Create node n3 with value 3, n3.next -> 4")

n2 = Node(2, n3)
print("Create node n2 with value 2, n2.next -> 3")

head = Node(1, n2)
print("Create head node with value 1, head.next -> 2")
print("Linked list structure: 1 -> 2 -> 3 -> 4")
print()

# Traverse and collect values
print("=== Traverse Linked List ===")
current = head
values = []
index = 0
while current is not None:
	print(f"Step {index}: visit node value = {current.val}")
	values.append(str(current.val))
	current = current.next
	index += 1

print()
print("Final output:")
print(" ".join(values))
