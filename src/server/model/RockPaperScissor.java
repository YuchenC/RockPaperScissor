package server.model;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import server.net.PlayerHandler;

/**
 *
 * @author yuchen
 */
public class RockPaperScissor {
    private final List<String> entries = Collections.synchronizedList(new ArrayList<>());
    private PlayerHandler handler1;
    private PlayerHandler handler2;
    private String execution1;
    private String execution2;
    private boolean firstUser = true;
    private String winner;
    
    // set handler1 and handler2 based on the boolean firstUser,
    // if the user comes before its opponent, it will be set to handler1.
    // if the user comes after its opponent, it will be set to handler2
    // the return value indicates if the game is ready, 
    //i.e if the user needs to wait for its opponent
    public boolean preparePlay(PlayerHandler handler){
        if (firstUser)
        {
            handler1 = handler;
            System.out.println(handler1.getUsername() + " in preparePlay");
            firstUser = false;
            return false;
        } else {
            handler2 = handler;
            firstUser = true;
            System.out.println(handler2.getUsername() + " in preparePlay, will call playGame");
            playGame(handler1, handler2);
            return true;
        }      
    }
    
    // set both users' execution and call method getWinner()
    private void playGame(PlayerHandler handler1, PlayerHandler handler2) {
        System.out.println("in method playGame");
        execution1 = handler1.getExecution();
        execution2 = handler2.getExecution();
        decideWinner();
    }
    
    // return winner's username
    // if both user are anonymous this could be quite tricky??
    private void decideWinner (){
        System.out.println("in getWinner");
        if (execution1.equals(execution2))
            winner =  "tie";
        else if (execution1.equals("rock")){
            if (execution1.equals("paper"))
                winner =  handler1.getUsername();
            else
                winner = handler2.getUsername();
        } else if (execution1.equals("paper")) {
            if (execution2.equals("rock"))
                winner = handler2.getUsername();
            else
                winner = handler1.getUsername();
        } else {
            if (execution2.equals("rock"))
                winner = handler2.getUsername();
            else
                winner = handler1.getUsername();
        }
    }
    
    public String getWinner(){
        return winner;
    }
}
