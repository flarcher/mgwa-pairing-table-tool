package org.mgwa.w40k.pairing;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AssignmentTest {

	@Test
	public void testAssignmentCompletion_singleTable() {
		Assignment emptyAssignment = Assignment.createEmpty(1);
		List<Army> rowArmies = Army.createArmies(Arrays.asList("1"), true);
		List<Army> colArmies = Army.createArmies(Arrays.asList("A"), false);
		Assert.assertEquals(1, emptyAssignment.completedAssignments(rowArmies, colArmies)
				.distinct()
				//.peek(assign -> System.out.println(assign.toString(rowArmies, colArmies)))
				.peek(assign -> Assert.assertTrue(assign.isComplete()))
				.count());
	}

	@Test
	public void testAssignmentCompletion_twoTables() {
		Assignment emptyAssignment = Assignment.createEmpty(2);
		List<Army> rowArmies = Army.createArmies(Arrays.asList("1", "2"), true);
		List<Army> colArmies = Army.createArmies(Arrays.asList("A", "B"), false);
		Assert.assertEquals(4, emptyAssignment.completedAssignments(rowArmies, colArmies)
				.distinct()
				.peek(assign -> System.out.println(assign.toString(rowArmies, colArmies)))
				.peek(assign -> Assert.assertTrue(assign.isComplete()))
				.count());
	}
}
