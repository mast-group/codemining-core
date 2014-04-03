/**
 * 
 */
package codemining.languagetools;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.dom.ASTNode;

import com.google.common.collect.Multimap;

/**
 * A interface for extracting scoped related information.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public interface IScopeExtractor {
	Multimap<Scope, String> getFromFile(final File file) throws IOException;

	Multimap<Scope, String> getFromNode(final ASTNode node);

	Multimap<Scope, String> getFromString(final String code,
			final ParseType parseType);
}
