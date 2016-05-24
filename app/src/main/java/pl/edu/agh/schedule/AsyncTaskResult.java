package pl.edu.agh.schedule;

public class AsyncTaskResult {
    private String result;
    private Exception error;

    public String getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }

    public AsyncTaskResult(String result) {
        super();
        this.result = result;
    }

    public AsyncTaskResult(Exception error) {
        super();
        this.error = error;
    }
}
