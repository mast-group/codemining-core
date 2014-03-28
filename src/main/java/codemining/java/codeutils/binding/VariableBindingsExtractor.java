/**
 * 
 */
package codemining.java.codeutils.binding;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;

import codemining.java.codeutils.JavaTokenizer;
import codemining.languagetools.NameBinding;

/**
 * Retrieve the variable bindings, given an ASTNode.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class VariableBindingsExtractor extends
		AbstractJavaNameBindingsExtractor {

	@Override
	public List<NameBinding> getNameBindings(final ASTNode node,
			final String sourceCode) {
		final JavaTokenizer tokenizer = new JavaTokenizer();
		// TODO
		return null;
	}

}
