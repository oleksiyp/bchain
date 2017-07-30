package bchain.app;

public class Result {
    private final boolean ok;
    private String message;

    public Result() {
        this.ok = true;
    }


    public Result(String message) {
        this.ok = false;
        this.message = message;
    }

    public static Result verificationFailed() {
        return new Result("Verification failed");
    }

    public static Result containsSame() {
        return new Result("Same object exists");
    }

    public static Result ok() {
        return new Result();
    }
}
