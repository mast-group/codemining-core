/**
 * 
 */
package codemining.java.codeutils.binding;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;

import codemining.java.codeutils.JavaASTExtractor;
import codemining.languagetools.INameBindingsExtractor;
import codemining.languagetools.NameBinding;

/**
 * A name bindings extractor interface for Java
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public abstract class AbstractJavaNameBindingsExtractor implements
		INameBindingsExtractor {

	/**
	 * Get the name bindings for the given ASTNode.
	 * 
	 * @param node
	 *            the ASTNode where bindings will be computed.
	 * @param sourceCode
	 *            the sourceCode from which the ASTNode has been extracted.
	 * @return
	 */
	public abstract List<NameBinding> getNameBindings(final ASTNode node,
			final String sourceCode);

	@Override
	public List<NameBinding> getNameBindings(final File f) throws IOException {
		final JavaASTExtractor ex = new JavaASTExtractor(true);
		return getNameBindings(ex.getAST(f), FileUtils.readFileToString(f));
	}

	@Override
	public List<NameBinding> getNameBindings(final String code) {
		final JavaASTExtractor ex = new JavaASTExtractor(true);
		try {
			return getNameBindings(ex.getBestEffortAstNode(code), code);
		} catch (final Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
