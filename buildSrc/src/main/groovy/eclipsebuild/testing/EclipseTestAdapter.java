package eclipsebuild.testing;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class EclipseTestAdapter {

    private final BlockingQueue<EclipseTestEvent> queue;
    private final EclipseTestResultProcessor testResultProcessor;

    public EclipseTestAdapter(EclipseTestListener testListener, EclipseTestResultProcessor testResultProcessor) {
        this.queue = testListener.getQueue();
        this.testResultProcessor = testResultProcessor;
    }

    public boolean processEvents() {
        boolean success = true;
        while (true) {
            try {
                EclipseTestEvent event = queue.poll(5, TimeUnit.MINUTES);
                if (event instanceof EclipseTestEvent.TestFailed) {
                    success = false;
                }
                if (event == null || event instanceof EclipseTestEvent.TestRunEnded) {
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
