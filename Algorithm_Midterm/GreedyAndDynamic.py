def min_coins(amount):
	coins = [25, 10, 5, 1]
	used = []
	for coin in coins:
		while amount >= coin:
			amount -= coin
			used.append(coin)
	return used


used = min_coins(63)
print(f"{len(used)} coins")
print(used)