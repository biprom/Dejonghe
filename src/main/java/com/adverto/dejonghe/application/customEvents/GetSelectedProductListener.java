package com.adverto.dejonghe.application.customEvents;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class GetSelectedProductListener {

    private Consumer<GetSelectedProductEvent> eventConsumer;

    public void setEventConsumer(Consumer<GetSelectedProductEvent> consumer) {
        this.eventConsumer = consumer;
    }

    @EventListener
    public void handleMyCustomEvent(GetSelectedProductEvent event) {
        if (eventConsumer != null) {
            eventConsumer.accept(event);
        }
    }
}
