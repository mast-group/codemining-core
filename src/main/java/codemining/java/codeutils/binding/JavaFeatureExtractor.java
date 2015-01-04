/**
 *
 */
package codemining.java.codeutils.binding;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Utility class to get various features, related to bindings.
 *
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 *
 */
public class JavaFeatureExtractor {

	/**
	 * Extract the token parts from the contents of a method declaration.
	 *
	 */
	private static class MethodTopicNames extends ASTVisitor {
		final Set<String> nameParts = Sets.newHashSet();

		void populateNames(final MethodDeclaration declaration) {
			for (final Object param : declaration.parameters()) {
				((ASTNode) param).accept(this);
			}
			if (declaration.getBody() != null) {
				declaration.getBody().accept(this);
			}
		}

		@Override
		public boolean visit(final SimpleName node) {
			nameParts.addAll(JavaFeatureExtractor.getNameParts(node
					.getIdentifier()));
			return super.visit(node);
		}
	}

	/**
	 * Add ancestry features (parent and grandparent) of a node)
	 *
	 * @param features
	 * @param node
	 */
	public static void addAstAncestryFeatures(final Set<String> features,
			final ASTNode node) {
		features.add("DeclParentAstType:"
				+ ASTNode.nodeClassForType(node.getParent().getNodeType())
						.getSimpleName());
		features.add("DeclGrandparentAstType:"
				+ ASTNode.nodeClassForType(
						node.getParent().getParent().getNodeType())
						.getSimpleName());
	}

	/**
	 * Add the token parts of the method or class where the current node is
	 * placed.
	 *
	 * @param node
	 * @param features
	 */
	public static void addImplementorVocab(final ASTNode node,
			final Set<String> features) {
		ASTNode currentNode = node;
		List<String> tokenParts = null;
		while (currentNode.getParent() != null) {
			currentNode = currentNode.getParent();
			if (currentNode instanceof MethodDeclaration) {
				final MethodDeclaration md = (MethodDeclaration) currentNode;
				tokenParts = JavaFeatureExtractor.getNameParts(md.getName()
						.toString());
				break;
			} else if (currentNode instanceof TypeDeclaration) {
				final TypeDeclaration td = (TypeDeclaration) currentNode;
				tokenParts = JavaFeatureExtractor.getNameParts(td.getName()
						.toString());
				break;
			}
		}

		if (tokenParts != null) {
			for (final String tokenPart : tokenParts) {
				features.add("inName:" + tokenPart);
			}
		}
	}

	/**
	 * Add any modifiers as features.
	 *
	 * @param features
	 * @param modifiers
	 */
	public static void addModifierFeatures(final Set<String> features,
			final List<?> modifiers) {
		for (final Object modifier : modifiers) {
			final IExtendedModifier extendedModifier = (IExtendedModifier) modifier;
			features.add(extendedModifier.toString());
		}
	}

	/**
	 * Return the features to
	 *
	 * @param declaration
	 * @param features
	 *            where the features will be added.
	 */
	public static void getMethodTopicFeatures(
			final MethodDeclaration declaration, final Set<String> features) {
		final MethodTopicNames namesExtractor = new MethodTopicNames();
		namesExtractor.populateNames(declaration);
		features.addAll(namesExtractor.nameParts);
	}

	public static List<String> getNameParts(final String name) {
		final List<String> nameParts = Lists.newArrayList();
		for (String snakecasePart : name.split("_")) {
			for (final String w : snakecasePart
					.split("(?<!(^|[A-Z]))(?=[A-Z0-9])|(?<!^)(?=[A-Z][a-z])")) {
				nameParts.add(w.toLowerCase());
			}
		}
		return nameParts;
	}

	/**
	 * @param features
	 * @param type
	 */
	public static void getTypeFeatures(final Set<String> features,
			final Type type) {
		features.add(type.toString());
		if (type.isParameterizedType()) {
			features.add("isParameterizedType");
			final ParameterizedType paramType = (ParameterizedType) type;
			features.add(paramType.getType().toString());
		} else if (type.isArrayType()) {
			features.add("isArrayType");
			final ArrayType arrayType = (ArrayType) type;
			features.add("arrayDims:" + arrayType.dimensions().size());
			features.add("arrayType:" + arrayType.getElementType().toString());
		}
	}

	private JavaFeatureExtractor() {
	}

}
