package containers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Object held by the server that contains a list of all the groups and who is in them. THis is done so many clients dont need to be sent over the network and just the group ids can.
 * this is also useful because then the clients don't need to hold complicated information about the other clients such as IPs, which is also good from a security standpoint.
 * This object is used for storing both individual clients and groups of clients. this is done so the code for sending messages can be reused
 * TODO: Conciser refactoring to allow for 2 clients of the same name.
 */
public class GroupContainer {

    private class group {
        private ArrayList<Client> clients;
        private String password;

        public group() {
            clients = new ArrayList<>();
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public ArrayList<Client> getClients() {
            return clients;
        }

        public boolean addClient(Client client, String password) {
            if(this.password == null || this.password.equals(password)) {
                clients.add(client);
                return true;
            }
            return false;
        }

        public void setClients(ArrayList<Client> clients) {
            this.clients = clients;
        }
    }

    private final String allClientsIdentifier = "-1istheidentifier";
    Map<String, group> groups;

    public GroupContainer() {
        groups = new HashMap<>();
    }

    /**
     * Add a client to a group.
     * This method mutates the groups map.
     * @param groupId the id of the group to add a client too. This variable is changed within the method.
     * @param client the client that needs to be added
     * @return whether the client was added to the group.
     */
    public boolean addClientToGroup(String groupId, final Client client, final String password) {
        if(!groups.containsKey(groupId)) {
            groups.put(groupId,new group());
        }
        group group = groups.get(groupId);
        boolean added = group.addClient(client, password);
        if(added) {
            groups.put(groupId, group);
        }
        return added;
    }

    /**
     * get all the clients from a group
     * @param groupId the id of the group
     * @return A list of all clients in a group.
     */
    public ArrayList<Client> getClientsFromGroup(final String groupId) {
        if(groups.containsKey(groupId)) {
            return groups.get(groupId).getClients();
        }
        return null;
    }

    /**
     * check if group with group name exists
     * @param groupId the id of the group
     * @return the status of its existence
     */
    public boolean checkIfGroupExists(final String groupId) {
        return groups.containsKey(groupId);
    }

    /**
     * Add a client to server table.
     * This method mutates the groups map.
     * @param client the client that needs to be added
     */
    public void addClient(final Client client) {
        addClientToGroup(allClientsIdentifier,client,null);
    }

    /**
     * Get a list of all clients.
     * @return the list of clients.
     */
    public ArrayList<Client> getAllClients() {
        return groups.get(allClientsIdentifier).getClients();
    }

    /**
     * get all the clients from a group
     * @param clientName the name of the client to get
     * @return A list of all clients in a group.
     */
    public Client getClient(String clientName) {
        return groups.get(allClientsIdentifier).getClients().stream().filter(i-> i.getName() == clientName).findFirst().get();
    }

    /**
     * check if a client with a name exists
     * @param clientName the name of the client
     * @return the status of its existence
     */
    public boolean checkIfClientExists(String clientName) {
        return groups.get(allClientsIdentifier).getClients().stream().filter(i-> i.getName() == clientName).count() > 0;
    }

    /**
     * removes clients from all groups.
     * @param client
     */
    public void removeClient(final Client client) {
        groups.keySet().forEach(key -> getPut(client, key));
    }

    private void getPut(Client client, String key) {
        group group = groups.get(key);
        ArrayList<Client> allClients = group.getClients();
        allClients = allClients.stream().filter(i -> !i.getName().equals(client.getName())).collect(Collectors.toCollection(ArrayList::new));
        group.setClients(allClients);
        groups.put(key, group);
    }


}
