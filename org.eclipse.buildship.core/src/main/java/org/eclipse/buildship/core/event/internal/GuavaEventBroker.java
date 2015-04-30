package org.eclipse.buildship.core.event.internal;

import com.google.common.eventbus.EventBus;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.event.EventBroker;

public class GuavaEventBroker implements EventBroker {

    private EventBus eventBus = new EventBus("Buildship-Core");

    @Override
    public boolean post(String topic, Object data) {
        try {
            eventBus.post(data);
            return true;
        } catch (Exception e) {
            CorePlugin.logger().error("GuavaEventBroker was not able to complete posting the event", e);
        }

        return false;
    }

    @Override
    public boolean send(String topic, Object data) {
        return post(topic, data);
    }

    @Override
    public boolean subscribe(String topic, Object eventHandler) {
        eventBus.register(eventHandler);
        return true;
    }

    @Override
    public boolean unsubscribe(Object eventHandler) {
        eventBus.unregister(eventHandler);
        return true;
    }

}
