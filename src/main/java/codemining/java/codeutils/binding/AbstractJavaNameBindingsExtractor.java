/**
 * 
 */
package codemining.java.codeutils.binding;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;

import codemining.java.codeutils.JavaASTExtractor;
import codemining.java.codeutils.JavaTokenizer;
import codemining.languagetools.AbstractNameBindingsExtractor;
import codemining.languagetools.ResolvedSourceCode;
import codemining.languagetools.TokenNameBinding;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * A name bindings extractor interface for Java.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public abstract class AbstractJavaNameBindingsExtractor extends
		AbstractNameBindingsExtractor {

	public static ResolvedSourceCode getResolvedSourceCode(
			final String sourceCode, final Set<Set<ASTNode>> nodeBindings) {
		final JavaTokenizer tokenizer = new JavaTokenizer();
		final SortedMap<Integer, String> tokenPositions = tokenizer
				.tokenListWithPos(sourceCode.toCharArray());
		final SortedMap<Integer, Integer> positionToIndex = getTokenIndexForPostion(tokenPositions);
		final List<String> tokens = Lists.newArrayList(tokenPositions.values());

		final Multimap<String, TokenNameBinding> bindings = ArrayListMultimap
				.create();

		for (final Set<ASTNode> boundName : nodeBindings) {
			final List<Integer> boundPositions = Lists.newArrayList();
			for (final ASTNode name : boundName) {
				// Convert position to token index and add
				final int tokenIdx = positionToIndex.get(name
						.getStartPosition());
				boundPositions.add(tokenIdx);
			}
			bindings.put(tokens.get(boundPositions.get(0)),
					new TokenNameBinding(Sets.newTreeSet(boundPositions),
							tokens));
		}

		return new ResolvedSourceCode(tokens, bindings);
	}

	/**
	 * Get the token bindings given the ASTNode bindings and the source code
	 * positions.
	 * 
	 * @param sourceCode
	 * @param nodeBindings
	 * @return
	 */
	public static List<TokenNameBinding> getTokenBindings(
			final String sourceCode, final Set<Set<ASTNode>> nodeBindings) {
		final JavaTokenizer tokenizer = new JavaTokenizer();
		final SortedMap<Integer, String> tokenPositions = tokenizer
				.tokenListWithPos(sourceCode.toCharArray());
		final SortedMap<Integer, Integer> positionToIndex = getTokenIndexForPostion(tokenPositions);
		final List<String> tokens = Lists.newArrayList(tokenPositions.values());

		final List<TokenNameBinding> bindings = Lists.newArrayList();

		for (final Set<ASTNode> boundName : nodeBindings) {
			final List<Integer> boundPositions = Lists.newArrayList();
			for (final ASTNode name : boundName) {
				// Convert position to token index and add
				final int tokenIdx = positionToIndex.get(name
						.getStartPosition());
				boundPositions.add(tokenIdx);
			}
			bindings.add(new TokenNameBinding(Sets.newTreeSet(boundPositions),
					tokens));
		}

		return bindings;
	}

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
		return new JavaASTExtractor(false);
	}

	/**
	 * Return a set of sets of SimpleName ASTNode objects that are bound
	 * together
	 * 
	 * @param node
	 * @return
	 */
	public abstract Set<Set<ASTNode>> getNameBindings(final ASTNode node);

	/**
	 * Get the name bindings for the given ASTNode. This assumes that the
	 * ASTNode has been produced by the sourceCode, code with no variation.
	 * 
	 * @param node
	 *            the ASTNode where bindings will be computed.
	 * @param sourceCode
	 *            the sourceCode from which the ASTNode has been extracted.
	 * @return
	 */
	public final List<TokenNameBinding> getNameBindings(final ASTNode node,
			final String sourceCode) {
		final Set<Set<ASTNode>> nodeBindings = getNameBindings(node);
		return getTokenBindings(sourceCode, nodeBindings);
	}

	@Override
	public List<TokenNameBinding> getNameBindings(final File f)
			throws IOException {
		final JavaASTExtractor ex = createExtractor();
		return getNameBindings(ex.getAST(f), FileUtils.readFileToString(f));
	}

	@Override
	public List<TokenNameBinding> getNameBindings(final String code) {
		final JavaASTExtractor ex = createExtractor();
		try {
			return getNameBindings(ex.getBestEffortAstNode(code), code);
		} catch (final Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public ResolvedSourceCode getResolvedSourceCode(final File f)
			throws IOException {
		final JavaASTExtractor ex = createExtractor();
		return getResolvedSourceCode(FileUtils.readFileToString(f),
				getNameBindings(ex.getAST(f)));
	}

	@Override
	public ResolvedSourceCode getResolvedSourceCode(final String code) {
		final JavaASTExtractor ex = createExtractor();
		try {
			return getResolvedSourceCode(code,
					getNameBindings(ex.getBestEffortAstNode(code)));
		} catch (final Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
