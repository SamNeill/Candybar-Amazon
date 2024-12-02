package candybar.lib.utils;

public class License {
    /*
     * License Checker
     * Enable or disable license checker
     * Set license key for premium apps from Google Play Console
     */
    private static final boolean LICENSE_CHECKER_ENABLED = true;
    private static final String LICENSE_KEY = "YOUR_LICENSE_KEY";

    /*
     * Premium Request
     * Set premium request products
     * Product ID from Google Play Console
     */
    private static final String[] PREMIUM_REQUEST_PRODUCTS = {
        "1_20_icon_request",  // 20 icons for $1.99
        "1_30_icon_request",  // 30 icons for $2.99  
        "1_40_icon_request",  // 40 icons for $3.99
        "1_50_icon_request"   // 50 icons for $4.99
    };

    private static final int[] PREMIUM_REQUEST_COUNTS = {
        20,  // For 1_20_icon_request
        30,  // For 1_30_icon_request
        40,  // For 1_40_icon_request
        50   // For 1_50_icon_request
    };

    /*
     * Donation
     * Set donation products
     * Product ID from Google Play Console 
     */
    private static final String[] DONATION_PRODUCTS = {
        "coffee",      // $0.99 - Buy me a coffee
        "breakfast",   // $1.99 - Buy me breakfast
        "lunch",       // $4.99 - Buy me lunch
        "dinner"       // $9.99 - Buy me dinner
    };

    /*
     * In-App Billing Preferences
     */
    private static final boolean ENABLE_IN_APP_BILLING = true;
    private static final boolean ENABLE_PREMIUM_REQUEST = true;
    private static final boolean ENABLE_RESTORE_PURCHASES = false;
    private static final boolean ENABLE_DONATION = true;
    private static final int PREMIUM_REQUEST_LIMIT = 5;
    private static final boolean RESET_PREMIUM_REQUEST_LIMIT = true;

    public static String[] getPremiumRequestProductsId() {
        return PREMIUM_REQUEST_PRODUCTS;
    }

    public static int[] getPremiumRequestProductsCount() {
        return PREMIUM_REQUEST_COUNTS;
    }

    public static String[] getDonationProductsId() {
        return DONATION_PRODUCTS;
    }

    public static String getLicenseKey() {
        return LICENSE_KEY;
    }

    public static boolean isLicenseCheckerEnabled() {
        return LICENSE_CHECKER_ENABLED;
    }

    // In-App Billing Preferences Getters
    public static boolean isInAppBillingEnabled() {
        return ENABLE_IN_APP_BILLING;
    }

    public static boolean isPremiumRequestEnabled() {
        return ENABLE_PREMIUM_REQUEST;
    }

    public static boolean isRestorePurchasesEnabled() {
        return ENABLE_RESTORE_PURCHASES;
    }

    public static boolean isDonationEnabled() {
        return ENABLE_DONATION;
    }

    public static int getPremiumRequestLimit() {
        return PREMIUM_REQUEST_LIMIT;
    }

    public static boolean isResetPremiumRequestLimit() {
        return RESET_PREMIUM_REQUEST_LIMIT;
    }
} 