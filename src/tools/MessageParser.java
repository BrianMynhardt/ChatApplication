package tools;

import driver.ClientDriver;
import containers.InternalCommandMessage;
import containers.Message;
import containers.Payload;
import containers.payload.ConnectionAcceptedMessage;
import containers.payload.CreateGroup;
import containers.payload.DisconnectFromServer;
import containers.payload.GroupMessage;
import containers.payload.JoinGroup;
import containers.payload.NameSetting;
import containers.payload.TextMessage;
import containers.payload.WhosHere;
import containers.payload.Wisper;
import exceptions.AlreadyJoinedException;
import exceptions.CharacterLimitExceeded;
import exceptions.EmptyTextException;

import java.util.List;

public class MessageParser {

  /**
   * parses text from the client to generate the Message object.
   * @param text the clients text
   * @return the message object
   */
  public static Message parseText(final String text) throws AlreadyJoinedException {
    dealWithMalformedMessage(text);
    Message message = new Message();
    if (text.charAt(0) == '/') {
      return commandMessage(text);
    }
    message.setPayload(new TextMessage(text));
    return message;
  }

  private static void dealWithMalformedMessage(final String text) {
    if(text == "") {
      //empty text
      throw new EmptyTextException();
    }
    if(text.length() > 10000) {
      //Character limit reached, don't read message.
      throw new CharacterLimitExceeded();
    }
  }

  private static Message commandMessage(final String text) throws AlreadyJoinedException {
    Message message = new Message();
    String lowerCaseText = text.toLowerCase();
    if (lowerCaseText.charAt(1) == 'w' && lowerCaseText.charAt(2) == ' ') {
      String[] splitted = text.split(" ");
      String nameOfSendTo = splitted[1];
      message.setReciever(nameOfSendTo);
      String theText = text.replaceFirst("/w" ,"");
      theText = theText.replaceFirst(nameOfSendTo ,"");
      theText = theText.trim();
      message.setPayload(new Wisper(theText));
    } else if (lowerCaseText.trim().equals("/whoshere")) {
      message.setPayload(new WhosHere());
    } else if (lowerCaseText.trim().equals("/dc")) {
      message.setPayload(new DisconnectFromServer());
    } else if (lowerCaseText.trim().substring(0, 5).equals("/join")) {
      String[] splitted = text.split(" ");
      String groupName = splitted[1];
      String password = splitted[2];
      if(!ClientDriver.groupsPartOf.contains(groupName)) {
          message.setPayload(new JoinGroup(password, groupName));
      }else{
          throw new AlreadyJoinedException();
      }
    } else if (lowerCaseText.trim().substring(0, 9).equals("/mygroups")) {
      return new InternalCommandMessage();
    } else if (lowerCaseText.trim().substring(0, 8).equals("/message")) {
      String[] splitted = text.split(" ");
      String groupToSendToo = splitted[1];
      String theText = text.replaceFirst("/message" ,"");
      theText = theText.replaceFirst(groupToSendToo,"");
      message.setReciever(groupToSendToo);
      message.setPayload(new GroupMessage(theText, groupToSendToo));
    } else if (lowerCaseText.trim().substring(0, 12).equals("/creategroup")) {
      String[] splitted = text.split(" ");
      String groupName = splitted[1];
      String password = splitted[2];
      message.setPayload(new CreateGroup(password, groupName));
    }
    return message;
  }

  /**
   * parses a message and turns it into a human readable message for the client.
   * @param message the message received over the network
   * @return the human readable string
   */
  public static String parseMessage(final Message message) {
    final String name = message.getSender().getName();
    final Payload payload = message.getPayload();
    if (payload.getClass().equals(TextMessage.class)) {
      return textMessageReceived(name, ((TextMessage) payload).getMessage());
    }
    if (payload.getClass().equals(GroupMessage.class)) {
      final String groupName = ((GroupMessage) payload).getGroupName();
      return groupMessageReceived(name, groupName, ((GroupMessage) payload).getMessage());
    }
    if (payload.getClass().equals(Wisper.class)) {
      return whisperMessageReceived(name, ((TextMessage) payload).getMessage());
    }
    if (payload.getClass().equals(NameSetting.class)) {
      return MessageParser.personJoinedServer(((NameSetting) payload).getName());
    }
    if (payload.getClass().equals(WhosHere.class)) {
      return whosHere(((WhosHere) payload).getClients());
    }
    if (payload.getClass().equals(ConnectionAcceptedMessage.class)) {
      return "-------------------------";
    }
    if (payload.getClass().equals(DisconnectFromServer.class)) {
      System.exit(0);
    }
    if (payload.getClass().equals(CreateGroup.class)) {
      String groupName = ((CreateGroup) payload).getGroupName();
      ClientDriver.groupsPartOf.add(groupName);
      return String.format("Created and joined group %s", groupName);
    }
    if (payload.getClass().equals(JoinGroup.class)) {
      String groupName = ((JoinGroup) payload).getGroupName();
      ClientDriver.groupsPartOf.add(groupName);
      return String.format("Joined group %s", groupName);
    }
    return payload.getClass().toString();
  }

  /**
   * method that returns a human readable string of all the groups the client is part of.
   * @return string showing all formatted strings.
   */
  public static String groupsIAmPartOf() {
    String output = "Groups Currently part of \n ------------------------- \n";
    StringBuffer buffer = new StringBuffer(output);
    ClientDriver.groupsPartOf.stream().forEach(i -> buffer.append(i + "\n"));
    buffer.append("-------------------------");
    return buffer.toString();
  }

  private static String whosHere(final List<String> clients) {
    String output = "People currently connected \n ------------------------- \n";
    StringBuffer buffer = new StringBuffer(output);
    clients.stream().forEach(i -> buffer.append(i + "\n"));
    buffer.append("-------------------------");
    return buffer.toString();
  }

  private static String personJoinedServer(final String name) {
    return String.format("%s has joined the server", name);
  }

  private static String textMessageReceived(final String personName, final String text) {
    return String.format("%s: %s", personName, text);
  }

  private static String whisperMessageReceived(final String personName, final String text) {
    return String.format("%s whispers: %s", personName, text);
  }

  private static String groupMessageReceived(
      final String personName,
      final String group,
      final String text) {
    return String.format("%s-%s: %s", group, personName, text);
  }


}
