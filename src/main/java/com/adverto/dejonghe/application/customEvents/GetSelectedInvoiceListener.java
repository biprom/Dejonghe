package com.adverto.dejonghe.application.customEvents;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class GetSelectedInvoiceListener {

    private Consumer<GetSelectedInvoiceEvent> eventConsumer;

    public void setEventConsumer(Consumer<GetSelectedInvoiceEvent> consumer) {
        this.eventConsumer = consumer;
    }

    @EventListener
    public void handleMyCustomEvent(GetSelectedInvoiceEvent event) {
        if (eventConsumer != null) {
            eventConsumer.accept(event);
        }
    }
}
