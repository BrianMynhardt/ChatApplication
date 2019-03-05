import containers.Message;
import containers.payload.TextMessage;
import org.junit.Assert;
import org.junit.Test;
import tools.MessageParser;

import java.util.Arrays;
import java.util.List;

public class MessageParserTest {

   @Test
   public void parseTextMessage() {
      String textToTest = "Hello";
      Message createdMessage = MessageParser.parseText(textToTest);
      Message message = new Message();
      message.setReciever();
      message.setPayload(new TextMessage(textToTest));
      Assert.assertEquals(createdMessage.getPayload().getClass(), TextMessage.class);
      Assert.assertEquals(((TextMessage)createdMessage.getPayload()).getMessage(),textToTest);
   }

   @Test
   public void parseWisperMessage() {
      String textToTest = "/w bob Hello";
      Message createdMessage = MessageParser.parseText(textToTest);
      Message message = new Message();
      message.setReciever("bob");
      message.setPayload(new TextMessage("Hello"));
      Assert.assertEquals(createdMessage.getPayload().getClass(), TextMessage.class);
      Assert.assertEquals(((TextMessage)createdMessage.getPayload()).getMessage(),textToTest);
   }

}