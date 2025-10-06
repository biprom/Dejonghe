package com.adverto.dejonghe.application.customEvents;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class GetSelectedWorkorderListener {

    private Consumer<GetSelectedWorkOrderEvent> eventConsumer;

    public void setEventConsumer(Consumer<GetSelectedWorkOrderEvent> consumer) {
        this.eventConsumer = consumer;
    }

    @EventListener
    public void handleMyCustomEvent(GetSelectedWorkOrderEvent event) {
        if (eventConsumer != null) {
            eventConsumer.accept(event);
        }
    }
}
