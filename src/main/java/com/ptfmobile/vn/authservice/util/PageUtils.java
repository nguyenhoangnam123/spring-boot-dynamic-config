package com.ptfmobile.vn.authservice.util;

/**
 * @author MinhDV
 */
public class PageUtils {

    public static int getCurrentPage(int total, int start, int limit) {
        if (total == 0)
            return 1;
        return ((start + limit) / total) + 1;
    }
}
