package com.sait.workshop05.database;

import com.sait.workshop05.models.AddressOption;
import com.sait.workshop05.models.UserOption;

import java.sql.*;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared DAO utilities to eliminate duplication across individual DAOs.
 */
public class SharedDAO {
    private static final Pattern CANADA_POSTAL_RX =
            Pattern.compile("([A-Za-z]\\d[A-Za-z])[\\s-]?(\\d[A-Za-z]\\d)$");
    /** Compact Canadian postal after removing spaces (e.g. T2R1A5). */
    private static final Pattern CANADA_POSTAL_COMPACT = Pattern.compile("[A-Za-z]\\d[A-Za-z]\\d[A-Za-z]\\d");

    /** Official two-letter codes for provinces and territories. */
    private static final Set<String> CANADA_PT_CODES = Set.of(
            "AB", "BC", "MB", "NB", "NL", "NS", "NT", "NU", "ON", "PE", "QC", "SK", "YT"
    );

    /** English full names and common aliases → canonical code. */
    private static final Map<String, String> CANADA_PROVINCE_NAME_TO_CODE = new HashMap<>();

    static {
        registerProvinceAliases("AB", "Alberta");
        registerProvinceAliases("BC", "British Columbia");
        registerProvinceAliases("MB", "Manitoba");
        registerProvinceAliases("NB", "New Brunswick");
        registerProvinceAliases("NL",
                "Newfoundland and Labrador",
                "Newfoundland",
                "Labrador",
                "NF");
        registerProvinceAliases("NS", "Nova Scotia");
        registerProvinceAliases("NT", "Northwest Territories", "NWT");
        registerProvinceAliases("NU", "Nunavut");
        registerProvinceAliases("ON", "Ontario");
        registerProvinceAliases("PE", "Prince Edward Island", "PEI");
        registerProvinceAliases("QC", "Quebec", "PQ");
        registerProvinceAliases("SK", "Saskatchewan");
        registerProvinceAliases("YT", "Yukon", "Yukon Territory");
    }

    private static void registerProvinceAliases(String code, String... names) {
        for (String n : names) {
            CANADA_PROVINCE_NAME_TO_CODE.put(normalizeProvinceLookupKey(n), code);
        }
    }

    /**
     * Key for map lookup: lowercase, no accents, hyphens/dots → space, collapsed whitespace.
     */
    private static String normalizeProvinceLookupKey(String s) {
        if (s == null) {
            return "";
        }
        String t = Normalizer.normalize(s.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        t = t.toLowerCase(Locale.ROOT).replace('-', ' ').replace('.', ' ');
        return t.replaceAll("\\s+", " ").trim();
    }

    /**
     * Canonical province/territory for storage and matching: official code when recognized,
     * otherwise trimmed collapsed input.
     */
    private static String canonicalCanadianProvince(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String collapsed = collapseWhitespace(input);
        String key = normalizeProvinceLookupKey(collapsed);

        if (key.length() == 2) {
            String u = key.toUpperCase(Locale.ROOT);
            if (CANADA_PT_CODES.contains(u)) {
                return u;
            }
        }

        String fromName = CANADA_PROVINCE_NAME_TO_CODE.get(key);
        if (fromName != null) {
            return fromName;
        }

        return collapsed;
    }

    /** Get all users for ComboBox dropdowns. Used by Employee and Customer DAOs. */
    public static List<UserOption> getUserOptions() throws SQLException {
        String sql = "SELECT userId, userUsername FROM `User` ORDER BY userId DESC";
        List<UserOption> options = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                options.add(new UserOption(rs.getInt("userId"), rs.getString("userUsername")));
            }
        }
        return options;
    }

    /** Get all addresses for ComboBox dropdowns. Used by Employee, Customer, and Order DAOs. */
    public static List<AddressOption> getAddressOptions() throws SQLException {
        String sql =
                "SELECT addressId, addressLine1, addressCity, addressProvince, addressPostalCode " +
                        "FROM Address ORDER BY addressId DESC";

        List<AddressOption> options = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                options.add(new AddressOption(
                        rs.getInt("addressId"),
                        rs.getString("addressLine1"),
                        rs.getString("addressCity"),
                        rs.getString("addressProvince"),
                        rs.getString("addressPostalCode")
                ));
            }
        }
        return options;
    }

    public static AddressOption findOrCreateAddressFromInput(String input) throws SQLException {
        ParsedAddress parsed = parseTypedAddress(input);

        try (Connection conn = DBUtil.getConnection()) {
            AddressOption exact = findExactMatch(conn, parsed);
            if (exact != null) {
                return exact;
            }

            AddressOption normalized = findNormalizedMatch(conn, parsed);
            if (normalized != null) {
                return normalized;
            }

            String insertSql = """
                    INSERT INTO Address (addressLine1, addressLine2, addressCity, addressProvince, addressPostalCode)
                    VALUES (?, NULL, ?, ?, ?)
                    """;

            try (PreparedStatement insert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insert.setString(1, parsed.line1());
                insert.setString(2, parsed.city());
                insert.setString(3, parsed.province());
                insert.setString(4, parsed.postalCode());
                insert.executeUpdate();

                try (ResultSet keys = insert.getGeneratedKeys()) {
                    if (keys.next()) {
                        return new AddressOption(
                                keys.getInt(1),
                                parsed.line1(),
                                parsed.city(),
                                parsed.province(),
                                parsed.postalCode()
                        );
                    }
                }
            }
        }

        throw new SQLException("Unable to create address.");
    }

    /** Fast path: values already match DB formatting (trim + case + postal spacing). */
    private static AddressOption findExactMatch(Connection conn, ParsedAddress parsed) throws SQLException {
        String findSql = """
                SELECT addressId, addressLine1, addressCity, addressProvince, addressPostalCode
                FROM Address
                WHERE LOWER(TRIM(addressLine1)) = LOWER(TRIM(?))
                  AND LOWER(TRIM(addressCity)) = LOWER(TRIM(?))
                  AND LOWER(TRIM(addressProvince)) = LOWER(TRIM(?))
                  AND REPLACE(REPLACE(UPPER(TRIM(IFNULL(addressPostalCode, ''))), ' ', ''), '-', '')
                      = REPLACE(REPLACE(UPPER(TRIM(?)), ' ', ''), '-', '')
                LIMIT 1
                """;
        try (PreparedStatement find = conn.prepareStatement(findSql)) {
            find.setString(1, parsed.line1());
            find.setString(2, parsed.city());
            find.setString(3, parsed.province());
            find.setString(4, parsed.postalCode());
            try (ResultSet rs = find.executeQuery()) {
                if (rs.next()) {
                    return readAddressOption(rs);
                }
            }
        }
        return null;
    }

    /**
     * Match after normalizing user input and DB values the same way (extra spaces, postal A1A1A1 vs A1A 1A1, etc.).
     * Reuses existing {@code addressId} when logically the same address.
     */
    private static AddressOption findNormalizedMatch(Connection conn, ParsedAddress parsed) throws SQLException {
        String postalKey = postalComparableKey(parsed.postalCode());
        if (postalKey.isEmpty()) {
            return null;
        }

        String sql = """
                SELECT addressId, addressLine1, addressCity, addressProvince, addressPostalCode
                FROM Address
                WHERE REPLACE(REPLACE(UPPER(TRIM(IFNULL(addressPostalCode, ''))), ' ', ''), '-', '') = ?
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, postalKey);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String dbLine1 = rs.getString("addressLine1");
                    String dbCity = rs.getString("addressCity");
                    String dbProvince = rs.getString("addressProvince");
                    if (addressPartsMatch(parsed.line1(), parsed.city(), parsed.province(), dbLine1, dbCity, dbProvince)) {
                        return readAddressOption(rs);
                    }
                }
            }
        }
        return null;
    }

    private static AddressOption readAddressOption(ResultSet rs) throws SQLException {
        return new AddressOption(
                rs.getInt("addressId"),
                rs.getString("addressLine1"),
                rs.getString("addressCity"),
                rs.getString("addressProvince"),
                rs.getString("addressPostalCode")
        );
    }

    private static boolean addressPartsMatch(
            String line1, String city, String province,
            String dbLine1, String dbCity, String dbProvince) {
        return comparableAddressPart(line1).equals(comparableAddressPart(dbLine1))
                && comparableAddressPart(city).equals(comparableAddressPart(dbCity))
                && comparableProvince(province).equals(comparableProvince(dbProvince));
    }

    /** Trim and collapse internal whitespace for comparison. */
    private static String comparableAddressPart(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Province: same logical region matches (Alberta vs AB, Québec vs QC). */
    private static String comparableProvince(String s) {
        return canonicalCanadianProvince(s).toUpperCase(Locale.ROOT);
    }

    /** Canadian postal: strip spaces and hyphens, uppercase (A1A1A1). */
    private static String postalComparableKey(String postal) {
        if (postal == null) {
            return "";
        }
        return postal.trim().toUpperCase(Locale.ROOT).replaceAll("[\\s-]+", "");
    }

    /** Convert empty/whitespace string to null for DB insertion. */
    public static String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static ParsedAddress parseTypedAddress(String input) {
        String s = input == null ? "" : input.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Address is required.");
        }

        s = s.replaceFirst("^\\d+\\s*-\\s*", "").trim();

        String[] parts = s.split(",");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Address format must be: line1, city, province postalCode");
        }

        String line1 = parts[0].trim();
        String city = parts[1].trim();
        String province;
        String postalCode;

        if (parts.length >= 4) {
            province = parts[2].trim();
            postalCode = parts[3].trim();
        } else {
            String tail = parts[2].trim();
            Matcher m = CANADA_POSTAL_RX.matcher(tail);
            if (!m.find()) {
                throw new IllegalArgumentException("Address must include a postal code, e.g. T2A 3B4.");
            }
            postalCode = (m.group(1) + " " + m.group(2)).toUpperCase(Locale.ROOT);
            province = tail.substring(0, m.start()).trim();
        }

        line1 = collapseWhitespace(line1);
        city = collapseWhitespace(city);
        province = collapseWhitespace(province);
        postalCode = collapseWhitespace(postalCode);

        if (line1.isBlank() || city.isBlank() || province.isBlank() || postalCode.isBlank()) {
            throw new IllegalArgumentException("Address format must be: line1, city, province postalCode");
        }

        province = canonicalCanadianProvince(province);
        postalCode = normalizePostal(postalCode);
        return new ParsedAddress(line1, city, province, postalCode);
    }

    private static String collapseWhitespace(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().replaceAll("\\s+", " ");
    }

    /**
     * Canonical Canadian postal for storage and matching: {@code A1A 1A1}.
     * Accepts {@code A1A1A1}, {@code A1A 1A1}, {@code a1a-1a1}, etc.
     */
    private static String normalizePostal(String raw) {
        String key = postalComparableKey(raw);
        if (key.length() == 6 && CANADA_POSTAL_COMPACT.matcher(key).matches()) {
            return key.substring(0, 3).toUpperCase(Locale.ROOT) + " " + key.substring(3).toUpperCase(Locale.ROOT);
        }

        String spaced = raw.trim().toUpperCase(Locale.ROOT).replace('-', ' ').replaceAll("\\s+", " ");
        Matcher m = CANADA_POSTAL_RX.matcher(spaced);
        if (!m.find()) {
            throw new IllegalArgumentException("Postal code format is invalid. Example: T2A 3B4.");
        }
        return (m.group(1) + " " + m.group(2)).toUpperCase(Locale.ROOT);
    }

    private record ParsedAddress(String line1, String city, String province, String postalCode) {}
}
