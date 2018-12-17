package server.net;

import common.MessageException;
import common.MessageSplitter;
import java.io.IOException;
import java.util.StringJoiner;

import common.MsgType;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import server.controller.Controller;


/**
 *
 * @author yuchen
 */
public class PlayerHandler {
    private final Server server;
    private final SocketChannel playerChannel;
    private final Controller contr;
    
    private String username = "anonymous";
    private String execution;
    private String winner;
    private final MessageSplitter msgSplitter = new MessageSplitter();
    private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(2018);
    
//    PlayerHandler(Server server, SocketChannel playerChannel, Controller contr) {
//        this.server = server;
//        this.playerChannel = playerChannel;
//        this.contr = contr;
//    }
    
    PlayerHandler(Server server, SocketChannel playerChannel, Controller contr) {
        this.server = server;
        this.playerChannel = playerChannel;
        this.contr = contr;
    }
    
    public void handlePlayerRequest() {
        while (msgSplitter.hasNext()) {
            Message msg = new Message(msgSplitter.nextMsg());
            switch(msg.msgType) {
                case USER:
                    username = msg.msgBody;
                    System.out.println("username = " + username);
                    StringJoiner joinerUser = new StringJoiner("##");
                    joinerUser.add(MsgType.USER.toString());
                    joinerUser.add(username);
                    server.broadcast(joinerUser.toString());
                    break;

                case PLAY:
                    execution = msg.msgBody;
                    System.out.println(username + " " + execution);
                    boolean ready = contr.preparePlay(this);
                    StringJoiner joinerPlay = new StringJoiner("##");
                    if (ready)
                    {
                        winner = contr.getWinner();
                        System.out.println("winner = " + winner);
                        joinerPlay.add(MsgType.PLAY.toString());
                        joinerPlay.add("true");
                        joinerPlay.add(winner);
                        server.broadcast(joinerPlay.toString());
                    }
                    if (!ready){
                        joinerPlay.add(MsgType.PLAY.toString());
                        joinerPlay.add("false");
                        server.broadcast(joinerPlay.toString());
                    }
                    break;

                case DISCONNECT:
                    System.out.println(msg);
                    try {
                    disconnectClient();
                    } catch(IOException ioe) {};
                    StringJoiner joinerQuit = new StringJoiner("##");
                    joinerQuit.add(MsgType.DISCONNECT.toString());
                    joinerQuit.add(username);
                    server.broadcast(joinerQuit.toString());
                    break; 

                default:
                    System.out.println("Command:" + msg.receivedString + "is not known.");
            }
        }
    }

    void sendMsg(ByteBuffer msg) throws IOException {
        System.out.println("sendMsg " + msg);
        playerChannel.write(msg);
        if (msg.hasRemaining()) {
            throw new MessageException("Could not send message");
        }
    }
    
    void disconnectClient() throws IOException {
        playerChannel.close();
    }
    
    void recvMsg() throws IOException {
        msgFromClient.clear();
        int numOfReadBytes;
        numOfReadBytes = playerChannel.read(msgFromClient);
        if (numOfReadBytes == -1) {
            throw new IOException("Client has closed connection.");
        }
        String recvdString = extractMessageFromBuffer();
        msgSplitter.appendRecvdString(recvdString);
        handlePlayerRequest();
    }
    
    private String extractMessageFromBuffer() {
        msgFromClient.flip();
        byte[] bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        return new String(bytes);
    }    
    
    private static class Message {
        private String receivedString;
        private MsgType msgType;
        private String msgBody;
        
        private Message (String receivedString) {
            parse(receivedString);
            this.receivedString = receivedString;
        }
        
        private void parse (String strToParse) {
            try {
                String[] msgTokens = strToParse.split("##");
                msgType = MsgType.valueOf(msgTokens[0].toUpperCase());
                if (hasBody(msgTokens)) {
                    msgBody = msgTokens[1];
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        
        private boolean hasBody(String[] msgTokens) {
            return msgTokens.length > 1;
        }
    }
    
    public String getUsername(){
        return username;
    }
    
    public String getExecution(){
        return execution;
    }
}
