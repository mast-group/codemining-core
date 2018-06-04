/**
 *
 */
package codemining.java.codeutils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.Lists;

/**
 * Extract all methods in a class
 *
 * @author Miltos Allamanis<m.allamanis@ed.ac.uk>
 *
 */
public class MethodExtractor {

	private static class MethodVisitor extends ASTVisitor {

		final List<MethodDeclaration> allMethods = Lists.newArrayList();

		Stack<String> className = new Stack<String>();

		private String currentPackageName;

		private final ProjectTypeInformation pti;

		public MethodVisitor(final ProjectTypeInformation pti) {
			this.pti = pti;
		}

		@Override
		public void endVisit(final TypeDeclaration node) {
			className.pop();
			super.endVisit(node);
		}

		/**
		 * @param node
		 * @return
		 */
		public boolean isOverride(final MethodDeclaration node) {
			try {
				final boolean hasAnnotation = MethodUtils.hasOverrideAnnotation(node);
				if (pti == null || hasAnnotation) {
					return hasAnnotation;
				}

				final boolean isOverride = pti.isMethodOverride(className.peek(), node);
				return hasAnnotation || isOverride;
			} catch (final Throwable e) {
				System.err.println(e + ":" + node.toString());
				return false;
			}
		}

		@Override
		public boolean visit(final CompilationUnit node) {
			if (node.getPackage() != null) {
				currentPackageName = node.getPackage().getName().getFullyQualifiedName();
			} else {
				currentPackageName = "";
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(final ImportDeclaration node) {
			// Don't visit. It's boring
			return false;
		}

		@Override
		public boolean visit(final MethodDeclaration node) {
			if (node.isConstructor()) {
				return super.visit(node);
			} else if (isOverride(node)) {
				return super.visit(node);
			}
			allMethods.add(node);
			return super.visit(node);
		}

		@Override
		public boolean visit(final TypeDeclaration node) {
			if (className.isEmpty()) {
				className.push(currentPackageName + "." + node.getName().getIdentifier());
			} else {
				className.push(className.peek() + "." + node.getName().getIdentifier());
			}
			return super.visit(node);
		}

	}

	public static List<MethodDeclaration> getMethods(final File file) throws IOException {
		return getMethods(file, null);
	}

	public static List<MethodDeclaration> getMethods(final File file, final ProjectTypeInformation pti)
			throws IOException {
		try {
			final JavaASTExtractor ex = new JavaASTExtractor(false);
			final MethodVisitor mv = new MethodVisitor(pti);
			final CompilationUnit cu = ex.getAST(file);
			cu.accept(mv);
			return mv.allMethods;
		} catch (Exception e) {
			System.err.println(ExceptionUtils.getFullStackTrace(e));
		}
		return new ArrayList<>();
	}

	private MethodExtractor() {

	}

}
