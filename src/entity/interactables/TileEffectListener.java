package entity.interactables;

// anything that wants to react when a tile effect fires implements this and
// registers with round, which is the subject.
// round does not know what a listener does, and listeners do not know about
// each other.

@FunctionalInterface
public interface TileEffectListener {
    void onTileEffectTriggered(TileEffectEvent event);
}
