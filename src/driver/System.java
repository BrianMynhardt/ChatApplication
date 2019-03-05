package driver;

import containers.Client;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class System {

  PrintStream outputStream;
  InputStream inputStream;

  Scanner scanner;
  ClientDriver clientDriver;
  ServerDriver serverTools;
  Client me;
  boolean isServer;

  /**
   * System constructor to initialize basic variables and start up program.
   * @param outputStream where output should stream to.
   * @param inputStream where input should come from
   * @throws IOException can through and io exception for read or write issues.
   * @throws ClassNotFoundException if a message class cant be found.
   */
  public System(
      final PrintStream outputStream,
      final InputStream inputStream)
      throws IOException, ClassNotFoundException {
    this.outputStream = outputStream;
    this.inputStream = inputStream;
    this.scanner = new Scanner(inputStream);
    this.isServer = false;
    start();
  }


  /**
   * Asks if the person is the server or not.
   * if they are the server returns the IP with their IP else returns the Ip with the server Ip.
   *
   * @return the server IP
   * TODO: DO some DNS broadcasting to detect if there is a server already running.
   */
  public String getServerIp(){
    outputStream.println("What is the servers IP? If you are the server leave this blank");
    String ip = "";
    String readIp = scanner.nextLine();
    if (readIp.equals("")) {
      outputStream.println("IP Blank, you are server");
      this.isServer = true;
    } else {
      ip = readIp;
    }
    outputStream.printf("the server ip address is %s\n", ip);
    return ip;
  }

  /**
   * starts the system up.
   *
   * @throws IOException exception with one of the streams
   * @throws ClassNotFoundException class cant be found.
   */
  public void start() throws IOException, ClassNotFoundException {
    String serverIP = getServerIp();
    if (isServer) {
      serverTools = new ServerDriver(outputStream);
      // you are the server
      serverTools.serverLoop();
    } else {
      //you are client
      this.clientDriver = new ClientDriver(outputStream, scanner);
      me = clientDriver.createMe();
      clientDriver.clientLoop(serverIP, me);
    }
  }


  /**
   * starts up the service with parameters.
   *
   * @param args unused input args.
   * @throws IOException exception with one of the streams.
   * @throws ClassNotFoundException class cant be found/
   */
  public static void main(String[] args) throws IOException, ClassNotFoundException {
    //OutputStream out =
    new System(java.lang.System.out, java.lang.System.in);


  }


}
