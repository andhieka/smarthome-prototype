package id.ac.usbi.smarthome.client;

import java.util.TreeMap;

/**
 * Client Node, or Slave Node, is a node that communicates wirelessly with a <code>MasterNode</code>.
 * A client node controls zero or more <code>ApplianceBase</code>.
 * Created by andhieka on 29/5/14.
 */
public class ClientNode extends NodeBase {
    private MasterNode _master = null;
    private static TreeMap<String, ClientNode> _members = new TreeMap<String, ClientNode>();

    public ClientNode(String node_id) throws InvalidNodeIdException {
        super(node_id);
        _members.put(node_id, this);
    }


    /* GETTER AND SETTER METHODS */
    public MasterNode getMaster() { return _master; }


    /* STATIC METHODS */
    /**
     * @param node_id The ID of the <code>ClientNode</code> to be searched.
     * @return The corresponding <code>ClientNode</code>, or <code>null</code>
     * if there is no such <code>ClientNode</code>.
     */
    public static ClientNode getMember(String node_id) {
        return _members.get(node_id);
    }
}
