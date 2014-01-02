/**
 * 
 */
package codemining.java.codeutils;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import codemining.languagetools.ITokenizer;
import codemining.util.SettingsLoader;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Annotate tokens with their AST depth. TODO: Duplicate code with
 * ASTAnnotatedTokenizer
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class ASTDepthAnnotatedTokenizer implements ITokenizer {

	/**
	 * Visit all AST nodes and annotate tokens.
	 * 
	 */
	private class TokenDecorator extends ASTVisitor {
		final SortedMap<Integer, FullToken> baseTokens;
		final SortedMap<Integer, FullToken> annotatedTokens;

		public TokenDecorator(final SortedMap<Integer, FullToken> baseTokens) {
			this.baseTokens = baseTokens;
			annotatedTokens = Maps.newTreeMap();
		}

		SortedMap<Integer, FullToken> getAnnotatedTokens(final ASTNode node) {
			annotatedTokens.putAll(baseTokens);
			node.accept(this);
			checkArgument(baseTokens.size() == annotatedTokens.size());
			return annotatedTokens;
		}

		@Override
		public void preVisit(final ASTNode node) {
			final int fromPosition = node.getStartPosition();
			final int endPosition = fromPosition + node.getLength();
			int depth = 0;
			ASTNode currentNode = node;
			while (currentNode != null) {
				depth++;
				currentNode = currentNode.getParent();
			}
			final SortedMap<Integer, FullToken> nodeTokens = baseTokens.subMap(
					fromPosition, endPosition);
			for (final Entry<Integer, FullToken> token : nodeTokens.entrySet()) {
				if (token.getValue().token.startsWith("WS_")
						&& baseTokenizer instanceof JavaWhitespaceTokenizer) {
					annotatedTokens.put(
							token.getKey(),
							new FullToken(token.getValue().token, token
									.getValue().tokenType));
				} else {
					annotatedTokens.put(token.getKey(),
							new FullToken(
									token.getValue().token + "_d" + depth,
									token.getValue().tokenType));
				}
			}
			super.preVisit(node);
		}
	}

	private static final Logger LOGGER = Logger
			.getLogger(ASTDepthAnnotatedTokenizer.class.getName());

	private static final long serialVersionUID = -4518140661119781220L;

	private final ITokenizer baseTokenizer;

	public ASTDepthAnnotatedTokenizer() {
		try {
			final Class<? extends ITokenizer> tokenizerClass = (Class<? extends ITokenizer>) Class
					.forName(SettingsLoader.getStringSetting("baseTokenizer",
							"codemining.java.codeutils.JavaCodeTokenizer"));
			baseTokenizer = tokenizerClass.newInstance();
		} catch (final ClassNotFoundException e) {
			LOGGER.severe(ExceptionUtils.getFullStackTrace(e));
			throw new IllegalArgumentException(e);
		} catch (final InstantiationException e) {
			LOGGER.severe(ExceptionUtils.getFullStackTrace(e));
			throw new IllegalArgumentException(e);
		} catch (final IllegalAccessException e) {
			LOGGER.severe(ExceptionUtils.getFullStackTrace(e));
			throw new IllegalArgumentException(e);
		}
	}

	public ASTDepthAnnotatedTokenizer(final ITokenizer base) {
		baseTokenizer = base;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#fullTokenListWithPos(char[])
	 */
	@Override
	public SortedMap<Integer, FullToken> fullTokenListWithPos(final char[] code) {
		final SortedMap<Integer, FullToken> toks = baseTokenizer
				.fullTokenListWithPos(code);
		return toks;
	}

	/**
	 * Return the tokens annotated.
	 * 
	 * @param code
	 * @return
	 */
	private SortedMap<Integer, FullToken> getAnnotatedTokens(final char[] code) {
		final JavaASTExtractor ex = new JavaASTExtractor(false);
		final ASTNode cu = ex.getASTNode(code);

		final SortedMap<Integer, FullToken> baseTokens = baseTokenizer
				.fullTokenListWithPos(code);
		final TokenDecorator dec = new TokenDecorator(baseTokens);
		return dec.getAnnotatedTokens(cu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#getFileFilter()
	 */
	@Override
	public AbstractFileFilter getFileFilter() {
		return baseTokenizer.getFileFilter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#getIdentifierType()
	 */
	@Override
	public String getIdentifierType() {
		return baseTokenizer.getIdentifierType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * codemining.languagetools.ITokenizer#getTokenFromString(java.lang.String)
	 */
	@Override
	public FullToken getTokenFromString(final String token) {
		throw new IllegalArgumentException(
				"ASTAnnotatedTokenizer cannot return a token from a single string.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#getTokenListFromCode(char[])
	 */
	@Override
	public List<FullToken> getTokenListFromCode(final char[] code) {
		final List<FullToken> tokens = Lists.newArrayList();
		for (final Entry<Integer, FullToken> token : getAnnotatedTokens(code)
				.entrySet()) {
			tokens.add(token.getValue());
		}
		return tokens;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#tokenListFromCode(char[])
	 */
	@Override
	public List<String> tokenListFromCode(final char[] code) {
		final List<String> tokens = Lists.newArrayList();
		for (final Entry<Integer, FullToken> token : getAnnotatedTokens(code)
				.entrySet()) {
			tokens.add(token.getValue().token);
		}
		return tokens;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#tokenListWithPos(char[])
	 */
	@Override
	public SortedMap<Integer, String> tokenListWithPos(final char[] code) {
		final SortedMap<Integer, String> tokens = Maps.newTreeMap();
		for (final Entry<Integer, FullToken> token : getAnnotatedTokens(code)
				.entrySet()) {
			tokens.put(token.getKey(), token.getValue().token);
		}
		return tokens;
	}

}
