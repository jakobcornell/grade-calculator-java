package com.jakobcornell.gradecalculator;

import com.jakobcornell.gradecalculator.model.Course;
import com.jakobcornell.gradecalculator.model.Category;
import com.jakobcornell.gradecalculator.model.Assignment;

import java.util.Scanner;
import java.util.Optional;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class CliClient {
	protected static final String PROMPT = "> ";

	protected enum Messages {
		BAD_COMMAND("Unrecognized command. Try \"help\" for a list of commands."),
		NO_COURSE("No course loaded. Use \"new course\" or \"open\" to load one."),
		BAD_CATEGORY("Unrecognized category."),
		CATEGORY_CREATE("Error creating category: %s"),
		FILENAME_MISSING("Filename missing."),
		COURSE_READ("Error reading course: %s"),
		COURSE_WRITE("Error writing course: %s");

		protected String message;
	
		private Messages(String message) {
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
							System.err.println(String.format(Messages.COURSE_READ.toString(), e.getMessage()));
						}
					} else {
						System.err.println(Messages.FILENAME_MISSING);
					}

				} else if (tokens[0].equals("save")) {

					if (tokens.length == 2) {
						try (
							FileOutputStream fileOut = new FileOutputStream(tokens[1]);
							ObjectOutputStream courseOut = new ObjectOutputStream(fileOut);
						) {
							courseOut.writeObject(course);
						} catch (IOException e) {
							System.err.println(String.format(Messages.COURSE_WRITE.toString(), e.getMessage()));
						}
					}

				} else if (command.equals("new course")) {

					System.out.print("course name: ");
					course = new Course(scanner.nextLine());

				} else if (command.equals("course info")) {

					if (course == null) {
						System.err.println(Messages.NO_COURSE);
					} else {
						System.out.println(String.format("name: %s", course.name));
					}

				} else if (command.equals("categories")) {

					if (course == null) {
						System.err.println(Messages.NO_COURSE);
					} else {
						System.out.println(String.format("categories (%d):", course.categories.size()));
						for (Category category : course.categories.values()) {
							System.out.println(String.format("\t%s : %s", category.id, category.name));
						}
					}

				} else if (command.equals("category info")) {

					if (course == null) {
						System.err.println(Messages.NO_COURSE);
					} else {
						System.out.print("category id: ");
						String id = scanner.nextLine();
						Optional<Category> result = getCategory(id);
						if (result.isPresent()) {
							Category category = result.get();
							System.out.println(String.format("category %s:", category.id));
							System.out.println(String.format("\tname: %s", category.name));
							System.out.println(String.format("\tweight: %f", category.getWeight()));
							System.out.println(String.format("\tuses weights: %b", category.useWeights));
						} else {
							System.err.println(Messages.BAD_CATEGORY);
						}
					}

				} else if (command.equals("assignments")) {

					if (course == null) {
						System.err.println(Messages.NO_COURSE);
					} else {
						System.out.print("category id: ");
						String id = scanner.nextLine();
						Optional<Category> result = getCategory(id);
						if (result.isPresent()) {
							Category category = result.get();
							System.out.println(String.format("assignments (%d):", category.assignments.size()));
							for (Assignment assignment : category.assignments.values()) {
								System.out.println(String.format("\t%s : %s", assignment.id, assignment.name));
							}
						} else {
							System.err.println(Messages.BAD_CATEGORY);
						}
					}

				} else if (command.equals("assignment info")) {

				} else if (command.equals("add category")) {

					System.out.print("name: ");
					String name = scanner.nextLine();

					System.out.print("weight: ");
					double weight = scanner.nextDouble();
					scanner.nextLine();

					System.out.print("uses weights (true|false): ");
					boolean useWeights = scanner.nextBoolean();
					scanner.nextLine();

					Category category;
					try {
						category = new Category(name, weight, useWeights);
						course.categories.put(category.id, category);
					} catch (IllegalArgumentException e) {
						System.err.println(String.format(Messages.CATEGORY_CREATE.toString(), e.getMessage()));
					}

				} else if (command.equals("add assignment")) {
				
				} else if (command.equals("update category")) {
				
				} else if (command.equals("update assignment")) {
					
				} else if (command.equals("remove category")) {
					
				} else if (command.equals("remove assignment")) {
					
				} else if (command.equals("grade")) {
					
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

				} else {
					System.err.println(Messages.BAD_COMMAND);
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

	public static void main(String[] args) {
		if (args.length == 0) {
			(new CliClient()).main();
		} else if (args.length == 2 && args[0].equals("grade")) {
		
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
