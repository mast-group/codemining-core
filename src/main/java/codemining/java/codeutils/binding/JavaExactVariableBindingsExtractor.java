/**
 * 
 */
package codemining.java.codeutils.binding;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import codemining.java.codeutils.JavaASTExtractor;
import codemining.java.codeutils.JavaTokenizer;
import codemining.languagetools.NameBinding;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Retrieve the variable bindings, given an ASTNode. This finds exact bindings
 * to the detriment of recall. Partial code snippets are not supported.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class JavaExactVariableBindingsExtractor extends
		AbstractJavaNameBindingsExtractor {

	/**
	 * This class looks for declarations of variables and the references to
	 * them.
	 * 
	 */
	private class VariableBindingFinder extends ASTVisitor {

		private final Map<Integer, Integer> positionToIndex;

		/**
		 * Map of variables (represented as bindings) to all token positions
		 * where the variable is referenced.
		 */
		Map<IVariableBinding, List<Integer>> variableScope = Maps
				.newIdentityHashMap();

		VariableBindingFinder(final Map<Integer, Integer> positionToIndex) {
			this.positionToIndex = positionToIndex;
		}

		private void addBinding(final IVariableBinding binding) {
			variableScope.put(binding, Lists.<Integer> newArrayList());
		}

		/**
		 * @param binding
		 */
		private void addBindingData(final IVariableBinding binding,
				final int position) {
			if (binding == null) {
				return; // Sorry, cannot do anything.
			}
			final int tokenIdx = checkNotNull(positionToIndex.get(position));
			final List<Integer> thisVarBindings = checkNotNull(
					variableScope.get(binding),
					"Binding was not previously found");
			thisVarBindings.add(tokenIdx);
		}

		/**
		 * Looks for field declarations (i.e. class member variables).
		 */
		@Override
		public boolean visit(final FieldDeclaration node) {
			for (final Object fragment : node.fragments()) {
				final VariableDeclarationFragment frag = (VariableDeclarationFragment) fragment;
				final IVariableBinding binding = frag.resolveBinding();
				addBinding(binding);
			}
			return true;
		}

		/**
		 * Visits {@link SimpleName} AST nodes. Resolves the binding of the
		 * simple name and looks for it in the {@link #variableScope} map. If
		 * the binding is found, this is a reference to a variable.
		 * 
		 * @param node
		 *            the node to visit
		 */
		@Override
		public boolean visit(final SimpleName node) {
			final IBinding binding = node.resolveBinding();
			if (variableScope.containsKey(binding)) {
				addBindingData((IVariableBinding) binding,
						node.getStartPosition());
			}
			return true;
		}

		/**
		 * Looks for Method Parameters.
		 */
		@Override
		public boolean visit(final SingleVariableDeclaration node) {
			final IVariableBinding binding = node.resolveBinding();
			if (binding != null) {
				addBinding(binding);
			}
			return true;
		}

		/**
		 * Looks for variables declared in for loops.
		 */
		@Override
		public boolean visit(final VariableDeclarationExpression node) {
			for (final Object fragment : node.fragments()) {
				final VariableDeclarationFragment frag = (VariableDeclarationFragment) fragment;
				final IVariableBinding binding = frag.resolveBinding();
				if (binding != null) {
					addBinding(binding);
				}
			}
			return true;
		}

		/**
		 * Looks for local variable declarations. For every declaration of a
		 * variable, the parent {@link Block} denoting the variable's scope is
		 * stored in {@link #variableScope} map.
		 * 
		 * @param node
		 *            the node to visit
		 */
		@Override
		public boolean visit(final VariableDeclarationStatement node) {
			for (final Object fragment : node.fragments()) {
				final VariableDeclarationFragment frag = (VariableDeclarationFragment) fragment;
				final IVariableBinding binding = frag.resolveBinding();
				if (binding != null) {
					addBinding(binding);
				}
			}
			return true;
		}
	}

	@Override
	protected JavaASTExtractor createExtractor() {
		return new JavaASTExtractor(true);
	}

	@Override
	public List<NameBinding> getNameBindings(final ASTNode node,
			final String sourceCode) {
		final JavaTokenizer tokenizer = new JavaTokenizer();
		final SortedMap<Integer, String> tokenPositions = tokenizer
				.tokenListWithPos(sourceCode.toCharArray());
		final SortedMap<Integer, Integer> positionToIndex = getTokenIndexForPostion(tokenPositions);
		final List<String> codeTokens = Lists.newArrayList(tokenPositions
				.values());

		final VariableBindingFinder bindingFinder = new VariableBindingFinder(
				positionToIndex);
		node.accept(bindingFinder);

		final List<NameBinding> bindings = Lists.newArrayList();
		for (final Entry<IVariableBinding, List<Integer>> variable : bindingFinder.variableScope
				.entrySet()) {
			final NameBinding binding = new NameBinding(variable.getValue(),
					codeTokens);
			bindings.add(binding);
		}

		return bindings;
	}

	@Override
	public List<NameBinding> getNameBindings(final String code) {
		throw new UnsupportedOperationException(
				"Partial snippets cannot be resolved due to the "
						+ "lack of support from Eclipse JDT. Consider using the approximate binding extractor.");
	}
}
