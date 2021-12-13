package com.nttdata.transfer.exception;

public class messageException {

    public static String incorrectTransferType(){
        return "The type of transfer is wrong";
    }

    public static String insufficientBalance(){
        return "The origin account has insufficient balance";
    }

    public static String originAccountNotFound(){
        return "The source account number is incorrect";
    }

    public static String destinationAccountNotFound(){
        return "The destination account number is incorrect";
    }

    public static  String ownAccountError(){
        return "The accounts do not belong to the same customer";
    }
    public static  String thirdPartyAccountError(){
        return "The accounts belong to the same customer";
    }

}
