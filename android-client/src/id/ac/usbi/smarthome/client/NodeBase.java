package id.ac.usbi.smarthome.client;

import java.util.TreeMap;

/**
 * Created by andhieka on 29/5/14.
 */
public abstract class NodeBase {
    private String _node_id = null;
    private static TreeMap<String, NodeBase> _members = new TreeMap<String, NodeBase>();

    /**
     * @param node_id A unique identifier of a node.
     */
    public NodeBase(String node_id) throws InvalidNodeIdException {
        if (! _members.containsKey(node_id)) {
            _node_id = node_id;
            _members.put(node_id, this);
        } else {
            throw new InvalidNodeIdException("NodeBase::NodeBase -- There already exists a node with node_id = " + node_id);
        }
    }


    /* GETTER AND SETTER METHODS */

    /**
     * @return The unique identifier of this node.
     */
    public String getId() {
        return _node_id;
    }


    /* STATIC METHODS */

    /**
     * @param node_id The ID of the Node to be searched.
     * @return The <code>NodeBase</code> with corresponding <code>node_id</code>, or <code>null</code>
     * if there is no such Node.
     */
    public static NodeBase getMember(String node_id) {
        return _members.get(node_id);
    }

    /* INTERNAL CLASSES */
    public class InvalidNodeIdException extends Exception {
        public InvalidNodeIdException(String detailMessage) {
            super(detailMessage);
        }
    }
}
