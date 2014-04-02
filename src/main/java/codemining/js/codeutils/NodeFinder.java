/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package codemining.js.codeutils;

import org.eclipse.wst.jsdt.core.IBuffer;
import org.eclipse.wst.jsdt.core.ISourceRange;
import org.eclipse.wst.jsdt.core.ITypeRoot;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.ToolFactory;
import org.eclipse.wst.jsdt.core.compiler.IScanner;
import org.eclipse.wst.jsdt.core.compiler.ITerminalSymbols;
import org.eclipse.wst.jsdt.core.compiler.InvalidInputException;
import org.eclipse.wst.jsdt.core.dom.ASTNode;
import org.eclipse.wst.jsdt.core.dom.ASTVisitor;

/**
 * For a given range, finds the covered node and the covering node. Ported to
 * JavaScript by Jaroslav Fowkes.
 * 
 * @since 3.5
 */
public final class NodeFinder {
	/**
	 * This class defines the actual visitor that finds the node.
	 */
	private static class NodeFinderVisitor extends ASTVisitor {
		private final int fStart;
		private final int fEnd;
		private ASTNode fCoveringNode;
		private ASTNode fCoveredNode;

		NodeFinderVisitor(final int offset, final int length) {
			super(true); // include Javadoc tags
			this.fStart = offset;
			this.fEnd = offset + length;
		}

		public boolean preVisit2(final ASTNode node) {
			final int nodeStart = node.getStartPosition();
			final int nodeEnd = nodeStart + node.getLength();
			if (nodeEnd < this.fStart || this.fEnd < nodeStart) {
				return false;
			}
			if (nodeStart <= this.fStart && this.fEnd <= nodeEnd) {
				this.fCoveringNode = node;
			}
			if (this.fStart <= nodeStart && nodeEnd <= this.fEnd) {
				if (this.fCoveringNode == node) { // nodeStart == fStart &&
													// nodeEnd == fEnd
					this.fCoveredNode = node;
					return true; // look further for node with same length as
									// parent
				} else if (this.fCoveredNode == null) { // no better found
					this.fCoveredNode = node;
				}
				return false;
			}
			return true;
		}

		/**
		 * Returns the covered node. If more than one nodes are covered by the
		 * selection, the returned node is first covered node found in a
		 * top-down traversal of the AST
		 * 
		 * @return ASTNode
		 */
		public ASTNode getCoveredNode() {
			return this.fCoveredNode;
		}

		/**
		 * Returns the covering node. If more than one nodes are covering the
		 * selection, the returned node is last covering node found in a
		 * top-down traversal of the AST
		 * 
		 * @return ASTNode
		 */
		public ASTNode getCoveringNode() {
			return this.fCoveringNode;
		}
	}

	/**
	 * Maps a selection to a given ASTNode, where the selection is defined using
	 * a start and a length. The result node is determined as follows:
	 * <ul>
	 * <li>first the visitor tries to find a node with the exact
	 * <code>start</code> and <code>length</code></li>
	 * <li>if no such node exists then the node that encloses the range defined
	 * by <code>start</code> and <code>length</code> is returned.</li>
	 * <li>if the length is zero then also nodes are considered where the node's
	 * start or end position matches <code>start</code>.</li>
	 * <li>otherwise <code>null</code> is returned.</li>
	 * </ul>
	 * 
	 * @param root
	 *            the root node from which the search starts
	 * @param start
	 *            the given start
	 * @param length
	 *            the given length
	 * 
	 * @return the found node
	 */
	public static ASTNode perform(final ASTNode root, final int start,
			final int length) {
		final NodeFinder finder = new NodeFinder(root, start, length);
		final ASTNode result = finder.getCoveredNode();
		if (result == null || result.getStartPosition() != start
				|| result.getLength() != length) {
			return finder.getCoveringNode();
		}
		return result;
	}

	/**
	 * Maps a selection to a given ASTNode, where the selection is defined using
	 * a source range. It calls
	 * <code>perform(root, range.getOffset(), range.getLength())</code>.
	 * 
	 * @return the result node
	 * @see #perform(ASTNode, int, int)
	 */
	public static ASTNode perform(final ASTNode root, final ISourceRange range) {
		return perform(root, range.getOffset(), range.getLength());
	}

	/**
	 * Maps a selection to a given ASTNode, where the selection is given by a
	 * start and a length. The result node is determined as follows:
	 * <ul>
	 * <li>first the visitor tries to find a node that is covered by
	 * <code>start</code> and <code>length</code> where either
	 * <code>start</code> and <code>length</code> exactly matches the node or
	 * where the text covered before and after the node only consists of white
	 * spaces or comments.</li>
	 * <li>if no such node exists then the node that encloses the range defined
	 * by <code>start</code> and <code>length</code> is returned.</li>
	 * <li>if the length is zero then also nodes are considered where the node's
	 * start or end position matches <code>start</code>.</li>
	 * <li>otherwise <code>null</code> is returned.</li>
	 * </ul>
	 * 
	 * @param root
	 *            the root node from which the search starts
	 * @param start
	 *            the given start
	 * @param length
	 *            the given length
	 * @param source
	 *            the source of the compilation unit
	 * 
	 * @return the result node
	 * @throws JavaScriptModelException
	 *             if an error occurs in the JavaScript model
	 */
	public static ASTNode perform(final ASTNode root, final int start,
			final int length, final ITypeRoot source)
			throws JavaScriptModelException {
		final NodeFinder finder = new NodeFinder(root, start, length);
		final ASTNode result = finder.getCoveredNode();
		if (result == null)
			return null;
		final int nodeStart = result.getStartPosition();
		if (start <= nodeStart
				&& ((nodeStart + result.getLength()) <= (start + length))) {
			final IBuffer buffer = source.getBuffer();
			if (buffer != null) {
				final IScanner scanner = ToolFactory.createScanner(false,
						false, false, false);
				try {
					scanner.setSource(buffer.getText(start, length)
							.toCharArray());
					int token = scanner.getNextToken();
					if (token != ITerminalSymbols.TokenNameEOF) {
						final int tStart = scanner
								.getCurrentTokenStartPosition();
						if (tStart == result.getStartPosition() - start) {
							scanner.resetTo(tStart + result.getLength(),
									length - 1);
							token = scanner.getNextToken();
							if (token == ITerminalSymbols.TokenNameEOF)
								return result;
						}
					}
				} catch (final InvalidInputException e) {
					// ignore
				} catch (final IndexOutOfBoundsException e) {
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=305001
					return null;
				}
			}
		}
		return finder.getCoveringNode();
	}

	private final ASTNode fCoveringNode;
	private final ASTNode fCoveredNode;

	/**
	 * Instantiate a new node finder using the given root node, the given start
	 * and the given length.
	 * 
	 * @param root
	 *            the given root node
	 * @param start
	 *            the given start
	 * @param length
	 *            the given length
	 */
	public NodeFinder(final ASTNode root, final int start, final int length) {
		final NodeFinderVisitor nodeFinderVisitor = new NodeFinderVisitor(
				start, length);
		root.accept(nodeFinderVisitor);
		this.fCoveredNode = nodeFinderVisitor.getCoveredNode();
		this.fCoveringNode = nodeFinderVisitor.getCoveringNode();
	}

	/**
	 * Returns the covered node. If more than one nodes are covered by the
	 * selection, the returned node is first covered node found in a top-down
	 * traversal of the AST.
	 * 
	 * @return the covered node
	 */
	public ASTNode getCoveredNode() {
		return this.fCoveredNode;
	}

	/**
	 * Returns the covering node. If more than one nodes are covering the
	 * selection, the returned node is last covering node found in a top-down
	 * traversal of the AST.
	 * 
	 * @return the covering node
	 */
	public ASTNode getCoveringNode() {
		return this.fCoveringNode;
	}
}
