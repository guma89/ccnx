package org.ccnx.android.apps.ui.interfaces.transferObjects;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


public class TrainingPlanTest {
    private static double DISTANCE = 5.1;
    private static double AVG_SPEED = 20.2;
    private static double BURNED_CALORIES = 504;
    private static String PLAN_COMMENT = "pretty ok";
    private static int PLAN_RATE = 3;


    private TrainingPlan cut;

    @Before
    public void setUp() throws Exception {
        cut = new TrainingPlan(DISTANCE, AVG_SPEED, BURNED_CALORIES, PLAN_COMMENT, PLAN_RATE);
    }

    @Test
    public void getDistance_shouldBeSame() {
        // given

        // when
        double actualDistance = cut.getDistance();

        // then
        assertEquals("should be same distance", DISTANCE, actualDistance);
    }
}