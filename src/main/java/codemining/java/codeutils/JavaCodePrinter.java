package codemining.java.codeutils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;

import codemining.languagetools.ColoredToken;
import codemining.languagetools.ITokenizer;
import codemining.languagetools.ITokenizer.FullToken;

/**
 * Output java code to HTML with optional coloring.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class JavaCodePrinter {

	/**
	 * The tokenizer used to tokenize code.
	 */
	final ITokenizer jTokenizer;

	/**
	 * The background Color of the output HTML document.
	 */
	final Color documentBackgroundColor;

	public JavaCodePrinter() {
		jTokenizer = new JavaTokenizer();
		documentBackgroundColor = Color.WHITE;
	}

	public JavaCodePrinter(final Color documentBackgroundColor) {
		jTokenizer = new JavaTokenizer();
		this.documentBackgroundColor = documentBackgroundColor;
	}

	public JavaCodePrinter(final ITokenizer tokenizer,
			final Color documentBackgroundColor) {
		this.jTokenizer = tokenizer;
		this.documentBackgroundColor = documentBackgroundColor;
	}

	private void addSlack(final String substring, final StringBuffer buf) {
		for (final char c : substring.toCharArray()) {
			if (c == ' ') {
				buf.append("&nbsp;");
			} else if (c == '\n') {
				buf.append("<br/>\n");
			} else if (c == '\t') {
				buf.append("&nbsp;&nbsp;&nbsp;");
			} else {
				buf.append(c);
			}
		}
	}

	/**
	 * Return a StringBuffer with colored tokens as specified from the
	 * coloredTokens. There should be one-to-one correspondence with the actual
	 * tokens.
	 */
	public StringBuffer writeHTMLwithColors(
			final List<ColoredToken> coloredTokens, final File codeFile)
			throws IOException, InstantiationException, IllegalAccessException {
		final String code = FileUtils.readFileToString(codeFile);

		final StringBuffer buf = new StringBuffer();

		final SortedMap<Integer, FullToken> toks = jTokenizer
				.fullTokenListWithPos(code.toCharArray());

		int i = 0;
		int prevPos = 0;
		buf.append("<html><body style='font-family:monospace; "
				+ "background-color:rgb(" + documentBackgroundColor.getRed()
				+ "," + documentBackgroundColor.getGreen() + ","
				+ documentBackgroundColor.getBlue() + ")'>");
		for (final Entry<Integer, FullToken> entry : toks.entrySet()) {
			if (i == 0 || entry.getKey() == Integer.MAX_VALUE) {
				i++;
				continue;
			}
			addSlack(code.substring(prevPos, entry.getKey()), buf);
			final ColoredToken tok = coloredTokens.get(i);

			buf.append("<span style='background-color:rgb("
					+ tok.bgColor.getRed() + "," + tok.bgColor.getGreen() + ","
					+ tok.bgColor.getBlue() + "); color:rgb("
					+ tok.fontColor.getRed() + "," + tok.fontColor.getGreen()
					+ "," + tok.fontColor.getBlue() + "); " + tok.extraStyle
					+ "'>" + entry.getValue().token + "</span>");
			i++;
			prevPos = entry.getKey() + entry.getValue().token.length();
		}
		buf.append("</body></html>");
		return buf;
	}
}