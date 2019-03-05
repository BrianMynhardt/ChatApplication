package driver;

import containers.Client;
import containers.Group;
import containers.InternalCommandMessage;
import containers.Message;
import containers.payload.ConnectionAcceptedMessage;
import containers.payload.NameSetting;
import exceptions.AlreadyJoinedException;
import exceptions.CharacterLimitExceeded;
import tools.MessageParser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class ClientDriver {

  public static List<String> groupsPartOf = new ArrayList<>();

  PrintStream outputStream;
  Scanner scanner;

  Date date;

  private Socket socket;

  private ObjectInputStream objectInputStream;
  private ObjectOutputStream objectOutputStream;

  Client me;

  public ClientDriver(PrintStream printStream, Scanner scanner) {
    this.outputStream = printStream;
    this.scanner = scanner;
    date = new Date();
  }

  /**
   * Creates the client.
   * @return the created client
   */
  public Client createMe() {
    outputStream.println("What is your name?");
    String name = scanner.nextLine();
    return new Client(name);
  }

  /**
   * Method used to connect to the server.
   * @param ip the IP address of the server.
   * @param me who I am
   * @return whether it connected
   * @throws IOException IO exception probably server not being on
   * @throws ClassNotFoundException When the message class can't be created.
   */
  public boolean connectToServer(
      final String ip,
      final Client me
  ) throws IOException, ClassNotFoundException {
    //hardcoded localhost ip for testing and etc
    socket = new Socket(ip, 1234);
    objectInputStream = new ObjectInputStream(socket.getInputStream());
    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
    Message message = (Message) objectInputStream.readObject();
    if (message.getPayload().getClass().equals(ConnectionAcceptedMessage.class)) {
      setName(me.getName());
      return true;
    }
    return false;
  }

  /**
   * Loop that is run by the client to make sure everything runs well.
   * @param ip the ip of the server
   * @param me who I am
   * @throws IOException the IO exception when the output stream is undefined (probably)
   * @throws ClassNotFoundException cant find message app
   */
  public void clientLoop(final String ip, final Client me) throws IOException, ClassNotFoundException {
    if (!connectToServer(ip, me)) {
      outputStream.println("connection refused by server");
    }
    this.me = me;
    outputStream.println("connection accepted to server");
    readMessages();
    writeMessages();
  }

  /**
   * thread to read messages being sent from the server.
   */
  public void readMessages() {
    new Thread(new Runnable() {
      public void run() {
        while (true) {
          try {
            Message message = (Message) objectInputStream.readObject();
            outputStream.println(MessageParser.parseMessage(message));
          } catch (IOException e) {
            outputStream.printf("can not connect to server. please restart");
            java.lang.System.exit(0);
          } catch (ClassNotFoundException e) {
            e.printStackTrace();
          }
        }
      }
    }).start();
  }

  /**
   * method to read messages from the input stream.
   */
  public void writeMessages() {
    new Thread(new Runnable() {
      public void run() {
        while (true) {
            if (scanner.hasNextLine()) {
              String typedMessage = scanner.nextLine();
              Date currentDate = new Date();
              long dif = currentDate.getTime() - date.getTime();
              if (dif > 500) {
                try {
                  Message message = MessageParser.parseText(typedMessage);
                  if (message.getClass().equals(InternalCommandMessage.class)) {
                    outputStream.println(MessageParser.groupsIAmPartOf());
                  }
                  date = new Date();
                  message.setSender(me);
                  objectOutputStream.writeObject(message);
                } catch (IOException e) {
                  e.printStackTrace();
                } catch (CharacterLimitExceeded e) {
                  outputStream.printf(e.getLocalizedMessage());
                } catch (AlreadyJoinedException e) {
                  outputStream.println(e.getMessage());
                }
            } else {
                outputStream.printf("You can not send messages so rapidly.");
              }
          }
        }
      }
    }).start();
  }

  /**
   * set my name.
   * @param name my name
   * @throws IOException if the objectOutputStream is null or closed.
   */
  public void setName(String name) throws IOException {
    Message message = new Message();
    message.setSender(me);
    message.setReciever(new Group());
    message.setPayload(new NameSetting(name));
    objectOutputStream.writeObject(message);
  }


}
