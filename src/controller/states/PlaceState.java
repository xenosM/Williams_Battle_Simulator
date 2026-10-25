package controller.states;

import controller.Round;

public class PlaceState implements GameState {
    @Override
    public void update(Round round) {
        // Buying and placement continue until the player chooses Start Battle.
    }

    @Override
    public void render(Round round) {
        // draw placement-phase UI
    }
}