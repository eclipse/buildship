package eclipsebuild.testing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class EclipseTestAdapter {

    private final BlockingQueue<EclipseTestListener.EclipseTestEvent> queue;
    private final EclipseTestResultProcessor testResultProcessor;

    public EclipseTestAdapter(EclipseTestListener testListener, EclipseTestResultProcessor testResultProcessor) {
        this.queue = testListener.getQueue();
        this.testResultProcessor = testResultProcessor;
    }

    public boolean processEvents() {
        boolean success = true;
        while (true) {
            try {
                EclipseTestListener.EclipseTestEvent event = queue.poll(1, TimeUnit.MINUTES);
                if (event instanceof EclipseTestListener.TestFailedEvent) {
                    success = false;
                }
                if (event == null || event instanceof EclipseTestListener.TestRunEndedEvent) {
                    break;
                } else {
                    testResultProcessor.onEvent(event);
                }
            } catch (InterruptedException e) {
                // retry
            }
        }
         return success;
    }
}
