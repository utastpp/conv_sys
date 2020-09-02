/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.rules;

import static javax.swing.JOptionPane.showMessageDialog;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import rdr.rules.Rule;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class RuleTreeModel implements TreeModel{
    
    /**
     *
     */
    protected Rule root;
    
    /** Listeners. */
    protected EventListenerList listenerList = new EventListenerList();
    
    /**
     *
     * @param root
     */
    public RuleTreeModel(Rule root) {
        super();
        this.root = root;
    }
    
    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((Rule)parent).getChildAt(index);
        
    }

    @Override
    public int getChildCount(Object parent) {
        return ((Rule)parent).getChildRuleCount();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((Rule)node).isLeaf();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {

        Rule   aRule = (Rule)path.getLastPathComponent();

        aRule.setCustomObject(newValue);
        nodeChanged(aRule);
        showMessageDialog(null, "Dave HERBERT TREE change");     
        
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if(parent == null || child == null)
            return -1;
        return ((Rule)parent).getIndex((Rule)child);
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listenerList.add(TreeModelListener.class, l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listenerList.remove(TreeModelListener.class, l);
    }

    private void nodeChanged(Rule aRule) {
        if(listenerList != null && aRule != null) {
            Rule         parent = aRule.getParent();

            if(parent != null) {
                int        anIndex = parent.getIndex(aRule);
                if(anIndex != -1) {
                    int[]        cIndexs = new int[1];

                    cIndexs[0] = anIndex;
                    nodesChanged(parent, cIndexs);
                }
            }
            else if (aRule == getRoot()) {
                nodesChanged(aRule, null);
            }
        }
    }

    /**
     *
     * @param aRule
     * @param childIndices
     */
    public void nodesChanged(Rule aRule, int[] childIndices) {
        if(aRule != null) {
            if (childIndices != null) {
                int            cCount = childIndices.length;

                if(cCount > 0) {
                    Object[]       cChildren = new Object[cCount];

                    for(int counter = 0; counter < cCount; counter++)
                        cChildren[counter] = aRule.getChildAt
                            (childIndices[counter]);
                    fireTreeNodesChanged(this, getPathToRoot(aRule),
                                         childIndices, cChildren);
                }
            }
            else if (aRule == getRoot()) {
                fireTreeNodesChanged(this, getPathToRoot(aRule), null, null);
            }
        }
    }

    /**
     *
     * @param aRule
     * @return
     */
    public Rule[] getPathToRoot(Rule aRule) {
        return getPathToRoot(aRule, 0);
    }
    
    /**
     *
     * @param aRule
     * @param depth
     * @return
     */
    protected Rule[] getPathToRoot(Rule aRule, int depth) {
        Rule[]              retNodes;
        // This method recurses, traversing towards the root in order
        // size the array. On the way back, it fills in the nodes,
        // starting from the root and working back to the original node.

        /* Check for null, in case someone passed in a null node, or
           they passed in an element that isn't rooted at root. */
        if(aRule == null) {
            if(depth == 0)
                return null;
            else
                retNodes = new Rule[depth];
        }
        else {
            depth++;
            if(aRule == root)
                retNodes = new Rule[depth];
            else
                retNodes = getPathToRoot(aRule.getParent(), depth);
            retNodes[retNodes.length - depth] = aRule;
        }
        return retNodes;
    }

    /**
     *
     * @param source
     * @param path
     * @param childIndices
     * @param children
     */
    protected void fireTreeNodesChanged(Object source, Object[] path,
                                        int[] childIndices,
                                        Object[] children) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        TreeModelEvent e = null;
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i]==TreeModelListener.class) {
                // Lazily create the event:
                if (e == null)
                    e = new TreeModelEvent(source, path,
                                           childIndices, children);
                ((TreeModelListener)listeners[i+1]).treeNodesChanged(e);
            }
        }
    }
    
}
