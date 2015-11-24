package Kinfolk;

import java.util.Scanner;

/**
 * 
 * @author Vance Miller, Tyler Newsome, Sarah Rust
 * 
 * Goal:
 * 	Determine relationship between person and their relative,
 * 	where person's and relative's value is determined from a binary tree
 * 
 * Input:
 * 	%d %d %c (person, relative, gender ('M' or 'F'))
 * 	person and relative are non-equal integers in [0, 32767]
 * 	gender is applied to the relative
 * 	input should be read until negative number
 * 
 * Output:
 * 	1. Can only use the phrases:
 * 		"parent", "child", "nephew", "niece", "aunt", "uncle", "cousin", "grand",
 * 		"great-", "1st", "2nd", "3rd", "once removed", "twice removed", "thrice removed"
 * 	2. No more than 2 "great-" prefixes can be applied
 * 	3. If “1st”, “2nd”, or “3rd” is used, it should be separated from the following part
 * 		of the line by a single blank.
 * 	4. If “once removed”, “twice removed”, or “thrice removed” is used, it must be separated
 * 		from the preceding part of the line by a single blank.
 * 	5. If it is not possible to describe the relationship of relative to person
 * 		under the above limitations, then print “kin”
 * 
 * Examples:
 * 	Input:
 * 		1 5 M
 * 		1 11 F
 * 		0 8 F
 * 		5 7 M
 * 		0 32767 F
 * 		-1 -1 M
 * 	Output:
 * 		nephew
 * 		grandniece
 * 		great-grandchild
 * 		1st cousin once removed
 * 		kin
 * 
 * Methods (in order):
 *	public static void main(String[] args)
 * 	public static void relationship(int a, int b, char s)
 *	private static String childCousinNiece(int a, int b, char s)
 *	private static String parentCousinAunt(int a, int b, char s)
 *	private static String siblingCousin(int a, int b)
 *	private static int distanceToCommonAncestor(int a, int b)
 *	private static String removify(int levelHops)
 *	private static String grandify(int levelHops, String relationship)
 *	private static String greatify(int levelHops)
 *	private static String numberify(int n)
 *	private static int parent(int me)
 *	private static int level(int me)	
 *
 */
public class Main {
	private static final int MAX_GREATS = 2;
	private static final int MAX_CUZS = 3;

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int a, b;
		char s;
		while ((a = sc.nextInt()) != -1) {
			b = sc.nextInt();
			s = sc.next().charAt(0);

			relationship(a, b, s);
		}
		sc.close();
	}

	/**
	 * Calculates the relationship between a and b, with respect to a.
	 * 
	 * @param a
	 *            Person
	 * @param b
	 *            Relative
	 * @param s
	 *            Gender 'M' or 'F'
	 */
	public static void relationship(int a, int b, char s) {
		int levelA = level(a);
		int levelB = level(b);
		if (levelA == levelB) {
			// Sibling/Cousin
			System.out.println(siblingCousin(a, b));
		} else if (levelA > levelB) {
			// Parent, cousin removed, aunt/uncle
			System.out.println(parentCousinAunt(a,b,s));
		} else {
			// Child, cousin removed, niece/nephew,
			System.out.println(childCousinNiece(a,b,s));
		}
	}

	/**
	 * 
	 * @param a is always greater than b
	 * @param b
	 * @param s gender
	 * @return String saying relationship between a and b, with respect to a
	 */
	private static String childCousinNiece(int a, int b, char s) {
		int ancestor = b;
		int levelA = level(a);
		int levelHops = 0;
		while ((level(ancestor = parent(ancestor))) > levelA) {
			levelHops++;
		}
		if (ancestor == a) {
			if (levelHops > MAX_GREATS+1) {
				return "kin";
			} else {
				return greatify(levelHops) + grandify(levelHops, "child");
			}
		} else {
			String type = siblingCousin(ancestor, a);
			if (type.contains("cousin")) {
				if (levelHops+1 >MAX_CUZS) {
					return "kin";
				} else {
					return type + removify(levelHops+1) + " removed";
				}
			} else if (type.contains("sibling")) {
				if (levelHops > MAX_GREATS+1) {
					return "kin";
				}
				if (s == 'M') { //male
					return greatify(levelHops) + grandify(levelHops, "nephew");
				} else { //female
					return greatify(levelHops) + grandify(levelHops, "niece");
				}
			} else {
				return "kin";
			}
		}
	}

	/**
	 * 
	 * @param a is always smaller than b
	 * @param b
	 * @param s gender
	 * @return String saying relationship between a and b, with respect to a
	 */
	private static String parentCousinAunt(int a, int b, char s) {
		int ancestor = a;
		int levelB = level(b);
		int levelHops = 0;
		while ((level(ancestor = parent(ancestor))) > levelB) {
			levelHops++;
		}
		if (ancestor == b) {
			if (levelHops > MAX_GREATS+1)
				return "kin";
			else
				return greatify(levelHops) + grandify(levelHops, "parent");
		} else {
			String type = siblingCousin(ancestor, b);
			if (type.contains("cousin")) {
				if (levelHops+1 > MAX_CUZS) {
					return "kin";
				} else {
					return type + removify(levelHops+1);
				}
			} else if (type.contains("sibling")){ //sibling of ancestor
				if (levelHops > MAX_GREATS+1) {
					return "kin";
				}
				if (s == 'M') { // male
					return greatify(levelHops) + grandify(levelHops, "uncle");
				} else { // female
					return greatify(levelHops) + grandify(levelHops, "aunt");
				}
			} else {
				return "kin";
			}
		}	
	}
	
	/**
	 * 
	 * @param a person
	 * @param b relative, guaranteed to be on same level as a
	 * @return String saying if sibling, cousin, or kin
	 */
	private static String siblingCousin(int a, int b) {
		int distanceToCommonAncestor = distanceToCommonAncestor(a, b);
		switch (distanceToCommonAncestor) {
		case 0:
			// invalid
			return "";
		case 1:
			return "sibling";
		default:
			if (distanceToCommonAncestor-1 > MAX_CUZS) {
				return "kin";
			} else {
				return numberify(distanceToCommonAncestor - 1) + " cousin";
			}
		}
	}
	
	/**
	 * 
	 * @param a person
	 * @param b guaranteed to be on same level as a
	 * @return number of levels until common ancestor found
	 */
	private static int distanceToCommonAncestor(int a, int b) {
		if (a == b) {
			return 0;
		} else {
			return 1 + distanceToCommonAncestor(parent(a), parent(b));
		}
	}
	
	/**
	 * 
	 * @param levelHops number of ancestors of a till on same level as b
	 * @return String to append how many times they are cousins removed
	 */	
	private static String removify(int levelHops) {
		switch(levelHops) {
		case 1:
			return " once removed";
		case 2:
			return " twice removed";
		case 3:
			return " thrice removed";
		default:
			return "";
		}
	}
	
	/**
	 * 
	 * @param levelHops determine if grand should be prefixed
	 * @param relationship to append with grand
	 * @return String saying relationship of a to b with respect to a
	 */
	private static String grandify(int levelHops, String relationship) {
		if (levelHops > 0) {
			relationship = "grand" + relationship;
		}
		return relationship;
	}

	/**
	 * 
	 * @param levelHops number of ancestors of a till on same level as b
	 * @return String to append how many greats to prefix
	 */
	private static String greatify(int levelHops) {
		String greats = "";
		while (levelHops > 1) {
			greats += "great-";
			levelHops--;
		}
		return greats;
	}
	
	/**
	 * 
	 * @param n integer
	 * @return String that converted n to text
	 */
	private static String numberify(int n) {
		switch (n) {
		case 1:
			return "1st";
		case 2:
			return "2nd";
		case 3:
			return "3rd";
		default:
			return n + "th";
		}
	}
	
	/**
	 * 
	 * @param me person
	 * @return find parent "node" of me
	 */
	private static int parent(int me) {
		return (me - 1) / 2;
	}

	/**
	 * 
	 * @param me person
	 * @return determine level in the tree that me is on
	 */
	private static int level(int me) {
		return (int) (Math.log(me + 1) / Math.log(2));
	}

}
