/**
 * 
 */
package codemining.java.codeutils;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import codemining.languagetools.ITokenizer;
import codemining.languagetools.ParseKind;

/**
 * A utility class to retrieve an Eclipse AST.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class JavaASTExtractor {

	/**
	 * Remembers if the given Extractor will calculate the bindings.
	 */
	private final boolean useBindings;

	private final boolean useJavadocs;

	/**
	 * Constructor.
	 * 
	 * @param useBindings
	 *            calculate bindings on the extracted AST.
	 */
	public JavaASTExtractor(final boolean useBindings) {
		this.useBindings = useBindings;
		useJavadocs = false;
	}

	public JavaASTExtractor(final boolean useBindings,
			final boolean useJavadocs) {
		this.useBindings = useBindings;
		this.useJavadocs = useJavadocs;
	}

	/**
	 * Get the AST of a file. It is assumed that a CompilationUnit will be
	 * returned. An heuristic is used to set the path variables.
	 * 
	 * @param file
	 * @return the compilation unit of the file
	 * @throws IOException
	 */
	public final CompilationUnit getAST(final File file) throws IOException {
		final String sourceFile = FileUtils.readFileToString(file);
		final ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final Map<String, String> options = new Hashtable<String, String>();
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				JavaCore.VERSION_1_7);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
		if (useJavadocs) {
			options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		}
		parser.setCompilerOptions(options);
		parser.setSource(sourceFile.toCharArray()); // set source
		parser.setResolveBindings(useBindings);
		parser.setBindingsRecovery(useBindings);

		parser.setStatementsRecovery(true);

		parser.setUnitName(file.getAbsolutePath());

		// Heuristic to retrieve source file path
		final String srcFilePath;
		if (file.getAbsolutePath().contains("/src")) {
			srcFilePath = file.getAbsolutePath().substring(0,
					file.getAbsolutePath().indexOf("src", 0) + 3);
		} else {
			srcFilePath = "";
		}

		final String[] sourcePathEntries = new String[] { srcFilePath };
		final String[] classPathEntries = new String[0];
		parser.setEnvironment(classPathEntries, sourcePathEntries, null, false);

		final CompilationUnit compilationUnit = (CompilationUnit) parser
				.createAST(null);
		return compilationUnit;
	}

	/**
	 * Get a compilation unit of the given file content.
	 * 
	 * @param fileContent
	 * @return the compilation unit
	 * @deprecated You need to use the getASTNode() specifying the type of input
	 */
	public final CompilationUnit getAST(final String fileContent) {
		return (CompilationUnit) getASTNode(fileContent);
	}

	/**
	 * Get a compilation unit of the given file content.
	 * 
	 * @param fileContent
	 * @param kind
	 * @return the compilation unit
	 */
	public final ASTNode getAST(final String fileContent, final ParseKind kind) {
		return (ASTNode) getASTNode(fileContent, kind);
	}

	/**
	 * 
	 * @deprecated Use getASTNode specifying the parse kind.
	 */
	public final ASTNode getASTNode(final char[] content) {
		for (final ParseKind kind : ParseKind.values()) {
			final ASTNode node = getASTNode(content, kind);
			if (normalizeCode(node.toString().toCharArray()).equals(
					normalizeCode(content))) {
				return node;
			}
		}
		throw new IllegalArgumentException(
				"Code snippet could not be recognized as any of the known types");
	}

	/**
	 * Hacky way to compare snippets.
	 * 
	 * @param snippet
	 * @return
	 */
	private String normalizeCode(final char[] snippet) {
		final List<String> tokens = (new JavaTokenizer())
				.tokenListFromCode(snippet);

		final StringBuffer bf = new StringBuffer();
		for (final String token : tokens) {
			if (token.equals(ITokenizer.SENTENCE_START)
					|| token.equals(ITokenizer.SENTENCE_END)) {
				continue;
			} else {
				bf.append(token);
			}
			bf.append(" ");
		}
		return bf.toString();

	}

	/**
	 * Return an ASTNode given the content
	 * 
	 * @param content
	 * @return
	 */
	public final ASTNode getASTNode(final char[] content, final ParseKind kind) {
		final ASTParser parser = ASTParser.newParser(AST.JLS4);
		final int astKind;
		switch (kind) {
		case CLASS_BODY:
		case METHOD:
			astKind = ASTParser.K_CLASS_BODY_DECLARATIONS;
			break;
		case COMPILATION_UNIT:
			astKind = ASTParser.K_COMPILATION_UNIT;
			break;
		case EXPRESSION:
			astKind = ASTParser.K_EXPRESSION;
			break;
		case STATEMENTS:
			astKind = ASTParser.K_STATEMENTS;
			break;
		default:
			astKind = ASTParser.K_COMPILATION_UNIT;
		}
		parser.setKind(astKind);

		final Map<String, String> options = new Hashtable<String, String>();
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				JavaCore.VERSION_1_7);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
		if (useJavadocs) {
			options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		}
		parser.setCompilerOptions(options);
		parser.setSource(content); // set source
		parser.setResolveBindings(useBindings);
		parser.setBindingsRecovery(useBindings);

		parser.setStatementsRecovery(true);

		if (kind != ParseKind.METHOD) {
			return parser.createAST(null);
		} else {
			final ASTNode cu = parser.createAST(null);
			return getFirstMethodDeclaration(cu);
		}
	}

	private static final class TopMethodRetriever extends ASTVisitor {
		public MethodDeclaration topDcl;

		@Override
		public boolean visit(final MethodDeclaration node) {
			topDcl = node;
			return false;
		}
	}

	private final MethodDeclaration getFirstMethodDeclaration(final ASTNode node) {
		final TopMethodRetriever visitor = new TopMethodRetriever();
		node.accept(visitor);
		return visitor.topDcl;
	}

	/**
	 * Get the AST of a string. Path variables cannot be set.
	 * 
	 * @param file
	 * @return an AST node for the given file content
	 * @throws IOException
	 * @deprecated Use getASTNode specifying the parse kind.
	 */
	public final ASTNode getASTNode(final String fileContent) {
		return getASTNode(fileContent.toCharArray());
	}

	/**
	 * Get the AST of a string. Path variables cannot be set.
	 * 
	 * @param file
	 * @param kind
	 * @return an AST node for the given file content
	 * @throws IOException
	 */
	public final ASTNode getASTNode(final String fileContent,
			final ParseKind kind) {
		return getASTNode(fileContent.toCharArray(), kind);
	}

}
