/**
 * 
 */
package codemining.java.codeutils.binding;

import java.util.Set;

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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Extract Java type name bindings.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class JavaTypeBindingExtractor extends AbstractJavaNameBindingsExtractor {

	private static class ClassnameFinder extends ASTVisitor {

		Multimap<String, ASTNode> classNamePostions = HashMultimap.create();

		@Override
		public boolean visit(final CastExpression node) {
			final String type = node.getType().toString();
			classNamePostions.put(type, node.getType());
			return super.visit(node);
		}

		@Override
		public boolean visit(final ClassInstanceCreation node) {
			final String type = node.getType().toString();
			classNamePostions.put(type, node.getType());
			return super.visit(node);
		}

		@Override
		public boolean visit(final EnumDeclaration node) {
			classNamePostions.put(node.getName().getIdentifier(),
					node.getName());
			return super.visit(node);
		}

		@Override
		public boolean visit(final FieldDeclaration node) {
			final String type = node.getType().toString();
			classNamePostions.put(type, node.getType());
			return super.visit(node);
		}

		@Override
		public boolean visit(final SingleVariableDeclaration node) {
			final String type = node.getType().toString();
			classNamePostions.put(type, node.getType());
			return false;
		}

		@Override
		public boolean visit(final TypeDeclaration node) {
			classNamePostions.put(node.getName().getIdentifier(),
					node.getName());
			return super.visit(node);
		}

		@Override
		public boolean visit(final TypeLiteral node) {
			final String type = node.getType().toString();
			classNamePostions.put(type, node.getType());
			return super.visit(node);
		}

		@Override
		public boolean visit(final VariableDeclarationExpression node) {
			final String type = node.getType().toString();
			classNamePostions.put(type, node.getType());
			return super.visit(node);
		}

		@Override
		public boolean visit(final VariableDeclarationStatement node) {
			final String type = node.getType().toString();
			classNamePostions.put(type, node.getType());
			return super.visit(node);
		}

	}

	@Override
	public Set<Set<ASTNode>> getNameBindings(final ASTNode node) {
		final ClassnameFinder finder = new ClassnameFinder();
		node.accept(finder);

		final Set<Set<ASTNode>> nameBindings = Sets.newHashSet();
		for (final String methodName : finder.classNamePostions.keySet()) {
			final Set<ASTNode> boundNodes = Sets.newIdentityHashSet();
			boundNodes.addAll(finder.classNamePostions.get(methodName));
			nameBindings.add(boundNodes);
		}
		return nameBindings;
	}

}
