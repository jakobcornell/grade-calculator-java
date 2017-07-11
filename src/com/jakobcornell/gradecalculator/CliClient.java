package com.jakobcornell.gradecalculator;

import com.jakobcornell.gradecalculator.model.Course;
import com.jakobcornell.gradecalculator.model.Category;
import com.jakobcornell.gradecalculator.model.Assignment;
import com.jakobcornell.gradecalculator.model.ScoringException;

import java.util.Scanner;
import java.util.Optional;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class CliClient {
	protected static final String PROMPT = "> ";
	protected static final String LEADER = "... ";

	protected enum Message {
		BAD_COMMAND("Unrecognized command. Try \"help\" for a list of commands."),
		NO_COURSE("No course loaded. Use \"new course\" or \"open\" to load one."),
		BAD_CATEGORY("Unrecognized category."),
		BAD_ASSIGNMENT("Unrecognized assignment."),
		CATEGORY_CREATE("Error creating category: %s"),
		ASSIGNMENT_CREATE("Error creating assignment: %s"),
		CATEGORY_UPDATE("Error updating category: %s"),
		ASSIGNMENT_UPDATE("Error updating assignment: %s"),
		FILENAME_MISSING("Filename missing."),
		COURSE_READ("Error reading course: %s"),
		COURSE_WRITE("Error writing course: %s"),
		BAD_ATTRIBUTE("Unrecognized attribute."),
		SCORING_ERROR("Error calculating score: %s");

		protected String message;

		private Message(String message) {
			this.message = message;
		}

		public String toString() {
			return message;
		}
	}

	protected Course course;

	public void main() {
		Scanner scanner = new Scanner(System.in);
		System.out.print(PROMPT);
		while (scanner.hasNextLine()) {
			String command = scanner.nextLine();
			String[] tokens = command.split("\\s+", 2);
			if (tokens.length > 0) {
				if (tokens[0].equals("open")) {

					if (tokens.length == 2) {
						try (
							FileInputStream fileIn = new FileInputStream(tokens[1]);
							ObjectInputStream courseIn = new ObjectInputStream(fileIn);
						) {
							course = (Course) courseIn.readObject();
						} catch (IOException | ClassNotFoundException e) {
							System.err.println(String.format(Message.COURSE_READ.toString(), e.getMessage()));
						}
					} else {
						System.err.println(Message.FILENAME_MISSING);
					}

				} else if (tokens[0].equals("save")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						if (tokens.length == 2) {
							try (
								FileOutputStream fileOut = new FileOutputStream(tokens[1]);
								ObjectOutputStream courseOut = new ObjectOutputStream(fileOut);
							) {
								courseOut.writeObject(course);
							} catch (IOException e) {
								System.err.println(String.format(Message.COURSE_WRITE.toString(), e.getMessage()));
							}
						} else {
							System.err.println(Message.FILENAME_MISSING);
						}
					}

				} else if (command.equals("new course")) {

					System.out.print(LEADER + "course name: ");
					course = new Course(scanner.nextLine());

				} else if (command.equals("course info")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.println(String.format("name: %s", course.name));
					}

				} else if (command.equals("categories")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.println(String.format("categories (%d):", course.categories.size()));
						for (Category category : course.categories.values()) {
							System.out.println(String.format("\t%s : %s", category.id, category.name));
						}
					}

				} else if (command.equals("category info")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.print(LEADER + "category id: ");
						String id = scanner.nextLine();
						Optional<Category> result = getCategory(id);
						if (result.isPresent()) {
							Category category = result.get();
							System.out.println(String.format("category %s:", category.id));
							System.out.println(String.format("\tname: %s", category.name));
							System.out.println(String.format("\tweight: %f", category.getWeight()));
							System.out.println(String.format("\tuses weights: %b", category.useWeights));
						} else {
							System.err.println(Message.BAD_CATEGORY);
						}
					}

				} else if (command.equals("assignments")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.print(LEADER + "category id: ");
						String id = scanner.nextLine();
						Optional<Category> result = getCategory(id);
						if (result.isPresent()) {
							Category category = result.get();
							System.out.println(String.format("assignments (%d):", category.assignments.size()));
							for (Assignment assignment : category.assignments.values()) {
								System.out.println(String.format("\t%s : %s", assignment.id, assignment.name));
							}
						} else {
							System.err.println(Message.BAD_CATEGORY);
						}
					}

				} else if (command.equals("assignment info")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.print(LEADER + "category id: ");
						String categoryId = scanner.nextLine();
						Optional<Category> categoryResult = getCategory(categoryId);
						if (categoryResult.isPresent()) {
							Category category = categoryResult.get();
							System.out.print(LEADER + "assignment id: ");
							String assignmentId = scanner.nextLine();
							Optional<Assignment> assignmentResult = getAssignment(category, assignmentId);
							if (assignmentResult.isPresent()) {
								Assignment assignment = assignmentResult.get();
								System.out.println(String.format("assignment %s:", assignment.id));
								System.out.println(String.format("\tname: %s", assignment.name));
								System.out.println(String.format("\tweight: %f", assignment.getWeight()));
								System.out.println(String.format("\tearned: %f", assignment.getEarned()));
								System.out.println(String.format("\tpossible: %f", assignment.getPossible()));
							} else {
								System.err.println(Message.BAD_ASSIGNMENT);
							}
						} else {
							System.err.println(Message.BAD_CATEGORY);
						}
					}

				} else if (command.equals("add category")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.print(LEADER + "name: ");
						String name = scanner.nextLine();

						System.out.print(LEADER + "weight: ");
						double weight = scanner.nextDouble();
						scanner.nextLine();

						System.out.print(LEADER + "uses weights (true|false): ");
						boolean useWeights = scanner.nextBoolean();
						scanner.nextLine();

						Category category;
						try {
							category = new Category(name, weight, useWeights);
							course.categories.put(category.id, category);
						} catch (IllegalArgumentException e) {
							System.err.println(String.format(Message.CATEGORY_CREATE.toString(), e.getMessage()));
						}
					}

				} else if (command.equals("add assignment")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.print(LEADER + "category id: ");
						String id = scanner.nextLine();
						Optional<Category> result = getCategory(id);
						if (result.isPresent()) {
							Category category = result.get();

							System.out.print(LEADER + "name: ");
							String name = scanner.nextLine();

							System.out.print(LEADER + "weight: ");
							double weight = scanner.nextDouble();
							scanner.nextLine();

							System.out.print(LEADER + "points earned: ");
							double earned = scanner.nextDouble();
							scanner.nextLine();

							System.out.print(LEADER + "points possible: ");
							double possible = scanner.nextDouble();
							scanner.nextLine();

							Assignment assignment;
							try {
								assignment = new Assignment(name, weight, earned, possible);
								category.assignments.put(assignment.id, assignment);
							} catch (IllegalArgumentException e) {
								System.err.println(String.format(Message.ASSIGNMENT_CREATE.toString(), e.getMessage()));
							}

						} else {
							System.err.println(Message.BAD_CATEGORY);
						}
					}

				} else if (command.equals("update course")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.print(LEADER + "new name: ");
						String name = scanner.nextLine();
						course.name = name;
					}

				} else if (command.equals("update category")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.print(LEADER + "category id: ");
						String id = scanner.nextLine();
						Optional<Category> result = getCategory(id);

						if (result.isPresent()) {
							Category category = result.get();
							System.out.print(LEADER + "update attribute (name|weight|uses weights): ");
							String attribute = scanner.nextLine();

							if (attribute.equals("name")) {
								System.out.print(LEADER + "new name: ");
								String name = scanner.nextLine();
								category.name = name;
							} else if (attribute.equals("weight")) {
								System.out.print(LEADER + "new weight: ");
								double weight = scanner.nextDouble();
								scanner.nextLine();
								try {
									category.setWeight(weight);
								} catch (IllegalArgumentException e) {
									System.err.println(String.format(Message.CATEGORY_UPDATE.toString(), e.getMessage()));
								}
							} else if (attribute.equals("uses weights")) {
								System.out.print(LEADER + "uses weights? (true|false): ");
								boolean useWeights = scanner.nextBoolean();
								scanner.nextLine();
								category.useWeights = useWeights;
							} else {
								System.err.println(Message.BAD_ATTRIBUTE);
							}
						} else {
							System.err.println(Message.BAD_CATEGORY);
						}
					}

				} else if (command.equals("update assignment")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.print(LEADER + "category id: ");
						String categoryId = scanner.nextLine();
						Optional<Category> categoryResult = getCategory(categoryId);

						if (categoryResult.isPresent()) {
							Category category = categoryResult.get();
							System.out.print(LEADER + "assignment id: ");
							String assignmentId = scanner.nextLine();
							Optional<Assignment> assignmentResult = getAssignment(category, assignmentId);

							if (assignmentResult.isPresent()) {
								Assignment assignment = assignmentResult.get();
								System.out.print(LEADER + "update attribute (name|weight|earned|possible): ");
								String attribute = scanner.nextLine();
								if (attribute.equals("name")) {
									System.out.print(LEADER + "new name: ");
									String name = scanner.nextLine();
									assignment.name = name;
								} else if (attribute.equals("weight")) {
									System.out.print(LEADER + "new weight: ");
									double weight = scanner.nextDouble();
									scanner.nextLine();
									try {
										assignment.setWeight(weight);
									} catch (IllegalArgumentException e) {
										System.err.println(String.format(Message.ASSIGNMENT_UPDATE.toString(), e.getMessage()));
									}
								} else if (attribute.equals("earned")) {
									System.out.print(LEADER + "points earned: ");
									double earned = scanner.nextDouble();
									scanner.nextLine();
									try {
										assignment.setEarned(earned);
									} catch (IllegalArgumentException e) {
										System.err.println(String.format(Message.ASSIGNMENT_UPDATE.toString(), e.getMessage()));
									}
								} else if (attribute.equals("possible")) {
									System.out.print(LEADER + "points possible: ");
									double possible = scanner.nextDouble();
									scanner.nextLine();
									try {
										assignment.setPossible(possible);
									} catch (IllegalArgumentException e) {
										System.err.println(String.format(Message.ASSIGNMENT_UPDATE.toString(), e.getMessage()));
									}
								} else {
									System.err.println(Message.BAD_ATTRIBUTE);
								}
							} else {
								System.err.println(Message.BAD_ASSIGNMENT);
							}
						} else {
							System.err.println(Message.BAD_CATEGORY);
						}
					}

				} else if (command.equals("remove category")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.print(LEADER + "category id: ");
						String id = scanner.nextLine();
						Optional<Category> result = getCategory(id);

						if (result.isPresent()) {
							course.categories.remove(result.get().id);
						} else {
							System.err.println(Message.BAD_CATEGORY);
						}
					}

				} else if (command.equals("remove assignment")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						System.out.print(LEADER + "category id: ");
						String categoryId = scanner.nextLine();
						Optional<Category> categoryResult = getCategory(categoryId);

						if (categoryResult.isPresent()) {
							Category category = categoryResult.get();
							System.out.print(LEADER + "assignment id: ");
							String assignmentId = scanner.nextLine();
							Optional<Assignment> assignmentResult = getAssignment(category, assignmentId);

							if (assignmentResult.isPresent()) {
								category.assignments.remove(assignmentResult.get().id);
							} else {
								System.err.println(Message.BAD_ASSIGNMENT);
							}
						} else {
							System.err.println(Message.BAD_CATEGORY);
						}
					}

				} else if (command.equals("grade")) {

					if (course == null) {
						System.err.println(Message.NO_COURSE);
					} else {
						double score;
						try {
							score = course.score();
							System.out.println(score);
						} catch (ScoringException e) {
							System.err.println(String.format(Message.SCORING_ERROR.toString(), e.getMessage()));
						}
					}

				} else if (command.equals("help")) {

					System.out.println("Commands:");
					System.out.println("\topen <file>");
					System.out.println("\tsave <file>");
					System.out.println("\tnew course");
					System.out.println("\tcourse info");
					System.out.println("\tcategories");
					System.out.println("\tcategory info");
					System.out.println("\tassignments");
					System.out.println("\tassignment info");
					System.out.println("\tadd category");
					System.out.println("\tadd assignment");
					System.out.println("\tupdate category");
					System.out.println("\tupdate assignment");
					System.out.println("\tremove category");
					System.out.println("\tremove assignment");
					System.out.println("\tgrade");
					System.out.println("\thelp");
					System.out.println("Send EOF (Ctrl+D) to exit.");

				} else if (!command.isEmpty()) {
					System.err.println(Message.BAD_COMMAND);
				}
			}
			System.out.print(PROMPT);
		}
		System.out.println();
	}

	protected Optional<Category> getCategory(String id) {
		return course.categories.values().stream()
			.filter(category -> category.id.toString().startsWith(id))
			.findAny();
	}

	protected Optional<Assignment> getAssignment(Category category, String id) {
		return category.assignments.values().stream()
			.filter(assignment -> assignment.id.toString().startsWith(id))
			.findAny();
	}

	public static void main(String[] args) {
		if (args.length == 0) {

			(new CliClient()).main();

		} else if (args.length == 1 && args[0].equals("grade")) {

			Course course;
			try (
				ObjectInputStream stream = new ObjectInputStream(System.in);
			) {
				course = (Course) stream.readObject();
			} catch (IOException | ClassNotFoundException e) {
				System.err.println(String.format(Message.COURSE_READ.toString(), e.getMessage()));
				return;
			}

			try {
				System.out.println(course.score());
			} catch (ScoringException e) {
				System.err.println(String.format(Message.SCORING_ERROR.toString(), e.getMessage()));
			}

		} else {

			String className = CliClient.class.getSimpleName();
			System.err.print(
				"Usage:\n" +
				String.format("\tjava %s\n", className) +
				String.format("\tjava %s grade <file\n", className)
			);

		}
	}
}
