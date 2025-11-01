package net.avicus.compendium.countdown;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CountdownEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final Countdown ended;

    public CountdownEndEvent(Countdown ended) {
        this.ended = ended;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
