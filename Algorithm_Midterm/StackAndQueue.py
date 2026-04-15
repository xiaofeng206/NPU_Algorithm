from collections import deque

# Stack (LIFO: Last In, First Out)
print("=== Stack Demo (LIFO) ===")
stack = []
print("Initial stack:", stack)

stack.append(10)
print("push 10 ->", stack)

stack.append(20)
print("push 20 ->", stack)

popped = stack.pop()
print(f"pop -> removed {popped}, stack:", stack)

stack.append(30)
print("push 30 ->", stack)

print("Final Stack:", stack)
print("Stack characteristic: LIFO (the last element pushed is popped first)")
print()

# Queue (FIFO: First In, First Out)
print("=== Queue Demo (FIFO) ===")
queue = deque()
print("Initial queue:", list(queue))

queue.append(10)
print("enqueue 10 ->", list(queue))

queue.append(20)
print("enqueue 20 ->", list(queue))

dequeued = queue.popleft()
print(f"dequeue -> removed {dequeued}, queue:", list(queue))

queue.append(30)
print("enqueue 30 ->", list(queue))

print("Final Queue:", list(queue))
print("Queue characteristic: FIFO (the first element enqueued is dequeued first)")
