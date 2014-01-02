/**
 * 
 */
package codemining.java.codeutils;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.core.util.PublicScanner;

import codemining.languagetools.ITokenizer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A token type tokenizer that tokenizes whitespace. The implementation is
 * thread safe.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class JavaWhitespaceTokenizer implements ITokenizer {

	/**
	 * A struct of an annotated token.
	 * 
	 */
	public static class AnnotatedToken {

		public final String token;

		public final int width;

		public AnnotatedToken(final String value, final int width) {
			token = value;
			this.width = width;
		}
	}

	/**
	 * The non-thread-safe implementation.
	 * 
	 */
	private static class TokenizerImplementation implements ITokenizer {

		private static final long serialVersionUID = 3466332155585174404L;

		private final RegexFileFilter javaCodeFilter = new RegexFileFilter(
				".*\\.java$");

		int currentIdentationSpaces = 0;

		int currentIdentationTabs = 0;

		/*
		 * (non-Javadoc)
		 * 
		 * @see codemining.languagetools.ITokenizer#fullTokenListWithPos(char[])
		 */
		@Override
		public SortedMap<Integer, FullToken> fullTokenListWithPos(char[] code) {
			final SortedMap<Integer, FullToken> tokenList = Maps.newTreeMap();
			for (final Entry<Integer, String> token : tokenListWithPos(code)
					.entrySet()) {
				tokenList.put(token.getKey(), new FullToken(token.getValue(),
						""));
			}
			return tokenList;
		}

		/**
		 * @param tokens
		 * @param scanner
		 * @param token
		 * @return
		 * @throws InvalidInputException
		 */
		private List<String> getConvertedToken(final PublicScanner scanner,
				final int token) throws InvalidInputException {
			final List<String> tokens = Lists.newArrayList();
			if (token == ITerminalSymbols.TokenNameEOF) {
				return ListUtils.EMPTY_LIST;
			}
			final String tokenString = scanner.getCurrentTokenString();

			if (token == ITerminalSymbols.TokenNameWHITESPACE) {
				tokens.addAll(toWhiteSpaceSymbol(tokenString));
			} else if (token == ITerminalSymbols.TokenNameIdentifier) {
				tokens.add(toIdentifierSymbol(tokenString));
			} else if (JavaTokenTypeTokenizer.isLiteralToken(token)) {
				tokens.add(toLiteralSymbol(tokenString));
			} else if (token == ITerminalSymbols.TokenNameCOMMENT_BLOCK) {
				tokens.add(JavaTokenTypeTokenizer.COMMENT_BLOCK);
			} else if (token == ITerminalSymbols.TokenNameCOMMENT_LINE) {
				tokens.add(JavaTokenTypeTokenizer.COMMENT_LINE);
				final int nextToken = scanner.getNextToken();
				if (nextToken == ITerminalSymbols.TokenNameWHITESPACE) {
					tokens.addAll(toWhiteSpaceSymbol("\n"
							+ scanner.getCurrentTokenString()));
				} else {
					tokens.addAll(toWhiteSpaceSymbol("\n"));
					tokens.addAll(getConvertedToken(scanner, nextToken));
				}
			} else if (token == ITerminalSymbols.TokenNameCOMMENT_JAVADOC) {
				tokens.add(JavaTokenTypeTokenizer.COMMENT_JAVADOC);
			} else {
				tokens.add(tokenString);
			}

			return tokens;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see codemining.languagetools.ITokenizer#getFileFilter()
		 */
		@Override
		public AbstractFileFilter getFileFilter() {
			return javaCodeFilter;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see codemining.languagetools.ITokenizer#getIdentifierType()
		 */
		@Override
		public String getIdentifierType() {
			// We do not return types here...
			return "";
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * codemining.languagetools.ITokenizer#getTokenFromString(java.lang.
		 * String)
		 */
		@Override
		public FullToken getTokenFromString(final String token) {
			return new FullToken(token, "");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see codemining.languagetools.ITokenizer#getTokenListFromCode(char[])
		 */
		@Override
		public List<FullToken> getTokenListFromCode(final char[] code) {
			final List<FullToken> tokens = Lists.newArrayList();
			tokens.add(new FullToken(SENTENCE_START, SENTENCE_START));
			final PublicScanner scanner = prepareScanner(code);
			do {
				try {
					final int token = scanner.getNextToken();
					if (token == ITerminalSymbols.TokenNameEOF) {
						break;
					}
					for (final String cToken : getConvertedToken(scanner, token)) {
						tokens.add(new FullToken(cToken, ""));
					}
				} catch (InvalidInputException e) {
					LOGGER.warning(ExceptionUtils.getFullStackTrace(e));
				}
			} while (!scanner.atEnd());
			tokens.add(new FullToken(SENTENCE_END, SENTENCE_END));
			return tokens;
		}

		public List<AnnotatedToken> getTokensWithWidthData(char[] code) {
			final List<AnnotatedToken> tokens = Lists.newArrayList();
			tokens.add(new AnnotatedToken(SENTENCE_START, 0));
			final PublicScanner scanner = prepareScanner(code);
			do {
				try {
					final int token = scanner.getNextToken();
					if (token == ITerminalSymbols.TokenNameEOF) {
						break;
					}
					for (final String cToken : getConvertedToken(scanner, token)) {
						int currentPosition = scanner
								.getCurrentTokenStartPosition();
						int currentLine = scanner
								.getLineNumber(currentPosition);
						int lineStart = scanner.getLineStart(currentLine);
						tokens.add(new AnnotatedToken(cToken, currentPosition
								- lineStart));
					}
				} catch (InvalidInputException e) {
					LOGGER.warning(ExceptionUtils.getFullStackTrace(e));
				}
			} while (!scanner.atEnd());
			tokens.add(new AnnotatedToken(SENTENCE_END, 0));
			return tokens;
		}

		/**
		 * @param code
		 * @return
		 */
		private PublicScanner prepareScanner(final char[] code) {
			final PublicScanner scanner = new PublicScanner();
			scanner.setSource(code);
			scanner.tokenizeWhiteSpace = true;
			scanner.recordLineSeparator = true;
			scanner.tokenizeComments = true;
			currentIdentationSpaces = 0;
			currentIdentationTabs = 0;
			return scanner;
		}

		public String toIdentifierSymbol(final String token) {
			return JavaTokenTypeTokenizer.IDENTIFIER_TOKEN;
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
			final PublicScanner scanner = prepareScanner(code);
			do {
				try {
					final int token = scanner.getNextToken();
					if (token == ITerminalSymbols.TokenNameEOF) {
						break;
					}
					tokens.addAll(getConvertedToken(scanner, token));
				} catch (InvalidInputException e) {
					LOGGER.warning(ExceptionUtils.getFullStackTrace(e));
				}
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
		public SortedMap<Integer, String> tokenListWithPos(final char[] code) {
			final SortedMap<Integer, String> tokens = Maps.newTreeMap();
			tokens.put(-1, SENTENCE_START);
			tokens.put(Integer.MAX_VALUE, SENTENCE_END);
			final PublicScanner scanner = prepareScanner(code);

			while (!scanner.atEnd()) {
				do {
					try {
						final int token = scanner.getNextToken();
						final int position = scanner
								.getCurrentTokenStartPosition();
						if (token == ITerminalSymbols.TokenNameEOF) {
							break;
						}
						int i = 0;
						final List<String> cTokens = getConvertedToken(scanner,
								token);
						for (final String cToken : cTokens) {
							tokens.put(position + i, cToken);
							i++;
						}
					} catch (InvalidInputException e) {
						LOGGER.warning(ExceptionUtils.getFullStackTrace(e));
					}
				} while (!scanner.atEnd());
			}
			return tokens;
		}

		public String toLiteralSymbol(final String token) {
			return JavaTokenTypeTokenizer.LITERAL_TOKEN;
		}

		/**
		 * @param scanner
		 * @return
		 */
		public List<String> toWhiteSpaceSymbol(final String token) {
			final List<String> symbols = Lists.newArrayList();
			int spaces = 0;
			int tabs = 0;
			int newLines = 0;
			for (final char c : token.replace("\r", "").toCharArray()) {
				if (c == '\n') {
					newLines++;
				} else if (c == '\t') {
					tabs++;
				} else if (c == ' ') {
					spaces++;
				}
			}

			if (newLines == 0) {
				symbols.add("WS_s" + spaces + "t" + tabs);
			} else if (newLines > 0) {
				final int spaceDiff = spaces - currentIdentationSpaces;
				final int tabDiff = tabs - currentIdentationTabs;
				currentIdentationSpaces = spaces;
				currentIdentationTabs = tabs;

				if (spaceDiff >= 0 && tabDiff >= 0) {
					symbols.add("WS_INDENTs" + spaceDiff + "t" + tabDiff + "n"
							+ newLines);
				} else {
					symbols.add("WS_DEDENTs" + -spaceDiff + "t" + -tabDiff
							+ "n" + newLines);
				}
			}

			return symbols;
		}
	}

	private static final long serialVersionUID = -3956186603216801513L;

	private static final Logger LOGGER = Logger
			.getLogger(JavaWhitespaceTokenizer.class.getName());

	/**
	 * A utility stateful class for converting whitespace tokens to whitespace.
	 * 
	 */
	public static final class WhitespaceConverter {

		/**
		 * A struct class.
		 * 
		 */
		private static final class Whitespace {
			int nTabs;
			int nSpace;
			int nNewLines;
		}

		private int currentSpaceIndentation = 0;
		private int currentTabIndentation = 0;

		public static final Pattern INDENT_PATTERN = Pattern
				.compile("WS_INDENTs([0-9]+)t([0-9]+)n([0-9]+)");

		public static final Pattern DEDENT_PATTERN = Pattern
				.compile("WS_DEDENTs(\\d+)t(\\d+)n(\\d+)");

		public static final Pattern SPACE_PATTERN = Pattern
				.compile("WS_s(\\d+)t(\\d+)");

		private Whitespace convert(final String wsToken,
				final Pattern patternToMatch) {
			final Whitespace space = new Whitespace();
			final Matcher m = patternToMatch.matcher(wsToken);
			checkArgument(m.matches(), "Pattern " + patternToMatch.toString()
					+ " does not match " + wsToken);
			space.nSpace = Integer.parseInt(m.group(1));
			space.nTabs = Integer.parseInt(m.group(2));
			if (m.groupCount() == 3) {
				space.nNewLines = Integer.parseInt(m.group(3));
			}
			return space;
		}

		/**
		 * Whitespace token converter.
		 * 
		 * @param wsToken
		 * @param buffer
		 */
		public void appendWS(final String wsToken, final StringBuffer buffer) {
			checkArgument(wsToken.startsWith("WS_"));
			final Whitespace space;
			if (wsToken.startsWith("WS_INDENT")) {
				space = convert(wsToken, INDENT_PATTERN);
				currentSpaceIndentation += space.nSpace;
				currentTabIndentation += space.nTabs;
				space.nSpace = currentSpaceIndentation;
				space.nTabs = currentTabIndentation;

			} else if (wsToken.startsWith("WS_DEDENT")) {
				space = convert(wsToken, DEDENT_PATTERN);
				currentSpaceIndentation -= space.nSpace;
				if (currentSpaceIndentation < 0) {
					currentSpaceIndentation = 0;
				}
				currentTabIndentation -= space.nTabs;
				if (currentTabIndentation < 0) {
					currentTabIndentation = 0;
				}
				space.nSpace = currentSpaceIndentation;
				space.nTabs = currentTabIndentation;
			} else {
				space = convert(wsToken, SPACE_PATTERN);
			}
			createWhitespace(space, buffer);
		}

		/**
		 * Append whitespace to StringBuffer, given the specifications.
		 * 
		 * @param nSpace
		 * @param nTab
		 * @param startAtNewLine
		 * @return
		 */
		public static final void createWhitespace(final Whitespace space,
				final StringBuffer sb) {
			for (int i = 0; i < space.nNewLines; i++) {
				sb.append(System.getProperty("line.separator"));
			}
			for (int i = 0; i < space.nSpace; i++) {
				sb.append(" ");
			}
			for (int i = 0; i < space.nTabs; i++) {
				sb.append("\t");
			}
		}
	}

	@Override
	public SortedMap<Integer, FullToken> fullTokenListWithPos(char[] code) {
		final TokenizerImplementation tok = new TokenizerImplementation();
		return tok.fullTokenListWithPos(code);
	}

	@Override
	public AbstractFileFilter getFileFilter() {
		final TokenizerImplementation tok = new TokenizerImplementation();
		return tok.getFileFilter();
	}

	@Override
	public String getIdentifierType() {
		final TokenizerImplementation tok = new TokenizerImplementation();
		return tok.getIdentifierType();
	}

	@Override
	public FullToken getTokenFromString(String token) {
		final TokenizerImplementation tok = new TokenizerImplementation();
		return tok.getTokenFromString(token);
	}

	@Override
	public List<FullToken> getTokenListFromCode(char[] code) {
		final TokenizerImplementation tok = new TokenizerImplementation();
		return tok.getTokenListFromCode(code);
	}

	public List<AnnotatedToken> getTokensWithWidthData(final char[] code) {
		final TokenizerImplementation tok = new TokenizerImplementation();
		return tok.getTokensWithWidthData(code);
	}

	@Override
	public List<String> tokenListFromCode(char[] code) {
		final TokenizerImplementation tok = new TokenizerImplementation();
		return tok.tokenListFromCode(code);
	}

	@Override
	public SortedMap<Integer, String> tokenListWithPos(char[] code) {
		final TokenizerImplementation tok = new TokenizerImplementation();
		return tok.tokenListWithPos(code);
	}
}
