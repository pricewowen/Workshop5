package com.sait.workshop05.util;

public class Validator {
    /**
     * Valides for valid email address
     * @param email String value of the email to validate
     * @return String with an error message if invalid. Null if valid
     */
    public static String isValidEmail(String email) {
        String emailRegex = "^[^\\s@]+@([^\\s@]+\\.)+[A-Za-z]{2,}$";

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

        // make sure there are not less than 10 numbers
        String digits = phoneNum.replaceAll("\\D", "");
        if (digits.length() != 10) {
            return ("Phone number must be 10 digits");
        }

        if (!phoneNum.matches(phoneRegex)) {
            return "Phone number format is invalid";
        }
        return null;
    }

    /**
     * Validates name with minimal special characters and spaces
     * @param name String value to validate
     * @param input String value of the input field name
     * @return error message if invalid. Null if valid
     */
    public static String isValidName(String name, String input) {
        String nameRegex = "^[a-zA-Z0-9' -]+$";

        if (name.isEmpty()) {
            return input + " is required";
        }

        if (!name.matches(nameRegex)) {
            return input + " format is invalid. No special Characters allowed";
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

    /**
     * Validates the province code
     * @param province String value of the province code
     * @return error message if invalid. Null if valid
     */
    public static String isValidProvince(String province) {
        String provinceRegex = "^[A-Z]{2}$";

        if (province.isEmpty()) {
            return "Province is required";
        }

        if (!province.matches(provinceRegex)) {
            return "Province format is invalid. Please use the drop down provided";
        }
        return null;
    }

    /**
     * Validates the postal code
     * @param postal String value of the postal code
     * @return error message if invalid. Null if valid
     */
    public static String isValidPostalCode(String postal) {
        String postalRegex = "^[A-Z]\\d[A-Z] \\d[A-Z]\\d$";

        if (postal.isEmpty()) {
            return "Postal code is required";
        }

        if (!postal.matches(postalRegex)) {
            return "Postal code format is invalid. Please make sure it is A1A 1A1";
        }
        return null;
    }
}
