import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SimulatorCity {

	enum Exit {
		A, B, C, ABSTAIN
	}

	static class Resident {
		String name;
		String district; // 北區 / 中區 / 南區
		double x;
		double y;
		Exit decision; // 今日決策
		Exit trueSafe; // 真正安全出口（地面真相）
		double trust; // 可信度 [0,1]

		Resident(String name, String district, double x, double y, Exit decision, Exit trueSafe, double trust) {
			this.name = name;
			this.district = district;
			this.x = x;
			this.y = y;
			this.decision = decision;
			this.trueSafe = trueSafe;
			this.trust = trust;
		}

		double distanceTo(double px, double py) {
			double dx = x - px;
			double dy = y - py;
			return Math.sqrt(dx * dx + dy * dy);
		}

		boolean isCorrect() {
			return decision == trueSafe;
		}
	}

	static class Investigator {
		String name;
		double x;
		double y;
		Exit trueSafe;

		Investigator(String name, double x, double y, Exit trueSafe) {
			this.name = name;
			this.x = x;
			this.y = y;
			this.trueSafe = trueSafe;
		}
	}

	public static void main(String[] args) {
		List<Resident> city = buildCity();
		Investigator investigator = new Investigator("你", 5.0, 3.0, Exit.A);

		System.out.println("=== 霧城模擬：Baseline 先失敗一次 ===");
		System.out.println("規則：ŷ = majority(N_k(x))");
		System.out.println("重點：第二夜不是不理性，而是把『距離』誤當『可信度』。\n");

		int[] nightsK = {1, 3, 5};
		String[] nightTitles = {
			"第一夜：K=1（局部雜訊敏感）",
			"第二夜：K=3（剛好踩中偏差區）",
			"第三夜：K=5（偏差共識被放大）"
		};

		for (int i = 0; i < nightsK.length; i++) {
			int k = nightsK[i];
			System.out.println("--------------------------------------------------");
			System.out.println(nightTitles[i] + " (K=" + k + ")");
			printNeighborSnapshot(city, investigator.x, investigator.y, k);

			Exit naive = predictByPlainKNN(city, investigator.x, investigator.y, k);
			Exit robust = predictByTrustWeightedKNN(city, investigator.x, investigator.y, k);
			Exit abstain = predictByAbstainKNN(city, investigator.x, investigator.y, k, 0.18);
		Exit reconnaissance = predictByReconnaissanceKNN(city, investigator.x, investigator.y, k);

		printInvestigatorResult("Baseline：傳統 KNN", naive, investigator.trueSafe);
		printInvestigatorResult("救援1：Trust + Distance Weighted KNN", robust, investigator.trueSafe);
		printInvestigatorResult("救援2：Reject / Abstain", abstain, investigator.trueSafe);
		printInvestigatorResult("救援3：先偵查、評估風險", reconnaissance, investigator.trueSafe);
			System.out.println();
		}

		System.out.println("結論：第二夜失敗的核心，是把『近』誤當成『可信』。");
		System.out.println("改造後：至少用 trust-aware weighted KNN，可在偏差區附近提高存活率。");
	}

	static List<Resident> buildCity() {
		List<Resident> city = new ArrayList<>();

		// 北區：較遠，但大多正確且可信
		city.add(new Resident("N1", "北區", 2.2, 8.0, Exit.A, Exit.A, 0.95));
		city.add(new Resident("N2", "北區", 1.6, 7.5, Exit.A, Exit.A, 0.92));
		city.add(new Resident("N3", "北區", 2.8, 7.2, Exit.A, Exit.A, 0.90));

		// 中區：有一位很近且可信，形成 K=1 的安全情境
		city.add(new Resident("M1", "中區", 4.9, 3.1, Exit.A, Exit.A, 0.98)); // 最近且正確
		city.add(new Resident("M2", "中區", 4.2, 4.2, Exit.A, Exit.A, 0.88));
		city.add(new Resident("M3", "中區", 3.8, 5.0, Exit.A, Exit.A, 0.85));

		// 南區：偏差區（很近、很一致、但錯）
		city.add(new Resident("S1", "南區", 5.5, 2.9, Exit.B, Exit.A, 0.25));
		city.add(new Resident("S2", "南區", 5.6, 3.2, Exit.B, Exit.A, 0.22));
		city.add(new Resident("S3", "南區", 5.7, 2.7, Exit.B, Exit.A, 0.20));
		city.add(new Resident("S4", "南區", 5.8, 3.1, Exit.B, Exit.A, 0.18));
		city.add(new Resident("S5", "南區", 5.9, 2.8, Exit.B, Exit.A, 0.18));

		return city;
	}

	static Exit predictByPlainKNN(List<Resident> city, double x, double y, int k) {
		List<Resident> neighbors = kNearest(city, x, y, k);
		Map<Exit, Integer> vote = new HashMap<>();
		for (Exit e : Exit.values()) vote.put(e, 0);

		for (Resident r : neighbors) {
			vote.put(r.decision, vote.get(r.decision) + 1);
		}

		Exit best = Exit.A;
		int bestCount = -1;
		for (Exit e : Exit.values()) {
			if (vote.get(e) > bestCount) {
				best = e;
				bestCount = vote.get(e);
			}
		}
		return best;
	}

	// 改造 KNN：trust-aware + distance-weighted
	static Exit predictByTrustWeightedKNN(List<Resident> city, double x, double y, int k) {
		List<Resident> neighbors = kNearest(city, x, y, k);
		Map<Exit, Double> weightedVote = new HashMap<>();
		for (Exit e : Exit.values()) weightedVote.put(e, 0.0);

		for (Resident r : neighbors) {
			double d = Math.max(r.distanceTo(x, y), 1e-6);
			double distanceWeight = 1.0 / d;
			double score = distanceWeight * r.trust;

			weightedVote.put(r.decision, weightedVote.get(r.decision) + score);
		}

		Exit best = Exit.A;
		double bestScore = -1;
		for (Exit e : Exit.values()) {
			if (weightedVote.get(e) > bestScore) {
				best = e;
				bestScore = weightedVote.get(e);
			}
		}
		return best;
	}

	// 鄰居矛盾時拒絕跟隨（Abstain）
	static Exit predictByAbstainKNN(List<Resident> city, double x, double y, int k, double marginThreshold) {
		List<Resident> neighbors = kNearest(city, x, y, k);
		Map<Exit, Double> weightedVote = new HashMap<>();
		weightedVote.put(Exit.A, 0.0);
		weightedVote.put(Exit.B, 0.0);
		weightedVote.put(Exit.C, 0.0);

		for (Resident r : neighbors) {
			double d = Math.max(r.distanceTo(x, y), 1e-6);
			double score = (1.0 / d) * r.trust;
			weightedVote.put(r.decision, weightedVote.get(r.decision) + score);
		}

		Exit best = Exit.A;
		Exit second = Exit.B;
		for (Exit e : new Exit[]{Exit.A, Exit.B, Exit.C}) {
			if (weightedVote.get(e) > weightedVote.get(best)) {
				second = best;
				best = e;
			} else if (e != best && weightedVote.get(e) > weightedVote.get(second)) {
				second = e;
			}
		}

		double s1 = weightedVote.get(best);
		double s2 = weightedVote.get(second);
		double margin = s1 <= 1e-9 ? 0 : (s1 - s2) / s1;
		if (margin < marginThreshold) {
			return Exit.ABSTAIN;
		}
		return best;
	}

	static List<Resident> kNearest(List<Resident> city, double x, double y, int k) {
		List<Resident> sorted = new ArrayList<>(city);
		sorted.sort(Comparator.comparingDouble(r -> r.distanceTo(x, y)));
		return sorted.subList(0, Math.min(k, sorted.size()));
	}

	// 救援3：提前偵查每個出口的風險與可信度，不被多數決策綁架
	static Exit predictByReconnaissanceKNN(List<Resident> city, double x, double y, int k) {
		List<Resident> neighbors = kNearest(city, x, y, k);

		// 對每個出口進行風險評估
		Map<Exit, ReconnaissanceScore> scores = new HashMap<>();
		for (Exit exit : new Exit[]{Exit.A, Exit.B, Exit.C}) {
			scores.put(exit, evaluateExit(neighbors, exit));
		}

		// 選擇可信度最高的出口（即便人數最多的不是它）
		Exit best = Exit.A;
		double bestScore = -1;
		for (Exit exit : new Exit[]{Exit.A, Exit.B, Exit.C}) {
			double score = scores.get(exit).reliability;
			if (score > bestScore) {
				best = exit;
				bestScore = score;
			}
		}

		// 如果最安全的出口的可信度仍低於閾值，說明這個決策點風險太高
		if (bestScore < 0.15) {
			return Exit.ABSTAIN;
		}

		return best;
	}

	static class ReconnaissanceScore {
		double reliability;   // 該出口支持者的平均trust（加權）
		int supporterCount;   // 支持者數量
		int diversityCount;   // 來自多少個不同district的支持者

		ReconnaissanceScore(double reliability, int count, int diversity) {
			this.reliability = reliability;
			this.supporterCount = count;
			this.diversityCount = diversity;
		}
	}

	static ReconnaissanceScore evaluateExit(List<Resident> neighbors, Exit exit) {
		double trustSum = 0;
		int count = 0;
		java.util.Set<String> districts = new java.util.HashSet<>();

		for (Resident r : neighbors) {
			if (r.decision == exit) {
				trustSum += r.trust;
				count++;
				districts.add(r.district);
			}
		}

		if (count == 0) {
			return new ReconnaissanceScore(0.0, 0, 0);
		}

		// 可信度 = 支持者的平均trust + diversity獎勵
		double avgTrust = trustSum / count;
		double diversityBonus = districts.size() * 0.05; // 來自不同地區加分
		double reliability = avgTrust + diversityBonus;

		return new ReconnaissanceScore(reliability, count, districts.size());
	}

	static void printInvestigatorResult(String strategyName, Exit predicted, Exit trueSafe) {
		if (predicted == Exit.ABSTAIN) {
			System.out.println("🛑 " + strategyName + " -> 暫不跟隨（避免在不穩定區被帶錯）");
			return;
		}
		boolean safe = predicted == trueSafe;
		String mark = safe ? "✅" : "⚠️";
		System.out.println(mark + " " + strategyName + " -> 你選擇出口 " + predicted + "（真安全出口：" + trueSafe + "）");
	}

	static void printNeighborSnapshot(List<Resident> city, double x, double y, int k) {
		List<Resident> neighbors = kNearest(city, x, y, k);
		System.out.println("最近的 " + k + " 位鄰居：");
		for (Resident r : neighbors) {
			double d = r.distanceTo(x, y);
			String ok = r.isCorrect() ? "correct" : "wrong";
			System.out.printf("- %-2s %-2s d=%.3f decision=%s trust=%.2f (%s)%n",
					r.name, r.district, d, r.decision, r.trust, ok);
		}
	}
}
