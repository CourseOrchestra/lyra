package ru.curs.lyra.kernel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFormProperties {
    @Test
    public void formPropertiesOverrideParent() {
        LyraFormProperties p1 = new LyraFormProperties();

        LyraFormProperties p2 = new LyraFormProperties(p1);

        p1.setGridwidth("100");
        p1.setGridheight("200");
        p2.setGridwidth("300");

        assertEquals("100", p1.getGridwidth());
        assertEquals("200", p1.getGridheight());

        assertEquals("300", p2.getGridwidth());
        assertEquals("200", p2.getGridheight());


    }
}
