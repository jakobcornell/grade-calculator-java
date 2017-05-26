package com.jakobcornell.gradecalculator.model;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class Course {
	public final Map<UUID, Category> categories;
	public String name;

	public Course(String name) {
		categories = new HashMap<UUID, Category>();
		this.name = name;
	}

	public double score() throws ScoringException {
		return 0; // TODO
	}
}
