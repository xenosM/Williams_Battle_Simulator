package controller;

import entity.interactables.TileEffectEvent;
import entity.interactables.TileEffectListener;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/*
 * Keeps the last few things that happened on the battlefield so the view can
 * show them.
 *
 * The point of having a second listener is that it reacts to the same events
 * as BudgetListener without either one knowing the other exists. Round fires
 * once; how many things care is not its problem.
 */
public class EventLogListener implements TileEffectListener {

    /* ---- STATIC VARIABLES ---- */
    private static final int MAX_ENTRIES = 6;

    /* ---- PRIVATE VARIABLES ---- */
    private final Deque<String> entries = new ArrayDeque<>();

    /* ---- PUBLIC METHODS ---- */
    @Override
    public void onTileEffectTriggered(TileEffectEvent event) {
        entries.addFirst(event.description());

        while (entries.size() > MAX_ENTRIES) {
            entries.removeLast();
        }
    }

    /* ---- GETTER METHODS ---- */
    // Most recent first.
    public List<String> getEntries() {
        return new ArrayList<>(entries);
    }

    public String getLatest() {
        return entries.isEmpty() ? "" : entries.getFirst();
    }
}
