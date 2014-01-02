/**
 * 
 */
package codemining.cpp.codeutils;

import java.util.List;
import java.util.SortedMap;

import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.eclipse.cdt.internal.formatter.scanner.Scanner;
import org.eclipse.cdt.internal.formatter.scanner.Token;

import codemining.languagetools.ITokenizer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A C/C++ tokenizer provided by the Eclipse CDT.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class CDTTokenizer implements ITokenizer {

	private static final long serialVersionUID = 3954406410244227404L;
	/**
	 * A filter for the files being tokenized.
	 */
	private static final RegexFileFilter cppCodeFilter = new RegexFileFilter(
			".*\\.(cc|cpp|h)$");

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#fullTokenListWithPos(char[])
	 */
	@Override
	public SortedMap<Integer, FullToken> fullTokenListWithPos(char[] code) {
		final SortedMap<Integer, FullToken> tokens = Maps.newTreeMap();
		tokens.put(-1, new FullToken(SENTENCE_START, SENTENCE_START));
		tokens.put(Integer.MAX_VALUE, new FullToken(SENTENCE_END, SENTENCE_END));

		final Scanner scanner = new Scanner();
		scanner.setSource(code);
		do {
			final int token = scanner.getNextToken();
			if (token == Token.tWHITESPACE) {
				continue;
			}
			final String nxtToken = new String(scanner.getCurrentTokenSource());
			tokens.put(scanner.getCurrentPosition(), new FullToken(nxtToken,
					Integer.toString(token)));
		} while (!scanner.atEnd());
		return tokens;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#getFileFilter()
	 */
	@Override
	public AbstractFileFilter getFileFilter() {
		return cppCodeFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#getIdentifierType()
	 */
	@Override
	public String getIdentifierType() {
		return Integer.toString(Token.tIDENTIFIER);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * codemining.languagetools.ITokenizer#getTokenFromString(java.lang.String)
	 */
	@Override
	public FullToken getTokenFromString(String token) {
		if (token.equals(ITokenizer.SENTENCE_START)) {
			return new FullToken(ITokenizer.SENTENCE_START,
					ITokenizer.SENTENCE_START);
		}

		if (token.equals(ITokenizer.SENTENCE_END)) {
			return new FullToken(ITokenizer.SENTENCE_END,
					ITokenizer.SENTENCE_END);
		}
		return getTokenListFromCode(token.toCharArray()).get(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#getTokenListFromCode(char[])
	 */
	@Override
	public List<FullToken> getTokenListFromCode(char[] code) {
		final List<FullToken> tokens = Lists.newArrayList();
		tokens.add(new FullToken(SENTENCE_START, SENTENCE_START));

		final Scanner scanner = new Scanner();
		scanner.setSource(code);

		do {
			final int token = scanner.getNextToken();
			if (token == Token.tWHITESPACE) {
				continue;
			}
			final String nxtToken = new String(scanner.getCurrentTokenSource());
			tokens.add(new FullToken(nxtToken, Integer.toString(token)));
		} while (!scanner.atEnd());

		tokens.add(new FullToken(SENTENCE_END, SENTENCE_END));
		return tokens;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#tokenListFromCode(char[])
	 */
	@Override
	public List<String> tokenListFromCode(char[] code) {
		final List<String> tokens = Lists.newArrayList();
		tokens.add(SENTENCE_START);

		final Scanner scanner = new Scanner();
		scanner.setSource(code);

		do {
			final int token = scanner.getNextToken();
			if (token == Token.tWHITESPACE) {
				continue;
			}
			final String nxtToken = new String(scanner.getCurrentTokenSource());
			tokens.add(nxtToken);
		} while (!scanner.atEnd());

		tokens.add(SENTENCE_END);
		return tokens;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.languagetools.ITokenizer#tokenListWithPos(char[])
	 */
	@Override
	public SortedMap<Integer, String> tokenListWithPos(char[] code) {
		final SortedMap<Integer, String> tokens = Maps.newTreeMap();
		tokens.put(-1, SENTENCE_START);
		tokens.put(Integer.MAX_VALUE, SENTENCE_END);

		final Scanner scanner = new Scanner();
		scanner.setSource(code);
		do {
			final int token = scanner.getNextToken();
			if (token == Token.tWHITESPACE) {
				continue;
			}
			final String nxtToken = new String(scanner.getCurrentTokenSource());
			tokens.put(scanner.getCurrentPosition(), nxtToken);
		} while (!scanner.atEnd());
		return tokens;
	}

}
