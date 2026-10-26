package controller;

import entity.*;
import entity.troops.melee.Infantry;
import entity.troops.melee.Cavalry;
import entity.troops.ranged.Archer;
import entity.troops.support.Priest;
import entity.troops.Troop;
import javax.swing.JFrame;
import javax.swing.Timer;
import view.*;

public class Session {
    /* ---- STATIC VARIABLES ---- */
    private static final int TICK_DURATION = 500;
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 500;

    /* ---- PRIVATE VARIABLES ---- */
    private final Team teamA;
    private final Team teamB;

    private BattlefieldPanel panel;
    private Round round;
    private Timer timer;

    // Survives between rounds so the log is not lost when a new Round starts.
    private final EventLogListener eventLog = new EventLogListener();

    /* ---- CONSTRUCTORS ---- */
    public Session() {
        teamA = new Team("teamA");
        teamB = new Team("teamB");
        setTemporaryTroops();
    }

    /* ---- PUBLIC METHODS ---- */
    public void start() {
        setUpWindow();
        startRound();
        startTimer();
    }

    /* ---- PRIVATE METHODS ---- */
    /*
     * Configures the jframe that will be shown on screen
     */
    private void setUpWindow() {
        panel = new BattlefieldPanel();

        JFrame frame = new JFrame("The Williams - Battle Simulator");
        frame.add(panel);
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /*
     * Creates a round instance, sets the round for the panel and starts a round
     */
    private void startRound() {
        round = new Round(teamA, teamB);

        // A second listener alongside the budget one. Round fires once and
        // both react, without either knowing the other is registered.
        round.addTileEffectListener(eventLog);

        panel.setRound(round);
        panel.setEventLog(eventLog);
        round.start();
    }

    /*
     * Initiate the simulation timer that handle all the time related activities for
     * the simulation
     */
    private void startTimer() {
        timer = new Timer(TICK_DURATION, e -> {
            round.update();
            panel.repaint();
        });
        timer.start();
    }

    /* ---- TEMPORARY METHOD ---- */
    private void setTemporaryTroops() {
    teamA.addTroop(new Infantry(1, 2, 10, Troop.Team.teamA));
    teamA.addTroop(new Archer(3, 2, 10, Troop.Team.teamA));
    teamA.addTroop(new Cavalry(5, 2, 10, Troop.Team.teamA));
    teamA.addTroop(new Priest(7, 2, 10, Troop.Team.teamA));

    teamB.addTroop(new Infantry(1, 13, 10, Troop.Team.teamB));
    teamB.addTroop(new Archer(3, 13, 10, Troop.Team.teamB));
    teamB.addTroop(new Cavalry(5, 13, 10, Troop.Team.teamB));
    teamB.addTroop(new Priest(7, 13, 10, Troop.Team.teamB));
}
}
