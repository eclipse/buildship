package org.eclipse.buildship.core.internal.test.fixtures

import org.eclipse.buildship.core.internal.console.ProcessDescription
import org.eclipse.buildship.core.internal.console.ProcessStreams
import org.eclipse.buildship.core.internal.console.ProcessStreamsProvider

abstract class TestProcessStreamProvider implements ProcessStreamsProvider {

    TestProcessStream backroundStream = new TestProcessStream()
    List<TestProcessStream> processStreams = []

    @Override
    public ProcessStreams getBackgroundJobProcessStreams() {
        backroundStream
    }

    @Override
    public ProcessStreams createProcessStreams(ProcessDescription processDescription) {
        ProcessStreams result = new TestProcessStream()
        processStreams += result
        result
    }

    static class TestProcessStream implements ProcessStreams {

        OutputStream confing = new ByteArrayOutputStream()
        OutputStream output = new ByteArrayOutputStream()
        OutputStream error = new ByteArrayOutputStream()
        InputStream input = new ByteArrayInputStream(new byte[0])

        @Override
        public OutputStream getConfiguration() {
            confing
        }

        @Override
        public OutputStream getOutput() {
           output
        }

        @Override
        public OutputStream getError() {
            error
        }

        @Override
        public InputStream getInput() {
            input
        }

        @Override
        public void close() {
        }

        String getConf() {
            confing.toString()
        }

        String getOut() {
            output.toString()
        }

        String getErr() {
            error.toString()
        }
    }
}
