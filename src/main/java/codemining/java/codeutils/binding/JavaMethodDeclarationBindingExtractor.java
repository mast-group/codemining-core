/**
 *
 */
package codemining.java.codeutils.binding;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import codemining.java.codedata.metrics.CyclomaticCalculator;
import codemining.java.codeutils.MethodUtils;
import codemining.java.tokenizers.JavaTokenizer;
import codemining.languagetools.ITokenizer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Extract bindings (and features) for method delarations.
 *
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 *
 */
public class JavaMethodDeclarationBindingExtractor extends
		AbstractJavaNameBindingsExtractor {

	public static enum AvailableFeatures {
		ARGUMENTS, EXCEPTIONS, RETURN_TYPE, MODIFIERS, ANCESTRY, METHOD_TOPICS, IMPLEMENTOR_VOCABULARY, FIELDS, SIBLING_METHODS, CYCLOMATIC
	}

	private class MethodBindings extends ASTVisitor {
		/**
		 * A map from the method name to the position.
		 */
		Multimap<String, ASTNode> methodNamePostions = HashMultimap.create();

		@Override
		public boolean visit(final MethodDeclaration node) {
			if (node.isConstructor()) {
				return super.visit(node);
			} else if (!includeOverrides && MethodUtils.hasOverrideAnnotation(node)) {
				return super.visit(node);
			}
			final String name = node.getName().toString();
			methodNamePostions.put(name, node.getName());
			return super.visit(node);
		}
	}

	private final boolean includeOverrides;

	private final Set<AvailableFeatures> activeFeatures = Sets
			.newHashSet(AvailableFeatures.values());

	public JavaMethodDeclarationBindingExtractor() {
		super(new JavaTokenizer());
		this.includeOverrides = true;
	}

	public JavaMethodDeclarationBindingExtractor(final boolean includeOverrides) {
		super(new JavaTokenizer());
		this.includeOverrides = includeOverrides;
	}

	public JavaMethodDeclarationBindingExtractor(final ITokenizer tokenizer) {
		super(tokenizer);
		this.includeOverrides = true;
	}

	public JavaMethodDeclarationBindingExtractor(final ITokenizer tokenizer,
			final boolean includeOverrides) {
		super(tokenizer);
		this.includeOverrides = includeOverrides;
	}

	/**
	 * Add argument-related features.
	 *
	 * @param md
	 * @param features
	 */
	private void addArgumentFeatures(final MethodDeclaration md,
			final Set<String> features) {
		checkArgument(activeFeatures.contains(AvailableFeatures.ARGUMENTS));
		features.add("nParams:" + md.parameters().size());
		for (int i = 0; i < md.parameters().size(); i++) {
			final SingleVariableDeclaration varDecl = (SingleVariableDeclaration) md
					.parameters().get(i);
			features.add("param" + i + "Type:" + varDecl.getType().toString());
			for (final String namepart : JavaFeatureExtractor
					.getNameParts(varDecl.getName().toString())) {
				features.add("paramName:" + namepart);
			}
		}

		if (md.isVarargs()) {
			features.add("isVarArg");
		}
	}

	/**
	 * Add exception related features.
	 *
	 * @param md
	 * @param features
	 */
	private void addExceptionFeatures(final MethodDeclaration md,
			final Set<String> features) {
		checkArgument(activeFeatures.contains(AvailableFeatures.EXCEPTIONS));
		for (final Object exception : md.thrownExceptionTypes()) {
			final SimpleType ex = (SimpleType) exception;
			features.add("thrownException:" + ex.toString());
		}
	}

	/**
	 * Add modifier-related features.
	 *
	 * @param md
	 * @param features
	 */
	private void addModifierFeatures(final MethodDeclaration md,
			final Set<String> features) {
		checkArgument(activeFeatures.contains(AvailableFeatures.MODIFIERS));
		JavaFeatureExtractor.addModifierFeatures(features, md.modifiers());

		if (md.getBody() == null) {
			features.add("isInterfaceDeclaration");
		}
	}

	@Override
	public Set<?> getAvailableFeatures() {
		return Sets.newHashSet(AvailableFeatures.values());
	}

	@Override
	protected Set<String> getFeatures(final Set<ASTNode> boundNodes) {
		checkArgument(boundNodes.size() == 1);
		final ASTNode method = boundNodes.iterator().next().getParent();
		final Set<String> features = Sets.newHashSet();

		checkArgument(method instanceof MethodDeclaration);
		final MethodDeclaration md = (MethodDeclaration) method;
		if (activeFeatures.contains(AvailableFeatures.ARGUMENTS)) {
			addArgumentFeatures(md, features);
		}
		if (activeFeatures.contains(AvailableFeatures.EXCEPTIONS)) {
			addExceptionFeatures(md, features);
		}

		if (activeFeatures.contains(AvailableFeatures.RETURN_TYPE)) {
			features.add("returnType:" + md.getReturnType2());
		}
		if (activeFeatures.contains(AvailableFeatures.MODIFIERS)) {
			addModifierFeatures(md, features);
		}

		if (activeFeatures.contains(AvailableFeatures.ANCESTRY)) {
			JavaFeatureExtractor.addAstAncestryFeatures(features, method);
		}
		if (activeFeatures.contains(AvailableFeatures.METHOD_TOPICS)) {
			JavaFeatureExtractor.addMethodTopicFeatures(md, features);
		}
		if (activeFeatures.contains(AvailableFeatures.IMPLEMENTOR_VOCABULARY)) {
			JavaFeatureExtractor.addImplementorVocab(method, features);
		}
		if (activeFeatures.contains(AvailableFeatures.FIELDS)) {
			JavaFeatureExtractor.addFields(method, features);
		}
		if (activeFeatures.contains(AvailableFeatures.SIBLING_METHODS)) {
			JavaFeatureExtractor.addSiblingMethodNames(md, features);
		}
		if (activeFeatures.contains(AvailableFeatures.CYCLOMATIC)) {
			features.add("cyclomatic:"
					+ (int) (new CyclomaticCalculator()
							.getMetricForASTNode(method)));
		}
		return features;
	}

	@Override
	public Set<Set<ASTNode>> getNameBindings(final ASTNode node) {
		final MethodBindings mb = new MethodBindings();
		node.accept(mb);

		final Set<Set<ASTNode>> nameBindings = Sets.newHashSet();
		for (final Entry<String, ASTNode> entry : mb.methodNamePostions
				.entries()) {
			final Set<ASTNode> boundNodes = Sets.newIdentityHashSet();
			boundNodes.add(entry.getValue());
			nameBindings.add(boundNodes);
		}
		return nameBindings;
	}

	@Override
	public void setActiveFeatures(final Set<?> activeFeatures) {
		this.activeFeatures.clear();
		this.activeFeatures
				.addAll((Collection<? extends AvailableFeatures>) activeFeatures);
	}

}
