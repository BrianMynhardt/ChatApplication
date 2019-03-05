package driver;

import containers.Client;
import containers.GroupContainer;
import containers.Message;
import containers.Payload;
import containers.payload.ConnectionAcceptedMessage;
import containers.payload.CreateGroup;
import containers.payload.DisconnectFromServer;
import containers.payload.JoinGroup;
import containers.payload.NameSetting;
import containers.payload.WhosHere;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServerDriver {

  public GroupContainer knownGroups;

  private ServerSocket serverSocket;

  private final PrintStream printStream;

  private final Client server = new Client("server");

  /**
   * prints the details about the server to the printStream.
   */
  public void printServerSocket() {
    printStream.printf("Server Ip is %s and port is %s \n",
        serverSocket.getInetAddress().getHostAddress(),
        serverSocket.getLocalPort());
  }

  /**
   * The constructor for the class that handles server creation
   * looping as well as handling all its threads.
   *
   * @param printStream where printing to the user should go.
   */
  public ServerDriver(final PrintStream printStream) {
    knownGroups = new GroupContainer();
    this.printStream = printStream;
    this.serverSocket = startServer(1234);
  }

  /**
   * add client to list of known clients.
   *
   * @param client the client to add to the list
   */
  public void addClient(final Client client) {
    knownGroups.addClient(client);
  }

  public boolean knowOfClient(Client client) {
    return knownGroups.checkIfClientExists(client.getName());
  }

  public Client getClientWithName(String name) {
    return knownGroups.getClient(name);
  }

  public void pingAllClients() {
    //pings all clients to make sure they still are online.
  }

  ServerSocket startServer(int portNumber) {
    try {
      serverSocket = new ServerSocket(1234);
      return serverSocket;
    } catch (IOException e) {
      if (portNumber != 8000) {
        return startServer(8000);
      }
      e.printStackTrace();
      throw new RuntimeException("port in use");
    }
  }

  /**
   * the loop that keeps keeps the main thread going.
   * blocking and waiting for new clients at each point.
   */
  public void serverLoop() {
    printServerSocket();
    while (true) {
      printStream.println("Looping, checking for connections");
      checkForNewConnection();
    }
  }

  /**
   * This loops checks for when a new client connects to the server.
   */
  public void checkForNewConnection() {
    try {
      Socket socket = serverSocket.accept();
      newChatClient(socket
      );
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * thread that starts up for each client.
   *
   * @param socket the socket of the new connection
   */
  public void newChatClient(Socket socket) {
    new Thread(new Runnable() {
      public void run() {
        try {
          Client client = new Client();
          Message message = new Message();
          message.setSender(server);
          message.setPayload(new ConnectionAcceptedMessage());
          ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
          ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
          client.setOutputStream(objectOutputStream);
          client.setInputStream(objectInputStream);
          objectOutputStream.writeObject(message);
          NameSetting name = (NameSetting) getMessage(objectInputStream).getPayload();
          client.setName(name.getName());
          knownGroups.addClient(client);
          java.lang.System.out.printf("started thread for new client %s\n", client.getName());
          message.setSender(client);
          message.setReciever();
          message.setPayload(new NameSetting(client.getName()));
          for (Client connectedClients : knownGroups.getAllClients()) {
            connectedClients.getOutputStream().writeObject(message);
          }
          while (true) {
            message = getMessage(client.getInputStream());
            if (message.getPayload().getClass().equals(WhosHere.class)) {
              whosHereCommand(message);
            } else if (message.getPayload().getClass().equals(DisconnectFromServer.class)) {
              disconnectFromServerCommand(message);
            } else if (message.getPayload().getClass().equals(CreateGroup.class)) {
              createGroupCommand(client, message);
            } else if (message.getPayload().getClass().equals(JoinGroup.class)) {
              joinGroupCommand(client, message);
            } else if (whoToSendToo(message)) {
              printStream.println("sending to all");
              for (Client connectedClients : allExceptSelf(message)) {
                printStream.println("sending to " + connectedClients.getName());
                connectedClients.getOutputStream().writeObject(message);
              }
            } else {
              printStream.println("sending to some");
              for (Client clientToSendToo : getClients(message)) {
                printStream.println("sending to " + clientToSendToo.getName());
                clientToSendToo.getOutputStream().writeObject(message);
              }
            }
          }
        } catch (IOException e) {
          printStream.println("client disconnected");

        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }

      private boolean whoToSendToo(Message message) {
        return message.getRecievers() == null || message.getRecievers().getGroupName() == null;
      }

      private void createGroupCommand(Client client, Message message) throws IOException {
        String groupName = ((CreateGroup) message.getPayload()).getGroupName();
        String password = ((CreateGroup) message.getPayload()).getPassword();
        knownGroups.addClientToGroup(groupName, client, password);
        printStream.println("created group " + groupName);
        message.setReciever(message.getSender().getName());
        message.setPayload(new CreateGroup(groupName));
        sendMessage(message);
      }

      private void joinGroupCommand(Client client, Message message) throws IOException {
        String groupName = ((JoinGroup) message.getPayload()).getGroupName();
        String password = ((JoinGroup) message.getPayload()).getPassword();
        boolean joined = knownGroups.addClientToGroup(groupName, client, password);
        if (joined) {
          printStream.printf("Client %s joined group %s", client.getName(), groupName);
          message.setReciever(message.getSender().getName());
          message.setPayload(new JoinGroup(groupName));
          sendMessage(message);
        } else {
          printStream.printf("Client has incorrect password for group");
        }
      }

      private void disconnectFromServerCommand(Message message) throws IOException {
        message.setReciever(message.getSender().getName());
        Client clientToDc = getClientWithName(message.getSender().getName());
        knownGroups.removeClient(getClientWithName(message.getSender().getName()));
        clientToDc.getOutputStream().writeObject(message);
        printStream.println("disconnecting " + clientToDc.getName());
      }

      private void whosHereCommand(Message message) throws IOException {
        message.setReciever(message.getSender().getName());
        List<String> clients = convertClientListIntoNameList();
        Payload whosHerePayload = new WhosHere(clients);
        message.setPayload(whosHerePayload);
        sendMessage(message);
      }

      private List<String> convertClientListIntoNameList() {
        return knownGroups.getAllClients().stream()
            .map(Client::getName)
            .collect(Collectors.toList());
      }

      private void sendMessage(Message message) throws IOException {
        getClientWithName(message.getSender().getName()).getOutputStream().writeObject(message);
      }

      private ArrayList<Client> allExceptSelf(Message message) {
        ArrayList<Client> group;
        if (whoToSendToo(message)) {
          group = knownGroups.getAllClients();
        } else {
          group = getClients(message);
        }
        return group.stream()
            .filter(notMe(message))
            .collect(Collectors.toCollection(ArrayList::new));
      }
    }).start();
  }

  /**
   * Get a list of clients that the message is intended to be sent too.
   * the group can contain 1 -> many clients. 1 for a pm and many for a group message.
   *
   * @param message the message being sent to the groups.
   * @return the people contained within the group
   */
  private ArrayList<Client> getClients(Message message) {
    String groupName = message.getRecievers().getGroupName();
    ArrayList<Client> clients = knownGroups.getClientsFromGroup(groupName).stream()
            .filter(notMe(message))
            .collect(Collectors.toCollection(ArrayList::new));
    return (clients == null) ? knownGroups.getAllClients().stream()
        .filter(i -> groupName.equals(i.getName()))
        .collect(Collectors.toCollection(ArrayList::new))
        : clients;
  }

  private Predicate<Client> notMe(Message message) {
    return i -> !i.getName().equals(message.getSender().getName());
  }

  private Message getMessage(
      final ObjectInputStream objectInputStream)
      throws IOException, ClassNotFoundException {
    return (Message) objectInputStream.readObject();
  }
}
