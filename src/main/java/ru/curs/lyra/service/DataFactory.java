package ru.curs.lyra.service;

import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.dbutils.Cursor;
import ru.curs.lyra.dto.DataResult;
import ru.curs.lyra.dto.DataRetrievalParams;
import ru.curs.lyra.dto.Labels;
import ru.curs.lyra.dto.LyraGridAddInfo;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.LyraFieldType;
import ru.curs.lyra.kernel.LyraFieldValue;
import ru.curs.lyra.kernel.LyraFormData;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Build data.
 */
class DataFactory {
    static final String INTERNAL_COLUMN_ADDDATA = "internalAddData";
    private static final String RECVERSION = "recversion";
    private static final String DGRID_NEW_POSITION = "dgridNewPosition";
    private static final String DGRID_NEW_POSITION_ID = "dgridNewPositionId";
    private static final String INTERNAL_COLUMN_ID = "internalId";
    private static final int LYRA_EXACT_TOTALCOUNT_DELTA = 20;

    private BasicGridForm<? extends BasicCursor> basicGridForm;
    private DataRetrievalParams dataRetrievalParams;

    private LyraGridAddInfo lyraGridAddInfo;
    private int position;
    private int lyraApproxTotalCountBeforeGetRows;
    private int lyraApproxTotalCountAfterGetRows;
    private boolean lyraExactTotalCount;
    private int dgridDelta;
    private List<LyraFormData> records;


    /**
     * @param aBasicGridForm       Lyra BasicGridForm
     * @param aDataRetrievalParams DataRetrievalParams
     */
    DataResult buildData(BasicGridForm<? extends BasicCursor> aBasicGridForm,
                         DataRetrievalParams aDataRetrievalParams) {

        init(aBasicGridForm, aDataRetrievalParams);

        setLyraExactTotalCount();

        setRecords();

        setTotalCount();

        printLog();

        setLyraGridAddInfo();

        return setDataResult();

    }


    private void init(BasicGridForm<? extends BasicCursor> aBasicGridForm,
                      DataRetrievalParams aDataRetrievalParams) {
        this.basicGridForm = aBasicGridForm;
        this.dataRetrievalParams = aDataRetrievalParams;

        if (dataRetrievalParams.isSortingOrFilteringChanged()) {
            ((LyraGridScrollBack) basicGridForm.getChangeNotifier())
                    .setLyraGridAddInfo(new LyraGridAddInfo());
        }

        lyraGridAddInfo =
                ((LyraGridScrollBack) basicGridForm.getChangeNotifier()).getLyraGridAddInfo();
        lyraApproxTotalCountBeforeGetRows = basicGridForm.getApproxTotalCount();
        dgridDelta = dataRetrievalParams.getOffset() - dataRetrievalParams.getDgridOldPosition();
    }

    private void setLyraExactTotalCount() {
        lyraExactTotalCount = false;

        if (lyraApproxTotalCountBeforeGetRows < basicGridForm.getGridHeight() + LYRA_EXACT_TOTALCOUNT_DELTA) {

            final int[] array = new int[1];
            basicGridForm.externalAction(c -> {
                array[0] = c.count();
                return null;
            }, null);

            lyraApproxTotalCountBeforeGetRows = array[0];

            lyraExactTotalCount = true;
        }
    }


    private void setRecords() {

        if (dataRetrievalParams.isFirstLoading()) {

            if (dataRetrievalParams.getSelectKey() == null) {
                records = basicGridForm.getRows(0);
            } else {
                if (dataRetrievalParams.isSortingOrFilteringChanged()) {
                    basicGridForm.getRows(0);
                }
                records = basicGridForm.setPosition(dataRetrievalParams.getSelectKey());
            }

        } else {

            if (dataRetrievalParams.getRefreshId() == null) {

                if (dataRetrievalParams.getOffset() == 0) {

                    position = 0;

                } else {

                    if (lyraApproxTotalCountBeforeGetRows <= LyraGridScrollBack.DGRID_MAX_TOTALCOUNT) {

                        position = dataRetrievalParams.getOffset();

                    } else {

                        if (Math.abs(dgridDelta) < LyraGridScrollBack.DGRID_SMALLSTEP) {

                            position = basicGridForm.getTopVisiblePosition() + dgridDelta;

                        } else {

                            if (Math.abs(dataRetrievalParams.getOffset()
                                    - LyraGridScrollBack.DGRID_MAX_TOTALCOUNT) < LyraGridScrollBack.DGRID_SMALLSTEP) {

                                position = lyraApproxTotalCountBeforeGetRows - dataRetrievalParams.getLimit();

                            } else {

                                double d = lyraApproxTotalCountBeforeGetRows;
                                d = d / LyraGridScrollBack.DGRID_MAX_TOTALCOUNT;
                                d = d * dataRetrievalParams.getOffset();
                                position = (int) d;

                            }

                        }

                    }

                }

                records = basicGridForm.getRows(position);
            } else {
                records = basicGridForm.setPosition(dataRetrievalParams.getRefreshId());
            }

        }

    }

    private void setTotalCount() {
        if (lyraExactTotalCount) {
            lyraApproxTotalCountAfterGetRows = lyraApproxTotalCountBeforeGetRows;
        } else {
            lyraApproxTotalCountAfterGetRows = basicGridForm.getApproxTotalCount();
        }

        if (records.size() < dataRetrievalParams.getLimit()) {
            dataRetrievalParams.setTotalCount(records.size());
        } else {
            if (lyraApproxTotalCountAfterGetRows <= LyraGridScrollBack.DGRID_MAX_TOTALCOUNT) {
                dataRetrievalParams.setTotalCount(lyraApproxTotalCountAfterGetRows);
            } else {
                dataRetrievalParams.setTotalCount(LyraGridScrollBack.DGRID_MAX_TOTALCOUNT);
            }
        }
    }

    private void printLog() {
        System.out.println("LyraGridDataFactory.ddddddddddddd1");
        System.out.println("className: " + basicGridForm.getClass().getSimpleName());
        System.out.println("date: " + LocalDateTime.now());
        System.out.println("params.isFirstLoading(): " + dataRetrievalParams.isFirstLoading());
        System.out.println("position: " + position);
        System.out.println("lyraNewPosition: " + basicGridForm.getTopVisiblePosition());
        System.out.println("lyraOldPosition: " + lyraGridAddInfo.getLyraOldPosition());
        System.out.println("lyraExactTotalCount: " + lyraExactTotalCount);
        System.out.println(
                "lyraApproxTotalCountBeforeGetRows: " + lyraApproxTotalCountBeforeGetRows);
        System.out
                .println("lyraApproxTotalCountAfterGetRows: " + lyraApproxTotalCountAfterGetRows);
        System.out.println(
                "basicGridForm.getApproxTotalCount: " + basicGridForm.getApproxTotalCount());
        System.out.println("records.size(): " + records.size());
        System.out.println("dGridLimit(): " + dataRetrievalParams.getLimit());
        System.out.println("dGridTotalCount: " + dataRetrievalParams.getTotalCount());
    }

    private void setLyraGridAddInfo() {
        lyraGridAddInfo.setLyraOldPosition(basicGridForm.getTopVisiblePosition());
        lyraGridAddInfo.setDgridOldTotalCount(dataRetrievalParams.getTotalCount());
    }


    private DataResult setDataResult() {
        List<Map<String, Object>> data = new ArrayList<>();

        int from;
        int length;

        if (dgridDelta > 0) {
            from = Math.max(records.size() - dataRetrievalParams.getLimit(), 0);
            length = records.size();
        } else {
            from = 0;
            length = Math.min(records.size(), dataRetrievalParams.getLimit());
        }

        for (int i = from; i < length; i++) {
            LyraFormData rec = records.get(i);

            Map<String, Object> obj = new HashMap<>();
            for (LyraFieldValue lyraFieldValue : rec.getFields()) {

                if (BasicGridForm.PROPERTIES.equalsIgnoreCase(lyraFieldValue.getName())) {
                    obj.put(BasicGridForm.PROPERTIES, lyraFieldValue.getValue());
                } else {
                    Object objValue;
                    switch (lyraFieldValue.meta().getType()) {
                        case DATETIME:
                            objValue = getStringValueOfDate(lyraFieldValue);
                            break;
                        case INT:
                        case REAL:
                            objValue = getStringValueOfNumber(lyraFieldValue);
                            break;
                        default:
                            objValue = lyraFieldValue.getValue();
                            break;
                    }
                    obj.put(lyraFieldValue.getName(), objValue);
                }

            }

            obj.put(INTERNAL_COLUMN_ID, rec.getKeyValues());
            obj.put(RECVERSION, String.valueOf(rec.getRecversion()));

            data.add(obj);

        }


        // Record Key Positioning
        if (dataRetrievalParams.isFirstLoading() && (data.size() > 0)
                && (basicGridForm.getTopVisiblePosition() > 0)) {

            double d = basicGridForm.getTopVisiblePosition();
            d = (d / lyraApproxTotalCountAfterGetRows)
                    * lyraGridAddInfo.getDgridOldTotalCount();
            int dgridNewPosition = (int) d;
            data.get(0).put(DGRID_NEW_POSITION, dgridNewPosition);

            basicGridForm.externalAction(c -> {
                Object[] keyValues = ((Cursor) c).getCurrentKeyValues();
                data.get(0).put(DGRID_NEW_POSITION_ID, keyValues);
                return null;
            }, null);

        }


        DataResult dataResult = new DataResult();

        Labels labels = new Labels();
        labels.setHeader(basicGridForm.getFormProperties().getHeader());
        labels.setFooter(basicGridForm.getFormProperties().getFooter());
        if (data.size() > 0) {
            data.get(0).put(INTERNAL_COLUMN_ADDDATA, labels);
            dataResult.setData(data);
        } else {
            Map<String, Labels> objAddData = new HashMap<>();
            objAddData.put(INTERNAL_COLUMN_ADDDATA, labels);
            dataResult.setObjAddData(objAddData);
        }

        return dataResult;
    }

    private String getStringValueOfDate(LyraFieldValue lyraFieldValue) {
        if (lyraFieldValue.getValue() == null) {
            return null;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat(lyraFieldValue.meta().getDateFormat());
        return dateFormat.format(lyraFieldValue.getValue());
    }

    private String getStringValueOfNumber(LyraFieldValue lyraFieldValue) {
        if (lyraFieldValue.getValue() == null) {
            return null;
        }

        NumberFormat nf = NumberFormat.getNumberInstance();
        if (lyraFieldValue.meta().getType() == LyraFieldType.INT) {
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(0);
        } else {
            nf.setMinimumFractionDigits(lyraFieldValue.meta().getScale());
            nf.setMaximumFractionDigits(lyraFieldValue.meta().getScale());
        }

        String decimalSeparator = lyraFieldValue.meta().getDecimalSeparator();
        String groupingSeparator = lyraFieldValue.meta().getGroupingSeparator();
        if ((decimalSeparator != null) || (groupingSeparator != null)) {
            DecimalFormat df = (DecimalFormat) nf;
            DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
            if (decimalSeparator != null) {
                dfs.setDecimalSeparator(decimalSeparator.charAt(0));
            }
            if (groupingSeparator != null) {
                if (groupingSeparator.isEmpty()) {
                    nf.setGroupingUsed(false);
                } else {
                    dfs.setGroupingSeparator(groupingSeparator.charAt(0));
                }
            }
            df.setDecimalFormatSymbols(dfs);
        }

        return nf.format(lyraFieldValue.getValue());

    }


    boolean isLyraExactTotalCount() {
        return lyraExactTotalCount;
    }

    BasicGridForm<? extends BasicCursor> getBasicGridForm() {
        return basicGridForm;
    }

    int getLyraApproxTotalCountBeforeGetRows() {
        return lyraApproxTotalCountBeforeGetRows;
    }


}
