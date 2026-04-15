import math


def l1_distance(p1, p2):
	return abs(p1[0] - p2[0]) + abs(p1[1] - p2[1])


def l2_distance(p1, p2):
	return math.sqrt((p1[0] - p2[0]) ** 2 + (p1[1] - p2[1]) ** 2)


train_points = {
	"A": (1, 1),
	"B": (4, 4),
	"C": (6, 1),
}
P = (3, 2)

l1_results = {name: l1_distance(P, point) for name, point in train_points.items()}
l2_results = {name: l2_distance(P, point) for name, point in train_points.items()}

print("L1 (Manhattan) distances:")
for name, dist in l1_results.items():
	print(f"P to {name}: {dist}")

print("\nL2 (Euclidean) distances:")
for name, dist in l2_results.items():
	print(f"P to {name}: {dist:.3f}")

# If distances tie, min() keeps the first encountered key (A before B here).
nearest_l1 = min(l1_results, key=l1_results.get)
nearest_l2 = min(l2_results, key=l2_results.get)

print(f"\nNearest under L1: {nearest_l1}")
print(f"Nearest under L2: {nearest_l2}")
