/**
 *
 */
package codemining.java.codedata;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Utilities for identifiers.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 *
 */
public class JavaIdentifierUtils {

	private JavaIdentifierUtils() {
		// No instantiation
	}

	public static List<String> getNameParts(final String name) {
		List<String> nameParts = Lists.newArrayList();
		for (String snakecasePart : name.split("_")) {
			for (String w : snakecasePart
					.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
				nameParts.add(w.toLowerCase());
			}
		}
		return nameParts;
	}

}
