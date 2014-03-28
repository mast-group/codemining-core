/**
 * 
 */
package codemining.java.codeutils.binding;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import codemining.java.codeutils.JavaTokenizer;
import codemining.languagetools.NameBinding;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * An approximate best-effort (worse-precision) variable binding extractor.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class JavaApproximateVariableBindingExtractor extends
		AbstractJavaNameBindingsExtractor {

	/**
	 * This class looks for declarations of variables and the references to
	 * them.
	 * 
	 */
	private class VariableBindingFinder extends ASTVisitor {

		private final Map<Integer, Integer> positionToIndex;

		private int nextDeclarId = 0;

		/**
		 * Map the names that are defined in each ast node, with their
		 * respective ids.
		 */
		private final Map<ASTNode, Map<String, Integer>> variableNames = Maps
				.newIdentityHashMap();

		/**
		 * Map of variables (represented with their ids) to all token positions
		 * where the variable is referenced.
		 */
		Map<Integer, List<Integer>> variableBinding = Maps.newIdentityHashMap();

		VariableBindingFinder(final Map<Integer, Integer> positionToIndex) {
			this.positionToIndex = positionToIndex;
		}

		/**
		 * Add the binding to the current scope.
		 * 
		 * @param scopeBindings
		 * @param name
		 */
		private void addBinding(final ASTNode node, final String name) {
			final int bindingId = nextDeclarId;
			nextDeclarId++;
			variableNames.get(node).put(name, bindingId);
			variableNames.get(node.getParent()).put(name, bindingId);
			variableBinding.put(bindingId, Lists.<Integer> newArrayList());
		}

		/**
		 * Add the binding data for the given name at the given scope and
		 * position.
		 */
		private void addBindingData(final String name, final int position,
				final Map<String, Integer> scopeBindings) {
			// Get varId or abort
			final Integer variableId = scopeBindings.get(name);
			if (variableId == null) {
				return;
			}
			variableBinding.get(variableId).add(positionToIndex.get(position));
		}

		@Override
		public void preVisit(final ASTNode node) {
			final ASTNode parent = node.getParent();
			if (parent != null && variableNames.containsKey(parent)) {
				// inherit all variables in parent scope
				final Map<String, Integer> bindingsCopy = Maps.newTreeMap();
				for (final Entry<String, Integer> binding : variableNames.get(
						parent).entrySet()) {
					bindingsCopy.put(binding.getKey(), binding.getValue());
				}

				variableNames.put(node, bindingsCopy);
			} else {
				// Start from scratch
				variableNames.put(node, Maps.<String, Integer> newTreeMap());
			}
			super.preVisit(node);
		}

		/**
		 * Looks for field declarations (i.e. class member variables).
		 */
		@Override
		public boolean visit(final FieldDeclaration node) {
			for (final Object fragment : node.fragments()) {
				final VariableDeclarationFragment frag = (VariableDeclarationFragment) fragment;
				addBinding(node, frag.getName().getIdentifier());
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
			addBindingData(node.getIdentifier(), node.getStartPosition(),
					variableNames.get(node));
			return true;
		}

		/**
		 * Looks for Method Parameters.
		 */
		@Override
		public boolean visit(final SingleVariableDeclaration node) {
			addBinding(node, node.getName().getIdentifier());
			return true;
		}

		/**
		 * Looks for variables declared in for loops.
		 */
		@Override
		public boolean visit(final VariableDeclarationExpression node) {
			for (final Object fragment : node.fragments()) {
				final VariableDeclarationFragment frag = (VariableDeclarationFragment) fragment;
				addBinding(node, frag.getName().getIdentifier());
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
				addBinding(node, frag.getName().getIdentifier());
			}
			return true;
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

		final VariableBindingFinder bindingFinder = new VariableBindingFinder(
				positionToIndex);
		node.accept(bindingFinder);

		final List<NameBinding> bindings = Lists.newArrayList();
		for (final Entry<Integer, List<Integer>> variable : bindingFinder.variableBinding
				.entrySet()) {
			final NameBinding binding = new NameBinding(variable.getValue(),
					codeTokens);
			bindings.add(binding);
		}

		return bindings;
	}

}
