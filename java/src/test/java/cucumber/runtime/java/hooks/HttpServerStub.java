package cucumber.runtime.java.hooks;

public class HttpServerStub {
    private boolean isStarted;
    private Request<String> request;

    public void start() {
        if (isStarted) {
            throw new IllegalStateException("Server already started!");
        }
        try {
            Thread.sleep(2000);
            isStarted = true;
        } catch (InterruptedException e) {}
    }

    public void stop() {
        if (!isStarted) {
            throw new IllegalStateException("Server is not running!");
        }
        try {
            Thread.sleep(2000);
            isStarted = false;
        } catch (InterruptedException e) {}
    }

    public boolean isStarted() {
        return isStarted;
    }

    public int send(Request<String> request) {
        if (!isStarted) {
            throw new IllegalStateException("server is not running");
        }
        this.request = request;
        return 200;
    }

    public String receive() {
        if (!isStarted) {
            throw new IllegalStateException("server is not running");
        }
        return request.getData();
    }

    public void clean() {
        request = new Request<String>(null);
    }

    static class Request<T> {
        private T data;

        Request(T data) {
            this.data = data;
        }

        T getData() {
            return data;
        }
    }
}
