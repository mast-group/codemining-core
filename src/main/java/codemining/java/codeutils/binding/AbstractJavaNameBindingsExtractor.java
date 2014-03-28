/**
 * 
 */
package codemining.java.codeutils.binding;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;

import codemining.java.codeutils.JavaASTExtractor;
import codemining.languagetools.INameBindingsExtractor;
import codemining.languagetools.NameBinding;

import com.google.common.collect.Maps;

/**
 * A name bindings extractor interface for Java.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public abstract class AbstractJavaNameBindingsExtractor implements
		INameBindingsExtractor {

	/**
	 * Return the token index for the given position.
	 * 
	 * @param sourceCode
	 * @return
	 */
	protected static SortedMap<Integer, Integer> getTokenIndexForPostion(
			final SortedMap<Integer, String> tokenPositions) {
		final SortedMap<Integer, Integer> positionToIndex = Maps.newTreeMap();
		int i = 0;
		for (final int position : tokenPositions.keySet()) {
			positionToIndex.put(position, i);
			i++;
		}
		return positionToIndex;
	}

	protected JavaASTExtractor createExtractor() {
		return new JavaASTExtractor(true);
	}

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
		final JavaASTExtractor ex = createExtractor();
		return getNameBindings(ex.getAST(f), FileUtils.readFileToString(f));
	}

	@Override
	public List<NameBinding> getNameBindings(final String code) {
		final JavaASTExtractor ex = createExtractor();
		try {
			return getNameBindings(ex.getBestEffortAstNode(code), code);
		} catch (final Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
