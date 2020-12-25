/*
 * Copyright (c) 2020. Electronic Tools Institute
 */

package org.etools.j1939_84.utils;

import java.util.Collection;

public class CollectionUtils {

    public static boolean areTwoCollectionsEqual(Collection<?> collectionA, Collection<?> collectionB) {

        //verify null checks
        if (collectionA == null && collectionB == null) {
            return true;
        }

        if (collectionA == null || collectionB == null) {
            return false;
        }

        //verify basic attributes
        if (collectionA.size() != collectionB.size()) {
            return false;
        }

        //ensure contents are the same
        for (Object itemA : collectionA) {
            if (!collectionB.contains(itemA)) {
                return false;
            }
        }

        return true;
    }
}
