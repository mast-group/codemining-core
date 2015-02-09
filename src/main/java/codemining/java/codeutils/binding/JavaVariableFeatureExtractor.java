/**
 *
 */
package codemining.java.codeutils.binding;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Sets;

/**
 * Utility class to extract features from a variable.
 *
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 *
 */
public class JavaVariableFeatureExtractor {

	/**
	 * @param features
	 * @param declarationPoint
	 */
	private static void getDeclarationFeatures(final Set<String> features,
			final ASTNode declarationPoint) {
		if (declarationPoint.getParent() instanceof SingleVariableDeclaration) {
			final SingleVariableDeclaration declaration = (SingleVariableDeclaration) declarationPoint
					.getParent();
			JavaFeatureExtractor.addTypeFeatures(declaration.getType(),
					features);
			JavaFeatureExtractor.addModifierFeatures(features,
					declaration.modifiers());
			JavaFeatureExtractor.addAstAncestryFeatures(features, declaration);
		} else if (declarationPoint.getParent() instanceof VariableDeclarationStatement) {
			final VariableDeclarationStatement declaration = (VariableDeclarationStatement) declarationPoint
					.getParent();
			JavaFeatureExtractor.addTypeFeatures(declaration.getType(),
					features);
			JavaFeatureExtractor.addModifierFeatures(features,
					declaration.modifiers());
			JavaFeatureExtractor.addAstAncestryFeatures(features, declaration);
		} else if (declarationPoint.getParent() instanceof VariableDeclarationFragment) {
			if (declarationPoint.getParent().getParent() instanceof VariableDeclarationStatement) {
				final VariableDeclarationStatement declaration = (VariableDeclarationStatement) declarationPoint
						.getParent().getParent();
				JavaFeatureExtractor.addTypeFeatures(declaration.getType(),
						features);
				JavaFeatureExtractor.addModifierFeatures(features,
						declaration.modifiers());
				JavaFeatureExtractor.addAstAncestryFeatures(features,
						declaration);
			} else if (declarationPoint.getParent().getParent() instanceof FieldDeclaration) {
				final FieldDeclaration declaration = (FieldDeclaration) declarationPoint
						.getParent().getParent();
				JavaFeatureExtractor.addTypeFeatures(declaration.getType(),
						features);
				JavaFeatureExtractor.addModifierFeatures(features,
						declaration.modifiers());
				JavaFeatureExtractor.addAstAncestryFeatures(features,
						declaration);
			} else if (declarationPoint.getParent().getParent() instanceof VariableDeclarationExpression) {
				final VariableDeclarationExpression declaration = (VariableDeclarationExpression) declarationPoint
						.getParent().getParent();
				JavaFeatureExtractor.addTypeFeatures(declaration.getType(),
						features);
				JavaFeatureExtractor.addModifierFeatures(features,
						declaration.modifiers());
				JavaFeatureExtractor.addAstAncestryFeatures(features,
						declaration);
			}
		} else {
			throw new IllegalStateException("Should not reach this");
		}
	}

	public static Set<String> variableFeatures(
			final Set<ASTNode> boundNodesOfVariable) {
		// Find the declaration and extract features
		final Set<String> features = Sets.newHashSet();
		for (final ASTNode node : boundNodesOfVariable) {
			if (!(node.getParent() instanceof VariableDeclaration)) {
				continue;
			}
			getDeclarationFeatures(features, node);
			JavaFeatureExtractor.addImplementorVocab(node, features);
			break;
		}
		checkArgument(!features.isEmpty());
		return features;
	}

	private JavaVariableFeatureExtractor() {
		// No instantiations
	}

}
