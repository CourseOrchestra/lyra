package ru.curs.lyra.kernel;

import foo.FooCursor;
import org.junit.jupiter.api.Test;
import ru.curs.celesta.CallContext;
import ru.curs.celestaunit.CelestaTest;
import ru.curs.lyra.kernel.forms.TestFormWithUnboundFields;
import ru.curs.lyra.kernel.grid.GridDriver;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.curs.lyra.kernel.grid.GridDriver.DEFAULT_COUNT;

@CelestaTest
public class BasicGridFormTest {

    public static final int NUM_RECORDS = 1000;

    @Test
    void positioning(CallContext ctx) throws InterruptedException {
        fillTable(ctx);

        final CountDownLatch latch = new CountDownLatch(1);
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx,
                f -> latch.countDown());

        List<LyraFormData> rows = testForm.getRowsH(ctx, 5);
        assertEquals(5, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(i, rows.get(i).getKeyValues()[0]);
        }

        assertEquals(0, testForm.getTopVisiblePosition());
        //position to key=42
        rows = testForm.setPositionH(ctx, 5, 42);

        assertEquals(5, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(42 + i, rows.get(i).getKeyValues()[0]);
        }

        latch.await();
        assertEquals(42, testForm.getTopVisiblePosition());

        //we're still positioned
        rows = testForm.getRowsH(ctx, 5);
        assertEquals(5, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(42 + i, rows.get(i).getKeyValues()[0]);
        }
    }

    @Test
    void scrollingWithInterpolation(CallContext ctx) throws InterruptedException {
        fillTable(ctx);
        //2 = one for initial refinement, one for refinement after interpolation
        final CountDownLatch latch = new CountDownLatch(2);
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx,
                f -> latch.countDown());

        long start = System.nanoTime();
        List<LyraFormData> rows = testForm.getRowsH(ctx, GridDriver.DEFAULT_SMALL_SCROLL + 50, 6);
        assertEquals(6, rows.size());

        latch.await();
        //    assertGreater()
        assertTrue((System.nanoTime() - start) / 1_000_000L >= GridDriver.REFINEMENT_DELAY_MS);
        int topPosition = testForm.getTopVisiblePosition();
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(topPosition + i, rows.get(i).getKeyValues()[0]);
        }

        //we're still positioned
        rows = testForm.getRowsH(ctx, 7);
        assertEquals(7, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(topPosition + i, rows.get(i).getKeyValues()[0]);
        }
    }


    @Test
    void scrollingWithoutInterpolation(CallContext ctx) throws InterruptedException {
        fillTable(ctx);
        //NB: only one!
        final CountDownLatch latch = new CountDownLatch(1);

        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx,
                f -> latch.countDown());

        long start = System.nanoTime();
        final int position = GridDriver.DEFAULT_SMALL_SCROLL / 2;
        List<LyraFormData> rows = testForm.getRowsH(ctx, position, 6);

        latch.await();
        assertTrue((System.nanoTime() - start) / 1_000_000L <= GridDriver.REFINEMENT_DELAY_MS);
        assertEquals(position, testForm.getTopVisiblePosition());
        assertEquals(6, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(position + i, rows.get(i).getKeyValues()[0]);
        }

        //we're still positioned
        rows = testForm.getRowsH(ctx, 7);
        assertEquals(7, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(position + i, rows.get(i).getKeyValues()[0]);
        }
    }

    @Test
    void outScrolling(CallContext ctx) {
        fillTable(ctx);
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx,
                null);
        List<LyraFormData> rows = testForm.getRowsH(ctx, NUM_RECORDS * 2, 6);
        assertEquals(6, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(NUM_RECORDS - 6 + i, rows.get(i).getKeyValues()[0]);
            // System.out.println(rows.get(i).getKeyValues()[0]);
        }

        //we're still positioned
        rows = testForm.getRowsH(ctx, 7);
        assertEquals(7, rows.size());
        for (int i = 0; i < rows.size(); i++) {
            assertEquals(NUM_RECORDS - 7 + i, rows.get(i).getKeyValues()[0]);
        }
    }

    @Test
    void approxTotalCountRefinedAutomatically(CallContext ctx) throws InterruptedException {
        fillTable(ctx);
        final CountDownLatch latch = new CountDownLatch(1);
        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx,
                f -> latch.countDown());
        assertEquals(DEFAULT_COUNT, testForm.getApproxTotalCount());
        latch.await();
        assertEquals(NUM_RECORDS, testForm.getApproxTotalCount());
    }

    @Test
    void tableTruncation(CallContext ctx) {
        fillTable(ctx);

        TestFormWithUnboundFields testForm = new TestFormWithUnboundFields(ctx, null);

        List<LyraFormData> rows = testForm.getRowsH(ctx, 5);
        assertEquals(5, rows.size());
        assertTrue(testForm.getApproxTotalCount() > 0);
        FooCursor c = new FooCursor(ctx);
        c.deleteAll();
        ctx.commit();

        rows = testForm.getRowsH(ctx, 5);
        assertEquals(0, rows.size());
        assertEquals(0, testForm.getApproxTotalCount());

        testForm.setPositionH(ctx, 5, 10);
        assertEquals(0, rows.size());
        assertEquals(0, testForm.getApproxTotalCount());
    }

    private void fillTable(CallContext ctx) {
        FooCursor c = new FooCursor(ctx);
        c.deleteAll();
        for (int i = 0; i < NUM_RECORDS; i++) {
            c.setId(i);
            c.setName(Integer.toString(i, 16));
            c.insert();
        }
        //commit in order to make separate refinement thread see the records.
        ctx.commit();
    }
}
