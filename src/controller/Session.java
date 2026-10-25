package controller;

import entity.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import view.*;

public class Session {
    /* ---- STATIC VARIABLES ---- */
    private static final int TICK_DURATION = 500;
    private static final int WINDOW_WIDTH = 1180;
    private static final int BATTLEFIELD_HEIGHT = 680;

    /* ---- PRIVATE VARIABLES ---- */
    private final Team teamA;
    private final Team teamB;

    private BattlefieldPanel panel;
    private Round round;
    private Timer timer;

    /* ---- CONSTRUCTORS ---- */
    public Session() {
        teamA = new Team("teamA");
        teamB = new Team("teamB");
    }

    /* ---- PUBLIC METHODS ---- */
    public void start() {
        startRound();
        setUpWindow();
        startTimer();
    }

    /* ---- PRIVATE METHODS ---- */
    /*
     * Configures the jframe that will be shown on screen
     */
    private void setUpWindow() {
        panel = new BattlefieldPanel();
        panel.setRound(round);
        JPanel battlefieldView = panel.createView();
        battlefieldView.setPreferredSize(new Dimension(WINDOW_WIDTH, BATTLEFIELD_HEIGHT));

        JFrame frame = new JFrame("The Williams - Battle Simulator");
        frame.add(battlefieldView, BorderLayout.CENTER);
        frame.add(new TroopPurchasePanel(round, panel), BorderLayout.SOUTH);
        frame.pack();
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        frame.setMinimumSize(new Dimension(Math.min(900, screen.width), Math.min(640, screen.height)));
        frame.setSize(Math.min(frame.getWidth(), screen.width), Math.min(frame.getHeight(), screen.height));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /*
     * Creates the round and allocates its board before placement begins.
     */
    private void startRound() {
        round = new Round(teamA, teamB);
        round.start();
        round.update(); // Allocate the board before showing the buying controls.
    }

    /*
     * Initiate the simulation timer that handle all the time related activities for
     * the simulation
     */
    private void startTimer() {
        timer = new Timer(TICK_DURATION, e -> {
            round.update();
            panel.refresh();
        });
        timer.start();
    }

}
