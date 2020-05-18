package org.sevensource.magnolia.backup.executor.backup.filter;

import info.magnolia.jcr.predicate.AbstractPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.List;

class NodePathsPredicate extends AbstractPredicate<Node> {

	private static final Logger log = LoggerFactory.getLogger(NodePathsPredicate.class);
	private final List<String> nodePaths;

	public NodePathsPredicate(List<String> nodePaths) {
		if (nodePaths == null) {
			throw new IllegalArgumentException("nodePaths must not be null");
		}
		this.nodePaths = nodePaths;
	}

	@Override
	public boolean evaluateTyped(Node node) {
		try {
			return !nodePaths.contains(node.getPath());
		} catch (RepositoryException e) {
			return false;
		}
	}
}
