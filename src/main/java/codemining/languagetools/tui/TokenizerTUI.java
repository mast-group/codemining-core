/**
 * 
 */
package codemining.languagetools.tui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import codemining.languagetools.ITokenizer;
import codemining.languagetools.ITokenizer.FullToken;

import com.google.common.collect.Lists;

/**
 * Print tokenized code of a file to stdout. Each token is placed at a separate
 * line. New files are separated by an empty line.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class TokenizerTUI {
	public static void main(final String[] args) throws InvalidInputException,
			IOException, InstantiationException, IllegalAccessException,
			ClassNotFoundException, IllegalArgumentException,
			SecurityException, InvocationTargetException, NoSuchMethodException {
		if (args.length < 2) {
			System.err
					.println("Usage <codeDir> <TokenizerClass> [TokenizerArgs]");
			return;
		}

		final ITokenizer tok;
		if (args.length == 2) {
			tok = (ITokenizer) Class.forName(args[1]).newInstance();
		} else {
			tok = (ITokenizer) Class.forName(args[1])
					.getDeclaredConstructor(String.class).newInstance(args[2]);
		}

		final File baseFile = new File(args[0]);
		final Collection<File> allFiles;
		if (baseFile.isDirectory()) {
			allFiles = FileUtils.listFiles(baseFile, tok.getFileFilter(),
					DirectoryFileFilter.DIRECTORY);
		} else {
			allFiles = Lists.newArrayList(baseFile);
		}

		for (final File fi : allFiles) {

			final StringBuffer buf = new StringBuffer();
			final char[] code = FileUtils.readFileToString(fi).toCharArray();
			for (final FullToken token : tok.getTokenListFromCode(code)) {
				buf.append(token);
				buf.append(System.getProperty("line.separator"));
			}

			System.out.println(buf.toString());
			System.out.println();

		}
	}
}
