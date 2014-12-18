package id.ac.usbi.smarthome.client;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by andhieka on 29/5/14.
 */
public class MasterNode extends NodeBase {
    private ArrayList<ClientNode> client_nodes;
    private static TreeMap<String, MasterNode> _members = new TreeMap<String, MasterNode>();

    public MasterNode(String node_id) throws InvalidNodeIdException {
        super(node_id);
        _members.put(node_id, this);
    }

    /* GETTER AND SETTER METHODS */
    /**
     *
     * @return An ArrayList of <code>ClientNode</code>s that report to this node.
     */
    public ArrayList<ClientNode> getClientNodes() {
        return client_nodes;
    }


    /* STATIC METHODS */
    /**
     *
     * @param node_id The ID of the <code>MasterNode</code> to be searched.
     * @return The corresponding <code>MasterNode</code>, or <code>null</code>
     * if there is no such <code>MasterNode</code>.
     */
    public static MasterNode getMember(String node_id) {
        return _members.get(node_id);
    }
}
