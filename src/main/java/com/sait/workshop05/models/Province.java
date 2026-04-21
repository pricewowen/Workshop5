// Contributor(s): Samantha
// Main: Samantha - Province enum for Canadian address fields.

package com.sait.workshop05.models;

/**
 * Canadian province or territory code with full name for address ComboBox rows.
 */
public class Province {
    private String code;
    private String name;

    /**
     * Creates one province row with code and display name.
     */
    public Province(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Returns province code value.
     */
    public String getCode() {
        return code;
    }

    /**
     * Returns province display name.
     */
    public String getName() {
        return name;
    }


}
