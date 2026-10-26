package entity.interactables;

/*
 * Observer. Anything that wants to react when a tile effect fires implements
 * this and registers with Round, which is the subject.
 *
 * Round does not know what any listener does with the event, and no listener
 * knows about any other. Adding a new reaction means adding a class and one
 * registration line, with no change to Round or to the effects themselves.
 */
@FunctionalInterface
public interface TileEffectListener {
    void onTileEffectTriggered(TileEffectEvent event);
}
