package com.atc0194.huge_homework;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private DashBoardData data;

    @Before
    public void setup() {
        data = new DashBoardData();
    }

    @Test
    public void testParse() {
        String[] rawDatas = {"1_80_1153_50",
                            "0_25_999999_23",
                            "0_0_0_0"};

        for(String rawData : rawDatas) {
            data.parseData(rawData);
            assertEquals(data.isTurnLeft(), rawData.split(DashBoardData.SPLITCHAR)[0].equals("1"));
            assertEquals("" + data.getMass(), rawData.split(DashBoardData.SPLITCHAR)[1]);
            assertEquals("" + data.getMileage(), rawData.split(DashBoardData.SPLITCHAR)[2]);
            assertEquals("" + data.getSpeed(), rawData.split(DashBoardData.SPLITCHAR)[3]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseDataNum() {
        data.parseData("0_54_247635_");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMassUpperRange() {
        data.parseData("0_101_142_94");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMassLowerRange() {
        data.parseData("0_-10_142_94");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMileageUpperRange() {
        data.parseData("0_54_1000000_150");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseMileageLowerRange() {
        data.parseData("0_54_-1_150");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseSpeedUpperRange() {
        data.parseData("0_85_1894_-24");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseSpeedLowerRange() {
        data.parseData("0_85_1894_181");
    }
}