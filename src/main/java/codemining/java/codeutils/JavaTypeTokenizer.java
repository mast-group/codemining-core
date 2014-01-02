/**
 * 
 */
package codemining.java.codeutils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.dom.ASTNode;

import codemining.languagetools.ITokenizer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A best-effort type tokenizer. This tokenizer substitutes variable tokens with
 * their types in the special form 'var%TypeName%'
 * 
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class JavaTypeTokenizer implements ITokenizer {

	private static final long serialVersionUID = -5145031374089339996L;

	final ITokenizer baseTokenizer = new JavaTokenizer();

	public static final String IDENTIFIER_ID = Integer
			.toString(ITerminalSymbols.TokenNameIdentifier);

	@Override
	public SortedMap<Integer, FullToken> fullTokenListWithPos(char[] code) {
		final SortedMap<Integer, FullToken> tokens = baseTokenizer
				.fullTokenListWithPos(code);

		final JavaASTExtractor ex = new JavaASTExtractor(false);
		final ASTNode cu = ex.getASTNode(code);
		final JavaApproximateTypeInferencer tInf = new JavaApproximateTypeInferencer(
				cu);
		tInf.infer();
		final Map<Integer, String> types = tInf.getVariableTypesAtPosition();

		final SortedMap<Integer, FullToken> typeTokenList = Maps.newTreeMap();
		for (final Entry<Integer, FullToken> token : tokens.entrySet()) {
			final String type = types.get(token.getKey());
			if (type != null) {
				typeTokenList.put(token.getKey(), new FullToken("var%" + type
						+ "%", token.getValue().tokenType));
			} else {
				typeTokenList.put(token.getKey(),
						new FullToken(token.getValue().token,
								token.getValue().tokenType));
			}
		}
		return typeTokenList;
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

	@Override
	public String getIdentifierType() {
		return IDENTIFIER_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * codemining.languagetools.ITokenizer#getTokenFromString(java.lang.String)
	 */
	@Override
	public FullToken getTokenFromString(final String token) {
		if (token.startsWith("var%")) {
			return new FullToken(token, baseTokenizer.getIdentifierType());
		}
		// we can't get the type though, but that's not our problem...
		return baseTokenizer.getTokenFromString(token);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#getTokenListFromCode(char[])
	 */
	@Override
	public List<FullToken> getTokenListFromCode(final char[] code) {
		final SortedMap<Integer, FullToken> tokens = baseTokenizer
				.fullTokenListWithPos(code);

		final JavaASTExtractor ex = new JavaASTExtractor(false);
		final ASTNode cu = ex.getASTNode(code);
		final JavaApproximateTypeInferencer tInf = new JavaApproximateTypeInferencer(
				cu);
		tInf.infer();
		final Map<Integer, String> types = tInf.getVariableTypesAtPosition();

		final List<FullToken> typeTokenList = Lists.newArrayList();
		for (final Entry<Integer, FullToken> token : tokens.entrySet()) {
			final String type = types.get(token.getKey());
			if (type != null) {
				typeTokenList.add(new FullToken("var%" + type + "%", token
						.getValue().tokenType));
			} else {
				typeTokenList.add(new FullToken(token.getValue().token, token
						.getValue().tokenType));
			}
		}
		return typeTokenList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#tokenListFromCode(char[])
	 */
	@Override
	public List<String> tokenListFromCode(char[] code) {
		final List<FullToken> tokens = getTokenListFromCode(code);
		final List<String> stringTokens = Lists.newArrayList();
		for (final FullToken token : tokens) {
			stringTokens.add(token.token);
		}
		return stringTokens;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#tokenListWithPos(char[])
	 */
	@Override
	public SortedMap<Integer, String> tokenListWithPos(char[] code) {
		// TODO Auto-generated method stub
		throw new NotImplementedException();
	}

}
