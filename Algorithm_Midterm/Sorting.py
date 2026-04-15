def bubble_sort(arr):
	n = len(arr)
	for i in range(n):
		swapped = False
		for j in range(0, n - i - 1):
			if arr[j] > arr[j + 1]:
				arr[j], arr[j + 1] = arr[j + 1], arr[j]
				swapped = True
		if not swapped:
			break
	return arr


arr = [5, 1, 4, 2, 8]
print("原先的資料順序:", arr)
sorted_arr = bubble_sort(arr.copy())
print("排序完成的順序:", sorted_arr)
