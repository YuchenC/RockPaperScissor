package server.controller;

import server.model.RockPaperScissor;
import java.io.IOException;

import server.net.PlayerHandler;
/**
 *
 * @author yuchen
 */
public class Controller {
    private final RockPaperScissor game = new RockPaperScissor();
  
    public boolean preparePlay(PlayerHandler handler) {
        return game.preparePlay(handler);
    }
    
    public String getWinner(){
        return game.getWinner();
    }
}
