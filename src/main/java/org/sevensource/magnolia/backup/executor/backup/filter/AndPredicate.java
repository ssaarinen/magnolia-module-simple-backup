package org.sevensource.magnolia.backup.executor.backup.filter;

import info.magnolia.jcr.predicate.AbstractPredicate;

import javax.jcr.Node;

class AndPredicate extends AbstractPredicate<Node> {
	private final AbstractPredicate<Node>[] predicates;

	public AndPredicate(final AbstractPredicate<Node>... predicates) {
		this.predicates = predicates;
	}

	@Override
	public boolean evaluateTyped(Node node) {
		try {
			for (AbstractPredicate<Node> predicate : predicates) {
				if (!predicate.evaluateTyped(node)) {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}

		return true;
	}
}
