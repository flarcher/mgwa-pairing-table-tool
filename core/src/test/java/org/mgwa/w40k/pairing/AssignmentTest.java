package org.mgwa.w40k.pairing;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AssignmentTest {

	@Test
	public void testAssignmentCompletion_singleTable() {
		Assignment emptyAssignment = Assignment.createEmpty(1);
		List<Army> rowArmies = Army.createArmies(Arrays.asList("1"), true);
		List<Army> colArmies = Army.createArmies(Arrays.asList("A"), false);
		List<Assignment> result = emptyAssignment.completedAssignments(rowArmies, colArmies)
				.distinct()
				.peek(assign -> System.out.println(assign.toString(rowArmies, colArmies)))
				.peek(assign -> Assert.assertTrue(assign.isComplete()))
				.collect(Collectors.toList());
		Assert.assertEquals(1, result.size());
		Assignment expected = Assignment
				.createEmpty(1)
				.assign(0, rowArmies.get(0), colArmies.get(0));
		Assert.assertEquals(expected, result.get(0));
	}

	private void checkCombinationUniqueness(
			List<Army> rowArmies,
			List<Army> colArmies,
			Set<Assignment> result,
			boolean isFull
	) {
		Assert.assertEquals(rowArmies.size(), colArmies.size());
		IntStream.range(0, rowArmies.size()).forEach(rowIndex -> {
			Set<OptionalInt> tables = result.stream()
				.map(a -> a.getTableOfRow(rowIndex))
				.collect(Collectors.toSet());
			Assert.assertTrue(tables.stream().allMatch(OptionalInt::isPresent));
			if (isFull) {
				Assert.assertEquals(rowArmies.size(), tables.size());
			}
		});
		IntStream.range(0, colArmies.size()).forEach(colIndex -> {
			Set<OptionalInt> tables = result.stream()
					.map(a -> a.getTableOfColumn(colIndex))
					.collect(Collectors.toSet());
			Assert.assertTrue(tables.stream().allMatch(OptionalInt::isPresent));
			if (isFull) {
				Assert.assertEquals(colArmies.size(), tables.size());
			}
		});
	}

	@Test
	public void testAssignmentCompletion_twoTables() {
		Assignment emptyAssignment = Assignment.createEmpty(2);
		List<Army> rowArmies = Army.createArmies(Arrays.asList("1", "2"), true);
		List<Army> colArmies = Army.createArmies(Arrays.asList("A", "B"), false);
		Set<Assignment> result = emptyAssignment.completedAssignments(rowArmies, colArmies)
				.distinct()
				.peek(assign -> System.out.println(assign.toString(rowArmies, colArmies)))
				.peek(assign -> Assert.assertTrue(assign.isComplete()))
				.collect(Collectors.toSet());
		Assert.assertEquals(4, result.size());
		checkCombinationUniqueness(rowArmies, colArmies, result, true);
	}

	@Test
	public void testAssignmentCompletion_oneTableAssigned_oneTableLeft() {
		Assignment emptyAssignment = Assignment.createEmpty(2);
		List<Army> rowArmies = Army.createArmies(Arrays.asList("1", "2"), true);
		List<Army> colArmies = Army.createArmies(Arrays.asList("A", "B"), false);
		Set<Assignment> result = emptyAssignment
				.assign(0, rowArmies.get(0) /*1*/, colArmies.get(0)/*A*/) // Arbitrary initial assignment
				.completedAssignments(rowArmies, colArmies)
				.distinct()
				.peek(assign -> System.out.println(assign.toString(rowArmies, colArmies)))
				.peek(assign -> Assert.assertTrue(assign.isComplete()))
				.collect(Collectors.toSet());
		Assert.assertEquals(1, result.size());
		checkCombinationUniqueness(rowArmies, colArmies, result, false);
	}

	@Test
	public void testAssignmentCompletion_oneTableAssigned_twoTablesLeft() {
		Assignment emptyAssignment = Assignment.createEmpty(3);
		List<Army> rowArmies = Army.createArmies(Arrays.asList("1", "2", "3"), true);
		List<Army> colArmies = Army.createArmies(Arrays.asList("A", "B", "C"), false);
		Set<Assignment> result = emptyAssignment
				.assign(0, rowArmies.get(0) /*1*/, colArmies.get(0)/*A*/) // Arbitrary initial assignment
				.completedAssignments(rowArmies, colArmies)
				.distinct()
				.peek(assign -> System.out.println(assign.toString(rowArmies, colArmies)))
				.peek(assign -> Assert.assertTrue(assign.isComplete()))
				.collect(Collectors.toSet());
		Assert.assertEquals(4, result.size());
		checkCombinationUniqueness(rowArmies, colArmies, result, false);
	}

}
