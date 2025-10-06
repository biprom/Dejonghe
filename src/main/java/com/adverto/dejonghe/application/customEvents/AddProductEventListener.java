package com.adverto.dejonghe.application.customEvents;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class AddProductEventListener {

    private Consumer<AddRemoveProductEvent> eventConsumer;

    public void setEventConsumer(Consumer<AddRemoveProductEvent> consumer) {
        this.eventConsumer = consumer;
    }

    @EventListener
    public void handleMyCustomEvent(AddRemoveProductEvent event) {
        if (eventConsumer != null) {
            eventConsumer.accept(event);
        }
    }
}
