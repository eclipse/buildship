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

    public void processEvents() {
        while (true) {
            try {
                EclipseTestListener.EclipseTestEvent event = queue.poll(5, TimeUnit.MINUTES);
                if (event == null || event instanceof EclipseTestListener.TestRunFinishedEvent) {
                    break;
                } else {
                    testResultProcessor.onEvent(event);
                }
            } catch (InterruptedException e) {
                // retry
            }
        }
    }
}
