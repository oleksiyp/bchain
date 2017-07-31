package bchain.app.result;

import bchain.domain.Hash;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class Result {
    private final boolean ok;
    private final String message;

    public Result() {
        this.ok = true;
        this.message = "OK";
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

    public static Result orphaned() {
        return new Result("Block orphaned");
    }

    public static Result ok() {
        return new Result();
    }

}
