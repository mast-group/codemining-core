/**
 * 
 */
package codemining.java.codeutils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.google.common.collect.Maps;

/**
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class JavaApproximateTypeInferencer {

	private class TypeInferencer extends ASTVisitor {

		@Override
		public boolean visit(FieldDeclaration node) {
			final String type = node.getType().toString();
			for (Object fragment : node.fragments()) {
				final VariableDeclarationFragment frag = (VariableDeclarationFragment) fragment;
				fieldTypeMap.put(frag.getName().getIdentifier(), type);
				variables.put(frag.getName().getStartPosition(), type);
			}
			return super.visit(node);
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			methods.put(node.getName().getStartPosition(), node.getName()
					.getIdentifier());
			return super.visit(node);
		}

		@Override
		public boolean visit(MethodInvocation node) {
			methods.put(node.getName().getStartPosition(), node.getName()
					.getIdentifier());
			return super.visit(node);
		}

		@Override
		public boolean visit(SimpleName node) {
			// TODO Sloppy: are all simple names variables?? (NO)
			final String type = variableTypeMap.get(node.getIdentifier());
			if (type != null) {
				variables.put(node.getStartPosition(), type);
			}

			return super.visit(node);
		}

		@Override
		public boolean visit(SingleVariableDeclaration node) {
			final String type = node.getType().toString();
			variableTypeMap.put(node.getName().getIdentifier(), type);
			variables.put(node.getName().getStartPosition(), type);
			return false;
		}

		@Override
		public boolean visit(SuperFieldAccess node) {
			// We don't know anything about this either...
			return false;
		}

		@Override
		public boolean visit(VariableDeclarationStatement node) {
			final String type = node.getType().toString();
			for (Object fragment : node.fragments()) {
				final VariableDeclarationFragment frag = (VariableDeclarationFragment) fragment;
				variableTypeMap.put(frag.getName().getIdentifier(), type);
				variables.put(frag.getName().getStartPosition(), type);
			}
			return super.visit(node);
		}
	}

	public static void main(String[] args) throws IOException {

		final JavaASTExtractor ex = new JavaASTExtractor(false);
		final CompilationUnit cu = ex.getAST(new File(args[0]));
		final JavaApproximateTypeInferencer typeInf = new JavaApproximateTypeInferencer(
				cu);
		typeInf.infer();
		System.out.println(typeInf.variableTypeMap);
		System.out.println(typeInf.fieldTypeMap);
		System.out.println(typeInf.variables);
		System.out.println(typeInf.methods);
	}

	final ASTNode rootNode;

	final Map<String, String> variableTypeMap = Maps.newHashMap();
	final Map<String, String> fieldTypeMap = Maps.newHashMap();

	// A map of the positions where methods are.
	final Map<Integer, String> methods = Maps.newTreeMap();

	final Map<Integer, String> variables = Maps.newTreeMap();

	public JavaApproximateTypeInferencer(final ASTNode node) {
		rootNode = node;
	}

	public Map<String, String> getVariableTypes() {
		final Map<String, String> vars = Maps.newHashMap(variableTypeMap);
		vars.putAll(variableTypeMap);
		return vars;
	}

	public Map<Integer, String> getVariableTypesAtPosition() {
		return variables;
	}

	public void infer() {
		rootNode.accept(new TypeInferencer());
	}

}
