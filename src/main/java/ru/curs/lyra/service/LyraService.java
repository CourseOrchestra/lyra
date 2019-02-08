package ru.curs.lyra.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.dbutils.Cursor;
import ru.curs.celesta.score.Index;
import ru.curs.celesta.score.Table;
import ru.curs.celesta.transaction.CelestaTransaction;
import ru.curs.lyra.dto.LyraGridAddInfo;
import ru.curs.lyra.dto.ScrollBackParams;
import ru.curs.lyra.kernel.*;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class LyraService {

    //TODO: What is THIS?
    private static final String STRING_SELECTED_RECORD_IDS_SEPARATOR = "D13&82#9g7";
    private static final String KEYVALUES_SEPARATOR = "_D13k82F9g7_";
    private static final String ADDDATA_COLUMN = "addData" + KEYVALUES_SEPARATOR;

    private static final String GRID_WIDTH_DEF_VALUE = "95%";
    private static final String GRID_HEIGHT_DEF_VALUE = "400px";
    public static final String GRID_WIDTH = "gridWidth";
    public static final String COMMON = "common";

    private final FormFactory formFactory = new FormFactory();

    private SimpMessageSendingOperations messagingTemplate;

    public LyraService(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }


    //TODO: get rid of transaction here. Maybe this requires changing the API for BasicGridForm
    @CelestaTransaction
    public JSONObject getMetadata(CallContext callContext, FormInstantiationParameters parameters) {


        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(callContext,
                        parameters,
                        this);

        JSONObject metadata = new JSONObject();

        JSONObject common = new JSONObject();
        common.put(GRID_WIDTH, Optional.ofNullable(basicGridForm.getFormProperties().getGridwidth()).filter(s -> !s.isEmpty()).orElse(GRID_WIDTH_DEF_VALUE));
        common.put("gridHeight", Optional.ofNullable(basicGridForm.getFormProperties().getGridheight()).filter(s -> !s.isEmpty()).orElse(GRID_HEIGHT_DEF_VALUE));
        common.put("limit", String.valueOf(basicGridForm.getGridHeight()));
        common.put("totalCount", basicGridForm.getApproxTotalCount());
        common.put("selectionModel", "RECORDS");
        common.put("isVisibleColumnsHeader", basicGridForm.getFormProperties().getVisibleColumnsHeader());
        common.put("isAllowTextSelection", basicGridForm.getFormProperties().getAllowTextSelection());
        common.put("stringSelectedRecordIdsSeparator", STRING_SELECTED_RECORD_IDS_SEPARATOR);
/*
        if (gridMetadata.isNeedCreateWebSocket()) {
            common.put("isNeedCreateWebSocket", "true");
        }
*/
        if (basicGridForm.meta() instanceof Table) {
            Object[] arr = ((Table) basicGridForm.meta()).getPrimaryKey().keySet().toArray();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(arr[i]);
            }

            common.put("primaryKey", sb);
        }

        String summaryRow = basicGridForm.getSummaryRow();
        if (summaryRow != null) {
            common.put("summaryRow", summaryRow);
        }

        metadata.put(COMMON, common);

        List<String> lyraGridAvailableSorting = new ArrayList<>();
        if (basicGridForm.meta() instanceof Table) {
            for (Index index : ((Table) basicGridForm.meta()).getIndices()) {
                if (index.getColumns().size() == 1) {
                    lyraGridAvailableSorting
                            .add((String) index.getColumns().keySet().toArray()[0]);
                }
            }
        }

        Map<String, LyraFormField> lyraFields = basicGridForm.getFieldsMeta();


        int count = 0;
        JSONObject columns = new JSONObject();

        for (LyraFormField field : lyraFields.values()) {

            JSONObject column = new JSONObject();
            column.put("id", field.getName());
            column.put("caption", field.getCaption());

            column.put("visible", field.isVisible());

            column.put("cssClassName", field.getCssClassName());
            column.put("cssStyle", field.getCssStyle());

            if (lyraGridAvailableSorting.contains(field.getName())) {
                column.put("sortingAvailable", "true");
            }

            columns.put(String.valueOf(++count), column);

        }

        metadata.put("columns", columns);

        return metadata;

    }


    private Object[] getKeyValuesById(final String refreshId) {
        return refreshId.split(KEYVALUES_SEPARATOR);
    }

    private String getIdByKeyValues(final Object[] keyValues) {
        StringBuilder refreshId = new StringBuilder();
        for (int i = 0; i < keyValues.length; i++) {
            if (i > 0) {
                refreshId.append(KEYVALUES_SEPARATOR);
            }
            refreshId.append(keyValues[i].toString());
        }
        return refreshId.toString();
    }


    @CelestaTransaction
    public Object getData(CallContext callContext,
                          FormInstantiationParameters formInstantiationParameters,
                          DataRetrievalParams dataRetrievalParams) {
        BasicGridForm<? extends BasicCursor> basicGridForm = formFactory.getFormInstance(callContext,
                formInstantiationParameters,
                this);

/*
        if (basicGridForm.getChangeNotifier() == null) {
            throw new ValidateException(new UserMessage(
                    "Внимание! Произошло обновление скриптов решения. Для корректной работы необходимо перегрузить грид.",
                    MessageType.INFO, "Сообщение"));
        }
*/


        if (dataRetrievalParams.isSortingOrFilteringChanged()) {
            ((LyraGridScrollBack) basicGridForm.getChangeNotifier())
                    .setLyraGridAddInfo(new LyraGridAddInfo());
        }


        // ---------------------------------------------------


        LyraGridAddInfo lyraGridAddInfo =
                ((LyraGridScrollBack) basicGridForm.getChangeNotifier()).getLyraGridAddInfo();

        int position = -1;
        int lyraApproxTotalCount = basicGridForm.getApproxTotalCount();
        int dgridDelta = dataRetrievalParams.getOffset() - dataRetrievalParams.getDgridOldPosition();


        List<LyraFormData> records;


        if (dataRetrievalParams.isFirstLoading()) {

            JSONObject json = new JSONObject(
                    formInstantiationParameters.getClientParams().get("context"));
            String selectKey = (String) ((JSONObject) json.get("refreshParams")).get("selectKey");

            if ((selectKey == null) || selectKey.trim().isEmpty()) {
                records = basicGridForm.getRows(0);
            } else {
                if (dataRetrievalParams.isSortingOrFilteringChanged()) {
                    basicGridForm.getRows(0);
                }
                records = basicGridForm.setPosition(getKeyValuesById(selectKey));
            }

        } else {

            if (dataRetrievalParams.getRefreshId() == null) {

                if (dataRetrievalParams.getOffset() == 0) {

                    position = 0;

                } else {

                    if (lyraApproxTotalCount <= LyraGridScrollBack.DGRID_MAX_TOTALCOUNT) {

                        position = dataRetrievalParams.getOffset();

                    } else {

                        if (Math.abs(dgridDelta) < LyraGridScrollBack.DGRID_SMALLSTEP) {

                            position = basicGridForm.getTopVisiblePosition() + dgridDelta;

                        } else {

                            if (Math.abs(dataRetrievalParams.getOffset()
                                    - LyraGridScrollBack.DGRID_MAX_TOTALCOUNT) < LyraGridScrollBack.DGRID_SMALLSTEP) {

                                position = lyraApproxTotalCount - dataRetrievalParams.getLimit();

                            } else {

                                double d = lyraApproxTotalCount;
                                d = d / LyraGridScrollBack.DGRID_MAX_TOTALCOUNT;
                                d = d * dataRetrievalParams.getOffset();
                                position = (int) d;

                            }

                        }

                    }

                }

                records = basicGridForm.getRows(position);
            } else {
                records = basicGridForm.setPosition(getKeyValuesById(dataRetrievalParams.getRefreshId()));
            }

        }


        if (records.size() < dataRetrievalParams.getLimit()) {
            dataRetrievalParams.setTotalCount(records.size());
        } else {
            if (basicGridForm.getApproxTotalCount() <= LyraGridScrollBack.DGRID_MAX_TOTALCOUNT) {
                dataRetrievalParams.setTotalCount(basicGridForm.getApproxTotalCount());
            } else {
                dataRetrievalParams.setTotalCount(LyraGridScrollBack.DGRID_MAX_TOTALCOUNT);
            }
        }


        System.out.println("LyraGridDataFactory.ddddddddddddd1");
        System.out.println("className: " + basicGridForm.getClass().getSimpleName());
        System.out.println("date: " + LocalDateTime.now());
        System.out.println("params.isFirstLoading(): " + dataRetrievalParams.isFirstLoading());
        System.out.println("position: " + position);
        System.out.println("lyraNewPosition: " + basicGridForm.getTopVisiblePosition());
        System.out.println("lyraOldPosition: " + lyraGridAddInfo.getLyraOldPosition());
        System.out.println("lyraApproxTotalCount(before getRows): " + lyraApproxTotalCount);
        System.out.println(
                "getApproxTotalCount(after getRows): " + basicGridForm.getApproxTotalCount());
        System.out.println("records.size(): " + records.size());
        System.out.println("dGridLimit(): " + dataRetrievalParams.getLimit());
        System.out.println("dGridTotalCount: " + dataRetrievalParams.getTotalCount());

        lyraGridAddInfo.setLyraOldPosition(basicGridForm.getTopVisiblePosition());
        lyraGridAddInfo.setDgridOldTotalCount(dataRetrievalParams.getTotalCount());


        // --------------------------------------------------------


        JSONArray data = new JSONArray();

        int length = Math.min(records.size(), dataRetrievalParams.getLimit());
        for (int i = 0; i < length; i++) {
            LyraFormData rec = records.get(i);

            JSONObject obj = new JSONObject();
            for (LyraFieldValue lyraFieldValue : rec.getFields()) {

                if (BasicGridForm.PROPERTIES.equalsIgnoreCase(lyraFieldValue.getName())) {
                    JSONObject properties = new JSONObject(lyraFieldValue.getValue().toString());
                    Iterator<String> keys = properties.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        obj.put(key, properties.getString(key));
                    }
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

            obj.put("id" + KEYVALUES_SEPARATOR, getIdByKeyValues(rec.getKeyValues()));
            obj.put("recversion", String.valueOf(rec.getRecversion()));

            data.put(obj);

        }


        if ((data.length() > 0) && lyraGridAddInfo.isNeedRecreateWebsocket()) {
            ((JSONObject) data.get(0)).put("needRecreateWebsocket", true);
            lyraGridAddInfo.setNeedRecreateWebsocket(false);
        }

        // Позиционирование по ключу записи
        if (dataRetrievalParams.isFirstLoading() && (data.length() > 0)
                && (basicGridForm.getTopVisiblePosition() > 0)) {

            double d = basicGridForm.getTopVisiblePosition();
            d = (d / basicGridForm.getApproxTotalCount())
                    * lyraGridAddInfo.getDgridOldTotalCount();
            int dgridNewPosition = (int) d;
            ((JSONObject) data.get(0)).put("dgridNewPosition", dgridNewPosition);

            basicGridForm.externalAction(c -> {
                Object[] keyValues = ((Cursor) c).getCurrentKeyValues();
                String recId = getIdByKeyValues(keyValues);
                ((JSONObject) data.get(0)).put("dgridNewPositionId", recId);
                return null;
            }, null);

        }


        JSONObject objAddData = null;
        JSONObject labels = new JSONObject();
        labels.put("header", basicGridForm.getFormProperties().getHeader());
        labels.put("footer", basicGridForm.getFormProperties().getFooter());
        if (data.length() > 0) {
            ((JSONObject) data.get(0)).put(ADDDATA_COLUMN, labels);
        } else {
            objAddData = new JSONObject();
            objAddData.put(ADDDATA_COLUMN, labels);
        }

        if ((data.length() == 0) && (objAddData != null)) {
            return objAddData;
        } else {
            return data;
        }
    }

    private String getStringValueOfDate(LyraFieldValue lyraFieldValue) {
        DateFormat df = DateFormat.getDateTimeInstance(lyraFieldValue.meta().getDateFormat(), lyraFieldValue.meta().getDateFormat());
        return df.format(lyraFieldValue.getValue());
    }

    private String getStringValueOfNumber(LyraFieldValue lyraFieldValue) {

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


    void sendScrollBackPosition(ScrollBackParams params) {
        messagingTemplate.convertAndSend("/position", params);
    }


}
