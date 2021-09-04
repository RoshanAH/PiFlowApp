package utils.math;

public class InputOutOfDomainException extends RuntimeException{
    public InputOutOfDomainException(String message){
        super(message);
    }

    public InputOutOfDomainException(){
        super();
    }

}
