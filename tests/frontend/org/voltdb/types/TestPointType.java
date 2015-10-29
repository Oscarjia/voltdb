/* This file is part of VoltDB.
 * Copyright (C) 2008-2015 VoltDB Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.voltdb.types;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class TestPointType extends TestCase {

    private void assertConstructorThrows(String expectedMessage, double lat, double lng) {
        try {
            new PointType(lat, lng);
            fail("Expected constructor to throw an exception");
    }
        catch (IllegalArgumentException iae) {
            assertTrue("Didn't find expected message in exception thrown by PointType constructor",
                    iae.getMessage().contains(expectedMessage));
        }
    }

    public void testPointCtor() {
        assertEquals(16, PointType.getLengthInBytes());

        PointType point = new PointType(10.333, 20.666);
        assertEquals(10.333, point.getLatitude(), 0.001);
        assertEquals(20.666, point.getLongitude(), 0.001);

        assertTrue(point.equals(point));
        assertFalse(point.equals(new PointType(0.0, 10.0)));

        assertEquals("POINT (10.333 20.666)", point.toString());

        // Make sure that it's not possible to create points
        // with bogus latitude or longitude.
        assertConstructorThrows("Latitude out of range", -91.0, 100);
        assertConstructorThrows("Latitude out of range", 91.0, 100);
        assertConstructorThrows("Longitude out of range", 45.0, 181.0);
        assertConstructorThrows("Longitude out of range", 45.0, -181.0);
    }

    public void testPointSerialization() {

        int len = PointType.getLengthInBytes();
        assertEquals(16, len);

        ByteBuffer bb = ByteBuffer.allocate(len);
        bb.putDouble(33.0);
        bb.putDouble(45.0);

        // Test deserialization
        bb.position(0);
        PointType pt = PointType.unflattenFromBuffer(bb);
        assertEquals("POINT (33.0 45.0)", pt.toString());

        // Test deserialization with offset argument
        bb.position(0);
        pt = PointType.unflattenFromBuffer(bb, 0);
        assertEquals("POINT (33.0 45.0)", pt.toString());

        // Test serialization
        pt = new PointType(-64.0, -77.0);
        bb.position(0);
        pt.flattenToBuffer(bb);
        bb.position(0);
        assertEquals(-64.0, bb.getDouble());
        assertEquals(-77.0, bb.getDouble());

        // Null serialization puts 360.0 in both lat and long
        bb.position(0);
        PointType.serializeNull(bb);
        bb.position(0);
        assertEquals(360.0, bb.getDouble());
        assertEquals(360.0, bb.getDouble());
    }

    private void testOnePointFromFactory(String aWKT, double aLatitude, double aLongitude, double aEpsilon, String aErrMsg) {
        try {
            PointType point = PointType.pointFromText(aWKT);
            assertEquals(aLatitude, point.getLatitude(), aEpsilon);
            if (aErrMsg != null) {
                assertTrue(String.format("Expected error message matching \"%s\", but got no error.", aErrMsg), aErrMsg == null);
            }
        } catch (Exception ex) {
            if (aErrMsg != null) {
                assertTrue(String.format("Expected error message matching \"%s\", but got \"%s\"",
                                         aErrMsg, ex.getMessage()),
                           Pattern.matches(aErrMsg, ex.getMessage()));
            } else {
                assertTrue(String.format("Unexpected error message: \"%s\"", ex.getMessage()), false);
            }
        }
    }

    public void testPointFactory() {
        testOnePointFromFactory("point(10.333 20.666)",               10.333,  20.666, 0.001, null);
        testOnePointFromFactory("  point  (10.333   20.666)    ",     10.333,  20.666, 0.001, null);
        testOnePointFromFactory("point(-10.333 -20.666)",            -10.333, -20.666, 0.001, null);
        testOnePointFromFactory("  point  (-10.333   -20.666)    ",  -10.333, -20.666, 0.001, null);
        testOnePointFromFactory("point(10 10)",                       10.0,    10.0,   0.001, null);
        // Test latitude/longitude ranges.
        testOnePointFromFactory("point( 100.0   100.0)",             100.0,   100.0,   0.001, "Latitude \"100.0+\" out of bounds.");
        testOnePointFromFactory("point(  45.0   360.0)",              45.0,   360.0,   0.001, "Longitude \"360.0+\" out of bounds.");
        testOnePointFromFactory("point(  45.0   270.0)",              45.0,   360.0,   0.001, "Longitude \"270.0+\" out of bounds.");
        testOnePointFromFactory("point(-100.0  -100.0)",            -100.0,  -100.0,   0.001, "Latitude \"-100.0+\" out of bounds.");
        testOnePointFromFactory("point( -45.0  -360.0)",             -45.0,  -360.0,   0.001, "Longitude \"-360.0+\" out of bounds.");
        testOnePointFromFactory("point( -45.0  -270.0)",             -45.0,  -360.0,   0.001, "Longitude \"-270.0+\" out of bounds.");
        // Syntax errors
        //   Comma separating the coordinates.
        testOnePointFromFactory("point(0.0, 0.0)",                     0.0,     0.0,   0.001, "Cannot construct PointType value from \"point\\(0[.]0, 0[.]0\\)\"");
    }
}