package com.jakobcornell.gradecalculator.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.function.Predicate;

public class Course implements java.io.Serializable {
	private static final long serialVersionUID = 0;

	public final Map<UUID, Category> categories;
	public String name;

	public Course(String name) {
		categories = new HashMap<UUID, Category>();
		this.name = name;
	}

	public double score() throws ScoringException {
		Predicate<Category> scorable = new Predicate<Category>() {
			public boolean test(Category category) {
				try {
					category.score();
				} catch (ScoringException e) {
					return false;
				}
				return true;
			}
		};

		List<Double> weights = categories.values().stream()
			.filter(scorable)
			.map(Category::getWeight)
			.collect(Collectors.toList());

		List<Double> scores = new ArrayList<Double>();
		for (Category category : categories.values()) {
			if (scorable.test(category)) {
				scores.add(category.score());
			}
		}

		double totalWeight = weights.stream()
			.mapToDouble(Double::valueOf)
			.sum();

		if (scores.isEmpty()) {
			throw new ScoringException("No scorable categories");
		} else {
			double score = 0;
			for (int i = 0; i < scores.size(); i += 1) {
				score += scores.get(i) * weights.get(i) / totalWeight;
			}
			return score;
		}
	}
}
