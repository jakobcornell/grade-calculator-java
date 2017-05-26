package com.jakobcornell.gradecalculator.model;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class Category {
	public final UUID id;
	public final Map<UUID, Assignment> assignments;
	public String name;
	public boolean useWeights;
	protected double weight;

	public Category(String name, double weight, boolean useWeights) {
		id = UUID.randomUUID();
		assignments = new HashMap<UUID, Assignment>();
		this.name = name;
		this.weight = weight;
		this.useWeights = useWeights;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) throws IllegalArgumentException {
		if (weight >= 0) {
			this.weight = weight;
		} else {
			throw new IllegalArgumentException("Weights cannot be negative");
		}
	}

	public double score() throws ScoringException {
		if (useWeights) {
			List<Double> weights = assignments.values().stream()
				.map(Assignment::getWeight)
				.collect(Collectors.toList());

			List<Double> scores = new ArrayList<Double>();
			for (Assignment assignment : assignments.values()) {
				scores.add(assignment.score());
			}

			double totalWeight = weights.stream()
				.mapToDouble(Double::valueOf)
				.sum();

			double score = 0;
			for (int i = 0; i < weights.size(); i += 1) {
				score += scores.get(i) * weights.get(i) / totalWeight;
			}

			return score;
		} else {
			double earned = assignments.values().stream()
				.mapToDouble(Assignment::getEarned)
				.sum();
			double possible = assignments.values().stream()
				.mapToDouble(Assignment::getPossible)
				.sum();

			if (possible > 0) {
				return earned / possible;
			} else {
				throw new ScoringException(String.format("Category %s has 0 total possible points", id.toString()));
			}
		}
	}
}
