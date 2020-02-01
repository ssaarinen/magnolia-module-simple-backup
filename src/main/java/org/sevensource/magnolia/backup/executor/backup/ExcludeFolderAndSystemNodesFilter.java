package org.sevensource.magnolia.backup.executor.backup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.jackrabbit.JcrConstants;

import com.google.common.collect.Lists;

import info.magnolia.jcr.decoration.NodePredicateContentDecorator;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.predicate.NodeFilteringPredicate;
import info.magnolia.jcr.predicate.PropertyFilteringPredicate;

final class ExcludeFolderAndSystemNodesFilter extends NodePredicateContentDecorator {

	public static final List<String> SPLITTABLE_NODE_TYPES =
			Collections.unmodifiableList(Arrays.asList("mgnl:folder", "mgnl:content"));
	
	
    public ExcludeFolderAndSystemNodesFilter() {
    	
    	final List<String> nodeTypes = new ArrayList<>();
    	nodeTypes.addAll(SPLITTABLE_NODE_TYPES);
    	nodeTypes.add("rep:AccessControl");
    	nodeTypes.add("rep:root");
    	nodeTypes.add("rep:system");
    	
        final NodeFilteringPredicate nodePredicate = new NodeFilteringPredicate();
        nodePredicate.setNodeTypes(nodeTypes);
        
        final PropertyFilteringPredicate propertyPredicate = new PropertyFilteringPredicate();
        propertyPredicate.setExcludedNames(Lists.newArrayList("jcr:createdBy", JcrConstants.JCR_CREATED));
        
        setPropertyPredicate(propertyPredicate);
        setNodePredicate(nodePredicate);
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

    @Override
    public NodeFilteringPredicate getNodePredicate() {
        return (NodeFilteringPredicate) super.getNodePredicate();
    }

    @Override
    public void setNodePredicate(AbstractPredicate<Node> propertyPredicate) {
        if (propertyPredicate instanceof NodeFilteringPredicate) {
            super.setNodePredicate(propertyPredicate);
        } else {
            throw new IllegalArgumentException(String.format("Expected instances of {%s} but got {%s}", PropertyFilteringPredicate.class, propertyPredicate.getClass()));
        }
    }
}