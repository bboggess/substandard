package com.example.substandard;

import com.example.substandard.database.network.SubsonicNetworkUtils;

import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for all functionality related to sending Network requests.
 */
public class NetworkUtilsUnitTest {
    private static final String TAG = "NetworkUtilsUnitTest";

    @Test
    public void hashComputedCorrectly() throws Exception {
        Method hashMethod = SubsonicNetworkUtils.class.getDeclaredMethod("createAuthToken", String.class, String.class);
        hashMethod.setAccessible(true);
        String hash = (String) hashMethod.invoke(hashMethod,"password", "sosalty");
        assertEquals(hash, "45c9cbadb2ed94bdcd537d28464d1834");
    }

    @Test
    public void saltNotNull() throws Exception {
        Method saltMethod = SubsonicNetworkUtils.class.getDeclaredMethod("createSalt");
        saltMethod.setAccessible(true);
        String salt = (String) saltMethod.invoke(saltMethod);
        assertNotNull(salt);
        assertEquals(6, salt.length());
    }

}
