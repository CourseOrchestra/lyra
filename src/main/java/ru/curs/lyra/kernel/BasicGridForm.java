package ru.curs.lyra.kernel;

import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.dbutils.Cursor;
import ru.curs.lyra.kernel.grid.GridDriver;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base Java class for Lyra grid form.
 *
 * @param <T> type of the form's main cursor
 */
public abstract class BasicGridForm<T extends BasicCursor> extends BasicLyraForm<T> {

    private static final int DEFAULT_GRID_HEIGHT = 50;

    private GridDriver gd;
    private final LinkedList<BasicCursor> savedPositions = new LinkedList<>();
    private final GridRefinementHandler changeNotifier;
    private final Runnable driverNotifier;

    public BasicGridForm(CallContext context, GridRefinementHandler changeNotifier) {
        super(context);
        this.changeNotifier = changeNotifier;
        if (changeNotifier == null){
            driverNotifier = null;
        } else {
            driverNotifier = () -> changeNotifier.accept(this);
        }
        actuateGridDriver(getCursor(context));
    }

    private void actuateGridDriver(BasicCursor c) {
        if (gd == null) {
            gd = new GridDriver(c, driverNotifier);
        } else if (!gd.isValidFor(c)) {
            int maxExactScrollValue = gd.getMaxExactScrollValue();
            gd = new GridDriver(c, driverNotifier);
            gd.setMaxExactScrollValue(maxExactScrollValue);
        }
    }

    /**
     * Returns contents of grid given scrollbar's position.
     *
     * @param ctx      current CallContext
     * @param position New scrollbar's position.
     */
    public List<LyraFormData> getRows(CallContext ctx, int position) {
        return getRowsH(ctx, position, getGridHeight());
    }

    /**
     * Returns contents of grid given scrollbar's position.
     *
     * @param ctx      current CallContext
     * @param position New scrollbar's position.
     * @param h        Form's height in rows
     */
    public synchronized List<LyraFormData> getRowsH(CallContext ctx, int position, int h) {
        BasicCursor c = rec(ctx);
        actuateGridDriver(c);
        if (gd.setPosition(position, c)) {
            return returnRows(c, h);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns contents of grid for current cursor's position.
     *
     * @param ctx current CallContext
     */
    public synchronized List<LyraFormData> getRows(CallContext ctx) {
        return getRowsH(ctx, getGridHeight());
    }

    /**
     * Returns contents of grid for current cursor's position.
     *
     * @param ctx current CallContext
     * @param h   form height in rows
     */
    public synchronized List<LyraFormData> getRowsH(CallContext ctx, int h) {
        BasicCursor bc = rec(ctx);
        String cmd =
                Arrays.stream(bc._currentValues()).anyMatch(Objects::nonNull)
                        ? "=<-" : "-";
        if (bc.navigate(cmd)) {
            gd.setPosition(bc);
            return returnRows(bc, h);
        } else {
            gd.truncate();
            return Collections.emptyList();
        }
    }

    public synchronized List<LyraFormData> setPosition(CallContext ctx, Object... pk) {
        return setPositionH(ctx, getGridHeight(), pk);
    }

    /**
     * Positions grid to a certain record.
     *
     * @param ctx current CallContext
     * @param pk  Values of primary key
     */
    public synchronized List<LyraFormData> setPositionH(CallContext ctx, int h, Object... pk) {
        BasicCursor bc = rec(ctx);
        actuateGridDriver(bc);
        if (bc instanceof Cursor) {
            Cursor c = (Cursor) bc;
            if (c.meta().getPrimaryKey().size() != pk.length) {
                throw new CelestaException(
                        "Invalid number of 'setPosition' arguments for '%s': expected %d, provided %d.",
                        c.meta().getName(), c.meta().getPrimaryKey().size(), pk.length);
            }
            int i = 0;
            for (String name : c.meta().getPrimaryKey().keySet()) {
                c.setValue(name, pk[i++]);
            }
        } else {
            bc.setValue(bc.meta().getColumns().keySet().iterator().next(), pk[0]);
        }

        if (bc.navigate("=<-")) {
            gd.setPosition(bc);
            return returnRows(bc, h);
        } else {
            return Collections.emptyList();
        }
    }

    private List<LyraFormData> returnRows(BasicCursor c, int h) {

        final String id = getId();
        final List<LyraFormData> result = new ArrayList<>(h);
        final Map<String, LyraFormField> meta = getFieldsMeta();
        BasicCursor copy = c._getBufferCopy(c.callContext(), null);
        copy.close();
        for (int i = 0; i < h; i++) {
            beforeSending(c);
            LyraFormData lfd = new LyraFormData(c, meta, id);
            result.add(lfd);
            if (!c.next()) {
                break;
            }
        }
        // return to the beginning!
        c.copyFieldsFrom(copy);

        if (result.size() < h) {
            for (int i = result.size(); i < h; i++) {
                if (!c.previous()) {
                    break;
                }
                beforeSending(c);
                LyraFormData lfd = new LyraFormData(c, meta, id);
                result.add(0, lfd);

            }
            c.copyFieldsFrom(copy);
        }

        return result;
    }

    /**
     * Returns change notifier.
     */
    public GridRefinementHandler getChangeNotifier() {
        return changeNotifier;
    }

    /**
     * If the grid is scrolled less than for given amount of records, the exact
     * positioning in cycle will be used instead of interpolation.
     *
     * @param val new value.
     */
    public void setMaxExactScrollValue(int val) {
        gd.setMaxExactScrollValue(val);
    }

    /**
     * If the grid is scrolled less than for given amount of records, the exact
     * positioning in cycle will be used instead of interpolation.
     */
    public int getMaxExactScrollValue() {
        return gd.getMaxExactScrollValue();
    }

    /**
     * Returns (approximate) total record count.
     * <p>
     * Just after creation of the form this method returns DEFAULT_COUNT value,
     * but it asynchronously requests total count right after constructor
     * execution.
     */
    public int getApproxTotalCount() {
        return gd.getApproxTotalCount();

    }

    /**
     * Returns scrollbar's knob position for current cursor value.
     */
    public int getTopVisiblePosition() {
        return gd.getTopVisiblePosition();
    }

    public void saveCursorPosition(CallContext ctx) {
        BasicCursor c = rec(ctx);
        BasicCursor copy = c._getBufferCopy(ctx, null);
        copy.close();
        savedPositions.push(copy);
    }

    public void restoreCursorPosition(CallContext ctx) {
        BasicCursor copy = savedPositions.pop();
        rec(ctx).copyFieldsFrom(copy);
    }

    /**
     * Should return a number of rows in grid.
     */
    public int getGridHeight() {
        return DEFAULT_GRID_HEIGHT;
    }

    /**
     * Should return a summary row.
     */
    public Map<String, String> getSummaryRow() {
        return null;
    }

}
