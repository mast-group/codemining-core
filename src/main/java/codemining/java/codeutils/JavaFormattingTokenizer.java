package codemining.java.codeutils;

import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.commons.io.filefilter.AbstractFileFilter;

import codemining.languagetools.ITokenizer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Adds a NO_SPACE between tokens that contain, no space.
 * 
 */
public class JavaFormattingTokenizer implements ITokenizer {

	public static final String WS_NO_SPACE = "WS_NO_SPACE";
	private static final long serialVersionUID = -1736507313790110846L;
	final ITokenizer baseTokenizer;

	public JavaFormattingTokenizer() {
		baseTokenizer = new JavaWhitespaceTokenizer();
	}

	public JavaFormattingTokenizer(final ITokenizer baseTokenizer) {
		this.baseTokenizer = baseTokenizer;
	}

	@Override
	public SortedMap<Integer, FullToken> fullTokenListWithPos(final char[] code) {
		throw new IllegalArgumentException("Cannot be implemented");
	}

	@Override
	public AbstractFileFilter getFileFilter() {
		return baseTokenizer.getFileFilter();
	}

	@Override
	public String getIdentifierType() {
		return baseTokenizer.getIdentifierType();
	}

	@Override
	public FullToken getTokenFromString(final String token) {
		return baseTokenizer.getTokenFromString(token);
	}

	@Override
	public List<FullToken> getTokenListFromCode(final char[] code) {
		final List<FullToken> list = Lists.newArrayList();
		final List<FullToken> original = baseTokenizer
				.getTokenListFromCode(code);
		for (int i = 0; i < original.size() - 1; i++) {
			final FullToken currentToken = original.get(i);
			list.add(currentToken);
			final FullToken nextToken = original.get(i + 1);
			if (!currentToken.token.startsWith("WS_")
					&& !nextToken.token.startsWith("WS_")) {
				list.add(new FullToken(WS_NO_SPACE, ""));
			}
		}
		list.add(original.get(original.size() - 1));
		return list;
	}

	@Override
	public List<String> tokenListFromCode(final char[] code) {
		// TODO Duplicate
		final List<String> list = Lists.newArrayList();
		final List<String> original = baseTokenizer.tokenListFromCode(code);
		for (int i = 0; i < original.size() - 1; i++) {
			final String currentToken = original.get(i);
			list.add(currentToken);
			final String nextToken = original.get(i + 1);
			if (!currentToken.startsWith("WS_") && !nextToken.startsWith("WS_")) {
				list.add(WS_NO_SPACE);
			}
		}
		list.add(original.get(original.size() - 1));
		return list;
	}

	@Override
	public SortedMap<Integer, String> tokenListWithPos(final char[] code) {
		throw new IllegalArgumentException("Cannot be implemented");
	}

	/**
	 * Return the position of just the whitespaces in the code.
	 * 
	 * @param code
	 * @return
	 */
	public SortedMap<Integer, String> whitespaceTokenPositions(final char[] code) {
		final SortedMap<Integer, String> wsPositions = Maps.newTreeMap();
		final SortedMap<Integer, String> originalPositions = baseTokenizer
				.tokenListWithPos(code);

		boolean previousWasWhitespace = true;
		for (final Entry<Integer, String> tokenEntry : originalPositions
				.entrySet()) {
			if (tokenEntry.getValue().startsWith(ITokenizer.SENTENCE_START)
					|| tokenEntry.getValue()
							.startsWith(ITokenizer.SENTENCE_END)) {
				continue;
			}
			if (tokenEntry.getValue().startsWith("WS_")) {
				wsPositions.put(tokenEntry.getKey(), tokenEntry.getValue());
				previousWasWhitespace = true;
			} else if (!previousWasWhitespace) {
				wsPositions.put(tokenEntry.getKey(), WS_NO_SPACE);
				previousWasWhitespace = false;
			} else {
				previousWasWhitespace = false;
			}
		}

		return wsPositions;
	}
}