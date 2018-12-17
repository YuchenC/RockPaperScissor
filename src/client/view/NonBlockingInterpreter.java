package client.view;

import java.net.InetSocketAddress;
import client.net.ServerConnection;
import java.util.Scanner;
import client.net.CommunicationListener;

/**
 * Reads and interprets user commands. The command interpreter will run in a separate thread, which
 * is started by calling the <code>start</code> method. Commands are executed in a thread pool, a
 * new prompt will be displayed as soon as a command is submitted to the pool, without waiting for
 * command execution to complete.
 */
public class NonBlockingInterpreter implements Runnable {
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private boolean receivingCmds = false;
    private ServerConnection server;
    private final ThreadSafeStdOut outMgr = new ThreadSafeStdOut();

    /**
     * Starts the interpreter. The interpreter will be waiting for user input when this method
     * returns. Calling <code>start</code> on an interpreter that is already started has no effect.
     */
    public void start() {
        if (receivingCmds) {
            return;
        }
        receivingCmds = true;
        server = new ServerConnection();
        new Thread(this).start();
    }

    /**
     * Interprets and performs user commands.
     */
    @Override
    public void run() {
        while (receivingCmds) {
            try {
                CmdLine cmdLine = new CmdLine(readNextLine());
                switch (cmdLine.getCmd()) {
                    case QUIT:
                        receivingCmds = false;
                        server.disconnect();
                        break;
                    case CONNECT:
                        server.addCommunicationListener(new ConsoleOutput());
                        server.connect(cmdLine.getParameter(0),
                                      Integer.parseInt(cmdLine.getParameter(1)));
                        break;
                    case USER:
                        server.sendUsername(cmdLine.getParameter(0));
                        break;
                    case PLAY:
                        server.sendGuess(cmdLine.getParameter(0));
                        break;
                    default:
                        server.sendMsg(cmdLine.getUserInput());
                }
            } catch (Exception e) {
                outMgr.println("Operation failed");
            }
        }
    }

    private String readNextLine() {
        outMgr.print(PROMPT);
        return console.nextLine();
    }

    private class ConsoleOutput implements CommunicationListener {
        @Override 
        public void recvdMsg(String msg){
            printToConsole(msg);
        }
        
        @Override
        public void connected(InetSocketAddress serverAddress){
            printToConsoleConnect("Connected to " + serverAddress.getHostName() + ":" + serverAddress.getPort());
        }
        
        @Override
        public void disconnected(){
            printToConsoleConnect("Disconnected from server.");
        }
        
        private void printToConsole(String msg) {
            String[] info = msg.split("##");
            switch(info[0]){
                case "USER":
                    outMgr.println("Welcome, " + info[1] + "!");
                    break;
                case "PLAY":
                    if (info[1].equals("false"))
                        outMgr.println("Waiting...");
                    else
                        outMgr.println("The winner is " + info[2]);
                    break;
                case "DISCONNECT":
                    outMgr.println("You are diconnected.");
                    break;
            }
            outMgr.print(PROMPT);       
        }
        
        private void printToConsoleConnect(String msg) {
           outMgr.println(msg);
           outMgr.print(PROMPT);
        }
    }
}
