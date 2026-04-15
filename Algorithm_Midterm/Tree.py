class Node:
	def __init__(self, val, left=None, right=None):
		self.val = val
		self.left = left
		self.right = right


def dfs_search(root, target, visited=None):
	"""Depth-First Search (preorder): root -> left -> right."""
	if visited is None:
		visited = []

	if root is None:
		return False, visited

	visited.append(root.val)
	if root.val == target:
		return True, visited

	found, visited = dfs_search(root.left, target, visited)
	if found:
		return True, visited

	return dfs_search(root.right, target, visited)


def run_search(root, target):
	found, visited = dfs_search(root, target)
	print(f"Searching for {target}")
	print("DFS visited order:", visited)
	print("Result:", "Found" if found else "Not Found")
	print("-" * 30)


root = Node(5,
			Node(3, Node(2), Node(4)),
			Node(8, None, Node(7)))

run_search(root, 7)
run_search(root, 9)
