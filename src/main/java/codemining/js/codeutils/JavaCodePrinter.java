package codemining.js.codeutils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.apache.commons.io.FileUtils;

import codemining.java.codeutils.JavaTokenizer;
import codemining.languagetools.ITokenizer;
import codemining.languagetools.ITokenizer.FullToken;

public class JavaCodePrinter {

	/**
	 * Struct class to get a code colored token.
	 * 
	 */
	public final static class ColoredToken {
		public final Color fontColor;
		public final Color bgColor;
		public final String token;
		public final String extraStyle;

		/**
		 * Construct with default bgColor white.
		 * 
		 * @param token
		 * @param fontColor
		 */
		public ColoredToken(final String token, final Color fontColor) {
			this.token = token;
			this.fontColor = fontColor;
			bgColor = Color.WHITE;
			extraStyle = "";
		}

		public ColoredToken(final String token, final Color fontColor,
				final Color bgColor, final String extraStyle) {
			this.token = token;
			this.fontColor = fontColor;
			this.bgColor = bgColor;
			this.extraStyle = extraStyle;
		}
	}

	private static void addSlack(final String substring, final StringBuffer buf) {
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
	 * @param lm
	 * @param codeFile
	 * @throws IOException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public static StringBuffer writeHTMLwithColors(
			final List<ColoredToken> coloredTokens, final File codeFile)
			throws IOException, InstantiationException, IllegalAccessException {
		final String code = FileUtils.readFileToString(codeFile);

		final StringBuffer buf = new StringBuffer();

		final ITokenizer jTokenizer = new JavaTokenizer(); // TODO: get from
															// main
		final SortedMap<Integer, FullToken> toks = jTokenizer
				.fullTokenListWithPos(code.toCharArray());

		int i = 0;
		int prevPos = 0;
		buf.append("<html><body style='font-family:monospace; background-color:rgb(200,200,255)'>");
		for (final Entry<Integer, FullToken> entry : toks.entrySet()) {
			if (i == 0 || entry.getKey() == Integer.MAX_VALUE) {
				i++;
				continue;
			}
			addSlack(code.substring(prevPos, entry.getKey()), buf);
			final ColoredToken tok = coloredTokens.get(i);

			buf.append("<span style='background-color:rgb("
					+ tok.bgColor.getRed() + "," + tok.bgColor.getGreen() + ","
					+ tok.bgColor.getBlue() + "); color:rbg("
					+ tok.fontColor.getRed() + "," + tok.fontColor.getGreen()
					+ "," + tok.fontColor.getBlue() + "); " + tok.extraStyle
					+ "'>" + entry.getValue().token + "</span>");
			i++;
			prevPos = entry.getKey() + entry.getValue().token.length();
		}
		buf.append("</body></html>");
		return buf;
	}

	public JavaCodePrinter() {
		super();
	}

}