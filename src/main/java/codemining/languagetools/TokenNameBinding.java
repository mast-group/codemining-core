/**
 * 
 */
package codemining.languagetools;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * A single name binding in source code. A struct-like class.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class TokenNameBinding implements Serializable {
	private static final long serialVersionUID = 2020613810485746430L;

	/**
	 * The tokens of source code.
	 */
	public final List<String> sourceCodeTokens;

	/**
	 * The positions in sourceCodeTokens that contain the given name.
	 */
	public final Set<Integer> nameIndexes;

	public TokenNameBinding(final Set<Integer> nameIndexes,
			final List<String> sourceCodeTokens) {
		checkArgument(nameIndexes.size() > 0);
		checkArgument(sourceCodeTokens.size() > 0);
		this.nameIndexes = Collections.unmodifiableSet(nameIndexes);
		this.sourceCodeTokens = Collections.unmodifiableList(sourceCodeTokens);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TokenNameBinding other = (TokenNameBinding) obj;
		if (nameIndexes == null) {
			if (other.nameIndexes != null) {
				return false;
			}
		} else if (!nameIndexes.equals(other.nameIndexes)) {
			return false;
		}
		if (sourceCodeTokens == null) {
			if (other.sourceCodeTokens != null) {
				return false;
			}
		} else if (!sourceCodeTokens.equals(other.sourceCodeTokens)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(sourceCodeTokens, nameIndexes);
	}

	/**
	 * Rename this name to the given binding. The source code tokens included in
	 * this struct, now represent the new structure.
	 * 
	 * @param name
	 * @return
	 */
	public TokenNameBinding renameTo(final String name) {
		final List<String> renamedCode = Lists.newArrayList(sourceCodeTokens);
		for (final int position : nameIndexes) {
			renamedCode.set(position, name);
		}
		return new TokenNameBinding(nameIndexes, renamedCode);
	}

	@Override
	public String toString() {
		return sourceCodeTokens.get(nameIndexes.iterator().next())
				+ nameIndexes;
	}
}
