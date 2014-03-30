/**
 * 
 */
package codemining.java.codeutils;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class JavaApproximateTypeInferencer {

	private static class TypeInferencer extends ASTVisitor {
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
		Map<Integer, List<ASTNode>> variableBinding = Maps.newTreeMap();
		/**
		 * Contains the types of the variables at each scope.
		 */
		Map<Integer, String> variableTypes = Maps.newTreeMap();

		/**
		 * Add the binding to the current scope.
		 * 
		 * @param scopeBindings
		 * @param name
		 */
		private void addBinding(final ASTNode node, final String name,
				final String type) {
			final int bindingId = nextDeclarId;
			nextDeclarId++;
			variableNames.get(node).put(name, bindingId);
			variableNames.get(node.getParent()).put(name, bindingId);
			variableBinding.put(bindingId, Lists.<ASTNode> newArrayList());
			variableTypes.put(bindingId, type);
		}

		/**
		 * Add the binding data for the given name at the given scope and
		 * position.
		 */
		private void addBindingData(final String name, final ASTNode nameNode,
				final Map<String, Integer> scopeBindings) {
			// Get varId or abort
			final Integer variableId = scopeBindings.get(name);
			if (variableId == null || !variableBinding.containsKey(variableId)) {
				return;
			}
			variableBinding.get(variableId).add(nameNode);
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
				addBinding(node, frag.getName().getIdentifier(), node.getType()
						.toString());
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
			addBindingData(node.getIdentifier(), node, variableNames.get(node));
			return true;
		}

		/**
		 * Looks for Method Parameters.
		 */
		@Override
		public boolean visit(final SingleVariableDeclaration node) {
			addBinding(node, node.getName().getIdentifier(), node.getType()
					.toString());
			return true;
		}

		/**
		 * Looks for variables declared in for loops.
		 */
		@Override
		public boolean visit(final VariableDeclarationExpression node) {
			for (final Object fragment : node.fragments()) {
				final VariableDeclarationFragment frag = (VariableDeclarationFragment) fragment;
				addBinding(node, frag.getName().getIdentifier(), node.getType()
						.toString());
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
				addBinding(node, frag.getName().getIdentifier(), node.getType()
						.toString());
			}
			return true;
		}
	}

	private final ASTNode rootNode;

	private final TypeInferencer inferencer = new TypeInferencer();

	public JavaApproximateTypeInferencer(final ASTNode node) {
		rootNode = node;
	}

	public Map<String, String> getVariableTypes() {
		final Map<String, String> variableNameTypes = Maps.newTreeMap();
		for (final Entry<Integer, List<ASTNode>> variableBinding : inferencer.variableBinding
				.entrySet()) {
			final String varType = checkNotNull(inferencer.variableTypes
					.get(variableBinding.getKey()));
			for (final ASTNode node : variableBinding.getValue()) {
				variableNameTypes.put(node.toString(), varType);
			}
		}
		return variableNameTypes;
	}

	public Map<Integer, String> getVariableTypesAtPosition() {
		final Map<Integer, String> variableTypes = Maps.newTreeMap();

		for (final Entry<Integer, List<ASTNode>> variableBinding : inferencer.variableBinding
				.entrySet()) {
			final String varType = checkNotNull(inferencer.variableTypes
					.get(variableBinding.getKey()));
			for (final ASTNode node : variableBinding.getValue()) {
				variableTypes.put(node.getStartPosition(), varType);
			}
		}

		return variableTypes;
	}

	public void infer() {
		rootNode.accept(inferencer);
	}

}
