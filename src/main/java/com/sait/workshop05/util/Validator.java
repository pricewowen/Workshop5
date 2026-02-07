package com.sait.workshop05.util;

public class Validator {
    /**
     * Valides for valid email address
     * @param email String value of the email to validate
     * @return String with an error message if invalid. Null if valid
     */
    public static String isValidEmail(String email) {
        String emailRegex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

        if (email.isEmpty()) {
            return "Email is required";
        }

        if (!email.matches(emailRegex)) {
            return "Email format is invalid";
        }

        return null;
    }

    /**
     * Validates for valid phone number
     * @param phoneNum String value of the number to validate
     * @return String with an error message if invalid. Null if valid
     */
    public static String isValidPhoneNumber(String phoneNum) {
        String phoneRegex = "^[0-9+()\\-\\s]{7,20}$";
        if (phoneNum.isEmpty()) {
            return "Phone number is required";
        }

        if (!phoneNum.matches(phoneRegex)) {
            return "Phone number format is invalid";
        }
        return null;
    }

    /**
     * Validates name with minimal special characters and spaces
     * @param name String value to validate
     * @return error message if invalid. Null if valid
     */
    public static String isValidName(String name) {
        String nameRegex = "^[a-zA-Z0-9' -]+$";

        if (name.isEmpty()) {
            return "Name is required";
        }

        if (!name.matches(nameRegex)) {
            return "Name format is invalid. No special Characters allowed";
        }
        return null;
    }

    /**
     * Validates addresses
     * @param address String value of the address to validate
     * @param num int value for if it's first address line or second
     * @return an error message if invalid. Null if valid
     */
    public static String isValidAddress(String address, int num) {
        String addressRegex = "^[a-zA-Z0-9\\s,'./#-]+$";

        if (address.isEmpty() && num == 1) {
            return "Address is required";
        }

        if (address.isEmpty() && num == 2) {
            return null;
        }

        if (!address.matches(addressRegex)) {
            return "Address" + num + " format is invalid";
        }
        return null;
    }
}
