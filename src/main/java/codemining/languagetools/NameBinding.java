/**
 * 
 */
package codemining.languagetools;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * A single name binding in source code. A struct-like class.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class NameBinding implements Serializable {
	private static final long serialVersionUID = 2020613810485746430L;

	/**
	 * The tokens of source code.
	 */
	final List<String> sourceCodeTokens;

	/**
	 * The positions in sourceCodeTokens that contain the given name.
	 */
	final List<Integer> nameIndexes;

	public NameBinding(final List<Integer> nameIndexes,
			final List<String> sourceCodeTokens) {
		this.nameIndexes = Collections.unmodifiableList(nameIndexes);
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
		final NameBinding other = (NameBinding) obj;
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
	public NameBinding renameTo(final String name) {
		final List<String> renamedCode = Lists.newArrayList(sourceCodeTokens);
		for (final int position : nameIndexes) {
			renamedCode.set(position, name);
		}
		return new NameBinding(nameIndexes, renamedCode);
	}
}
