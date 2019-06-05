package ru.curs.lyra.kernel.grid;

import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.SystemCallContext;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.dbutils.adaptors.DBAdaptor;
import ru.curs.celesta.dbutils.term.WhereTermsMaker;
import ru.curs.celesta.score.*;
import ru.curs.lyra.kernel.RefinementScheduler;
import ru.curs.lyra.kernel.RefinementTask;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Specifies the record position asynchronously, using separate execution
 * thread.
 */
public final class GridDriver {

    public static final int DEFAULT_SMALL_SCROLL = 120;
    public static final long REFINEMENT_DELAY_MS = 500;
    /**
     * The default assumption for a records count in a table.
     */
    public static final int DEFAULT_COUNT = 1024;
    private final KeyInterpolator interpolator;
    private final InterpolationInitializer interpolationInitializer;
    private final KeyEnumerator rootKeyEnumerator;
    private final Map<String, KeyEnumerator> keyEnumerators = new HashMap<>();
    private final Runnable changeNotifier;
    private final Counter counter = new Counter();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Inexact primary key for latest refinement request (cached to prevent
     * repeated refinement of the same value).
     */
    private BigInteger latestRequest;

    /**
     * Exact primary key for the current top visible record.
     */
    private BigInteger topVisiblePosition;

    /**
     * A closed copy of underlying cursor that handles filters and sorting.
     */
    private final BasicCursor closedCopy;

    private int smallScroll = DEFAULT_SMALL_SCROLL;

    private final class Counter extends RefinementScheduler {
        private BasicCursor c;
        private final List<String> columns =
                Arrays.stream(closedCopy.orderByColumnNames())
                        .map(WhereTermsMaker::unquot)
                        .collect(Collectors.toList());

        @Override
        protected boolean refineInterpolator() {
            int count = interpolator.getApproximateCount();
            return interpolationInitializer.initialize(c, count);
        }

        @Override
        protected void refineAndNotify(RefinementTask task) {
            // Execute the refinement task!
            setCursorOrdinal(c, task.getKey());
            int result = c.position();
            interpolator.setPoint(task.getKey(), result);
            if (changeNotifier != null) {
                changeNotifier.run();
            }
        }

        @Override
        protected void acquireContext() {
            CallContext sysContext =
                    new SystemCallContext(
                            closedCopy.callContext().getCelesta(),
                            "LyraCounterThread");
            c = closedCopy._getBufferCopy(sysContext, columns);
            c.copyFiltersFrom(closedCopy);
            c.copyOrderFrom(closedCopy);
        }

        @Override
        protected void releaseContext() {
            c.callContext().close();
        }
    }

    public GridDriver(BasicCursor c, Runnable changeNotifier) {
        this.changeNotifier = changeNotifier;
        // place to save filters and ordering
        closedCopy = c._getBufferCopy(c.callContext(), null);
        closedCopy.copyFiltersFrom(c);
        closedCopy.copyOrderFrom(c);
        closedCopy.close();

        // checking order
        final boolean[] descOrders = c.descOrders();
        final boolean desc = descOrders[0];
        for (int i = 1; i < descOrders.length; i++) {
            if (desc != descOrders[i]) {
                throw new CelestaException("Mixed ASC/DESC ordering for grid: %s", c.getOrderBy());
            }
        }

        // KeyEnumerator factory
        final DataGrainElement meta = c.meta();
        final String[] quotedNames = c.orderByColumnNames();
        final String[] names = new String[quotedNames.length];
        for (int i = 0; i < quotedNames.length; i++) {
            names[i] = quotedNames[i].substring(1, quotedNames[i].length() - 1);
        }

        DBAdaptor dbAdaptor = c.callContext().getDbAdaptor();
        if (names.length == 1) {
            // Single field key enumerator
            ColumnMeta m = meta.getColumns().get(names[0]);
            rootKeyEnumerator = createKeyEnumerator(m, dbAdaptor.nullsFirst(), dbAdaptor);
            keyEnumerators.put(names[0], rootKeyEnumerator);
        } else {
            // Multiple field key enumerator
            KeyEnumerator[] km = new KeyEnumerator[names.length];
            for (int i = 0; i < names.length; i++) {
                ColumnMeta m = meta.getColumns().get(names[i]);
                km[i] = createKeyEnumerator(m, dbAdaptor.nullsFirst(), dbAdaptor);
                keyEnumerators.put(names[i], km[i]);
            }
            rootKeyEnumerator = new CompositeKeyEnumerator(km);
        }


        if (c.navigate("+")) {
            BigInteger higherOrd = getCursorOrdinal(c);
            c.navigate("-");
            BigInteger lowerOrd = getCursorOrdinal(c);
            interpolator = new KeyInterpolator(lowerOrd, higherOrd, DEFAULT_COUNT, desc);
            topVisiblePosition = lowerOrd;
            // Request a total record count immediately -- but after
            // interpolator initialization
            requestRefinement(higherOrd, true);
        } else {
            // empty record set!
            interpolator = new KeyInterpolator(BigInteger.ZERO, BigInteger.ZERO, 0, desc);
            topVisiblePosition = BigInteger.ZERO;
        }

        interpolationInitializer = new InterpolationInitializer(interpolator, dbAdaptor) {
            @Override
            void setCursorOrdinal(BasicCursor c, BigInteger key) {
                GridDriver.this.setCursorOrdinal(c, key);
            }

            @Override
            BigInteger getCursorOrdinal(BasicCursor c) {
                return GridDriver.this.getCursorOrdinal(c);
            }
        };

        executorService.submit(counter);
    }

    /**
     * Checks if this driver is valid for a given cursor with its filters and
     * sorting.
     *
     * @param c Cursor for checking.
     */
    public boolean isValidFor(BasicCursor c) {
        return closedCopy.isEquivalent(c);
    }

    /**
     * Fills key fields of a cursor based on scroller knob position.
     *
     * @param position scrollbar knob position
     * @param c        Alive cursor to be modified
     * @return false if record set is empty
     */
    public boolean setPosition(int position, BasicCursor c) {
        checkMeta(c);
        // First, we are checking if exact positioning is possible
        final int closestPosition = interpolator.getClosestPosition(position);
        final int absDelta = Math.abs(position - closestPosition);

        if (absDelta < smallScroll) {
            // Trying to perform exact positioning!
            BigInteger key = interpolator.getExactPoint(closestPosition);
            if (key != null) {
                setCursorOrdinal(c, key);
                if (c.navigate("=")) {
                    String cmd = position > closestPosition ? ">" : "<";
                    for (int i = 0; i < absDelta; i++) {
                        // TODO: do something relevant when navigate(..) returns
                        // false
                        c.navigate(cmd);
                    }
                    BigInteger ord = getCursorOrdinal(c);
                    interpolator.setPoint(ord, position);
                    topVisiblePosition = ord;
                    return true;
                }
            }
        }
        // Exact positioning is not feasible, using interpolation
        BigInteger key = interpolator.getPoint(position);
        setCursorOrdinal(c, key);
        if (c.navigate("=>+")) {
            topVisiblePosition = getCursorOrdinal(c);
            requestRefinement(topVisiblePosition, false);
            return true;
        } else {
            // table became empty!
            c._clearBuffer(true);
            truncate();
            return false;
        }
    }

    public void truncate() {
        topVisiblePosition = BigInteger.ZERO;
        interpolator.resetToEmptyTable();
    }

    /**
     * Adjusts internal state for pre-positioned cursor.
     *
     * @param c Cursor that is set to a certain position.
     */
    public void setPosition(BasicCursor c) {
        checkMeta(c);
        topVisiblePosition = getCursorOrdinal(c);
        requestRefinement(topVisiblePosition, false);
    }

    private void requestRefinement(BigInteger key, boolean immediate) {
        // do not process one request twice in a row
        if (key.equals(latestRequest)) {
            return;
        }
        latestRequest = key;
        RefinementTask task = new RefinementTask(key,
                immediate ? 0L : REFINEMENT_DELAY_MS);
        counter.setTask(task);
    }

    synchronized BigInteger getCursorOrdinal(BasicCursor c) {
        return getCursorOrdinal(c, closedCopy.meta().getColumns().keySet());
    }

    synchronized BigInteger getCursorOrdinal(BasicCursor c, Collection<String> fields) {
        int i = 0;
        Object[] values = c._currentValues();
        KeyEnumerator km;
        for (String cname : fields) {
            km = keyEnumerators.get(cname);
            if (km != null) {
                km.setValue(values[i]);
            }
            i++;
        }
        return rootKeyEnumerator.getOrderValue();
    }

    synchronized void setCursorOrdinal(BasicCursor c, BigInteger key) {
        rootKeyEnumerator.setOrderValue(key);
        for (Map.Entry<String, KeyEnumerator> e : keyEnumerators.entrySet()) {
            c.setValue(e.getKey(), e.getValue().getValue());
        }
    }

    /**
     * Returns scrollbar's knob position for current cursor value.
     */
    public int getTopVisiblePosition() {
        return interpolator.getApproximatePosition(topVisiblePosition);
    }

    private KeyEnumerator createKeyEnumerator(ColumnMeta m, boolean nullsFirst, DBAdaptor dbAdaptor) {
        KeyEnumerator result;

        final String celestaType = m.getCelestaType();
        if (BooleanColumn.CELESTA_TYPE.equals(celestaType)) {
            result = new BitFieldEnumerator();
        } else if (IntegerColumn.CELESTA_TYPE.equals(celestaType)) {
            result = new IntFieldEnumerator();
        } else if (StringColumn.VARCHAR.equals(celestaType)) {
            final int length;
            if (m instanceof StringColumn) {
                StringColumn s = (StringColumn) m;
                length = s.getLength();
            } else {
                ViewColumnMeta vcm = (ViewColumnMeta) m;
                if (vcm.getLength() < 0) {
                    throw new CelestaException(
                            "Undefined length for VARCHAR view field: cannot use it as a key field in a grid.");
                }
                length = vcm.getLength();
            }

            result = new VarcharFieldEnumerator(dbAdaptor, length);
        } else if (DateTimeColumn.CELESTA_TYPE.equals(celestaType)) {
            result = new DateFieldEnumerator();
        } else {
            throw new CelestaException("The field with type '%s' cannot be used as a key field in a grid.",
                    celestaType);
        }

        if (m.isNullable()) {
            result = NullableFieldEnumerator.create(nullsFirst, result);
        }
        return result;
    }

    private void checkMeta(BasicCursor c) {
        if (c.meta() != closedCopy.meta()) {
            throw new CelestaException("Metaobjects for cursor and cursor position specifier don't match.");
        }
    }

    /**
     * Returns (approximate) total record count.
     * <p>
     * Just after creation of this object this method returns DEFAULT_COUNT
     * value, but it asynchronously requests total count right after constructor
     * execution.
     */
    public int getApproxTotalCount() {
        return interpolator.getApproximateCount();
    }

    /**
     * Gets change notifier.
     */
    public Runnable getChangeNotifier() {
        return changeNotifier;
    }

    /**
     * If the grid is scrolled less than for given amount of records, the exact
     * positioning in cycle will be used instead of interpolation.
     *
     * @param smallScroll new value.
     */
    public void setMaxExactScrollValue(int smallScroll) {
        this.smallScroll = smallScroll;
    }

    /**
     * If the grid is scrolled less than for returned number of records, the
     * exact positioning in cycle will be used instead of interpolation.
     */
    public int getMaxExactScrollValue() {
        return smallScroll;
    }

}
