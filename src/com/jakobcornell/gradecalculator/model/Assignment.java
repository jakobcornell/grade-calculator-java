package com.jakobcornell.gradecalculator.model;

import java.util.UUID;

public class Assignment implements java.io.Serializable {
	private static final long serialVersionUID = 0;

	public final UUID id;
	public String name;
	protected double weight;
	protected double earned, possible;

	public Assignment(String name, double weight, double earned, double possible) throws IllegalArgumentException {
		id = UUID.randomUUID();
		this.name = name;
		setWeight(weight);
		setEarned(earned);
		setPossible(possible);
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

	public double getEarned() {
		return earned;
	}

	public void setEarned(double earned) throws IllegalArgumentException {
		if (earned >= 0) {
			this.earned = earned;
		} else {
			throw new IllegalArgumentException("Scores cannot be negative");
		}
	}

	public double getPossible() {
		return possible;
	}

	public void setPossible(double possible) throws IllegalArgumentException {
		if (possible >= 0) {
			this.possible = possible;
		} else {
			throw new IllegalArgumentException("Scores cannot be negative");
		}
	}

	public double score() throws ScoringException {
		if (possible > 0) {
			return earned / possible;
		} else {
			throw new ScoringException(String.format("Assignment %s has 0 possible points", id.toString()));
		}
	}
}
