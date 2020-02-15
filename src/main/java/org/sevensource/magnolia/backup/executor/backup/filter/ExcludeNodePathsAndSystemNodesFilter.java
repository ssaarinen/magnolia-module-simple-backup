package org.sevensource.magnolia.backup.executor.backup.filter;

import com.google.common.collect.Lists;
import info.magnolia.jcr.decoration.NodePredicateContentDecorator;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.predicate.NodeFilteringPredicate;
import info.magnolia.jcr.predicate.PropertyFilteringPredicate;
import org.apache.jackrabbit.JcrConstants;

import javax.jcr.Property;
import java.util.ArrayList;
import java.util.List;

public class ExcludeNodePathsAndSystemNodesFilter extends NodePredicateContentDecorator {

    public ExcludeNodePathsAndSystemNodesFilter(List<String> nodePaths) {

    	final List<String> nodeTypes = new ArrayList<>();
    	nodeTypes.add("rep:AccessControl");
    	nodeTypes.add("rep:root");
    	nodeTypes.add("rep:system");

        final NodeFilteringPredicate nodePredicate = new NodeFilteringPredicate();
        nodePredicate.setNodeTypes(nodeTypes);

        if(nodePaths != null && nodePaths.size() != 0) {
			final NodePathsPredicate nodePathsPredicate = new NodePathsPredicate(nodePaths);
			setNodePredicate(new AndPredicate(nodePredicate, nodePathsPredicate));
		} else {
			setNodePredicate(nodePredicate);
		}

        final PropertyFilteringPredicate propertyPredicate = new PropertyFilteringPredicate();
        propertyPredicate.setExcludedNames(Lists.newArrayList("jcr:createdBy", JcrConstants.JCR_CREATED));

        setPropertyPredicate(propertyPredicate);
    }

    @Override
    public PropertyFilteringPredicate getPropertyPredicate() {
        return (PropertyFilteringPredicate) super.getPropertyPredicate();
    }

    @Override
    public void setPropertyPredicate(AbstractPredicate<Property> propertyPredicate) {
        if (propertyPredicate instanceof PropertyFilteringPredicate) {
            super.setPropertyPredicate(propertyPredicate);
        } else {
            throw new IllegalArgumentException(String.format("Expected instances of {%s} but got {%s}", PropertyFilteringPredicate.class, propertyPredicate.getClass()));
        }
    }
}

