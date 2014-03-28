/**
 * 
 */
package codemining.java.codeutils.binding;

import java.util.List;
import java.util.SortedMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import codemining.java.codeutils.JavaTokenizer;
import codemining.languagetools.NameBinding;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Extract Java type name bindings.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class JavaTypeBindingExtractor extends AbstractJavaNameBindingsExtractor {

	private static class ClassnameFinder extends ASTVisitor {

		Multimap<String, Integer> classNamePostions = HashMultimap.create();

		final SortedMap<Integer, Integer> positionToIndex;

		public ClassnameFinder(final SortedMap<Integer, Integer> positionToIndex) {
			this.positionToIndex = positionToIndex;
		}

		@Override
		public boolean visit(final CastExpression node) {
			final String type = node.getType().toString();
			classNamePostions.put(type,
					positionToIndex.get(node.getType().getStartPosition()));
			return super.visit(node);
		}

		@Override
		public boolean visit(final ClassInstanceCreation node) {
			final String type = node.getType().toString();
			classNamePostions.put(type,
					positionToIndex.get(node.getType().getStartPosition()));
			return super.visit(node);
		}

		@Override
		public boolean visit(final EnumDeclaration node) {
			classNamePostions.put(node.getName().getIdentifier(),
					positionToIndex.get(node.getName().getStartPosition()));
			return super.visit(node);
		}

		@Override
		public boolean visit(final FieldDeclaration node) {
			final String type = node.getType().toString();
			classNamePostions.put(type,
					positionToIndex.get(node.getType().getStartPosition()));
			return super.visit(node);
		}

		@Override
		public boolean visit(final SingleVariableDeclaration node) {
			final String type = node.getType().toString();
			classNamePostions.put(type,
					positionToIndex.get(node.getType().getStartPosition()));
			return false;
		}

		@Override
		public boolean visit(final TypeDeclaration node) {
			classNamePostions.put(node.getName().getIdentifier(),
					positionToIndex.get(node.getName().getStartPosition()));
			return super.visit(node);
		}

		@Override
		public boolean visit(final TypeLiteral node) {
			final String type = node.getType().toString();
			classNamePostions.put(type,
					positionToIndex.get(node.getType().getStartPosition()));
			return super.visit(node);
		}

		@Override
		public boolean visit(final VariableDeclarationExpression node) {
			final String type = node.getType().toString();
			classNamePostions.put(type,
					positionToIndex.get(node.getType().getStartPosition()));
			return super.visit(node);
		}

		@Override
		public boolean visit(final VariableDeclarationStatement node) {
			final String type = node.getType().toString();
			classNamePostions.put(type,
					positionToIndex.get(node.getType().getStartPosition()));
			return super.visit(node);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.java.codeutils.binding.AbstractJavaNameBindingsExtractor#
	 * getNameBindings(org.eclipse.jdt.core.dom.ASTNode, java.lang.String)
	 */
	@Override
	public List<NameBinding> getNameBindings(final ASTNode node,
			final String sourceCode) {
		final JavaTokenizer tokenizer = new JavaTokenizer();
		final SortedMap<Integer, String> tokenPositions = tokenizer
				.tokenListWithPos(sourceCode.toCharArray());
		final SortedMap<Integer, Integer> positionToIndex = getTokenIndexForPostion(tokenPositions);
		final List<String> codeTokens = Lists.newArrayList(tokenPositions
				.values());

		final ClassnameFinder finder = new ClassnameFinder(positionToIndex);
		node.accept(finder);

		final List<NameBinding> bindings = Lists.newArrayList();
		for (final String methodName : finder.classNamePostions.keySet()) {
			final NameBinding binding = new NameBinding(
					Lists.newArrayList(finder.classNamePostions.get(methodName)),
					codeTokens);
			bindings.add(binding);
		}

		return bindings;

	}

}
