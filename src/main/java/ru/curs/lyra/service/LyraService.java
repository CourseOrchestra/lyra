package ru.curs.lyra.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.CelestaException;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.dbutils.Cursor;
import ru.curs.celesta.score.Index;
import ru.curs.celesta.score.Table;
import ru.curs.celesta.transaction.CelestaTransaction;
import ru.curs.lyra.dto.DataParams;
import ru.curs.lyra.dto.LyraGridAddInfo;
import ru.curs.lyra.dto.MetaDataParams;
import ru.curs.lyra.dto.ScrollBackParams;
import ru.curs.lyra.kernel.*;

import java.lang.reflect.Constructor;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class LyraService {

    private static final String STRING_SELECTED_RECORD_IDS_SEPARATOR = "D13&82#9g7";
    private static final String KEYVALUES_SEPARATOR = "_D13k82F9g7_";
    private static final String ADDDATA_COLUMN = "addData" + KEYVALUES_SEPARATOR;

    private static final String GRID_WIDTH_DEF_VALUE = "95%";
    private static final String GRID_HEIGHT_DEF_VALUE = "400px";

    private Map<String, BasicGridForm<? extends BasicCursor>> forms = new HashMap<>();

    private SimpMessageSendingOperations messagingTemplate;

    public LyraService(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }


    private String getDgridId(String formClass, String instanceId) {
        return formClass + "." + instanceId;
    }


    private <T extends BasicCursor> BasicGridForm<T> getFormInstance(LyraCallContext callContext, String formClass, String instanceId) throws Exception {
        String dgridId = getDgridId(formClass, instanceId);
        @SuppressWarnings("unchecked")
        BasicGridForm<T> form = (BasicGridForm<T>) forms.computeIfAbsent(dgridId, key -> getBasicGridFormInstance(callContext, formClass, dgridId));
        form.setCallContext(callContext);
        return form;
    }

    private <T extends BasicCursor> BasicGridForm<T> getBasicGridFormInstance(LyraCallContext callContext, String formClass, String dgridId) throws CelestaException {
        try {
            Class<?> clazz = Class.forName(formClass);
            Constructor<?> constructor = clazz.getConstructor(CallContext.class);
            Object instance = constructor.newInstance(callContext);
            @SuppressWarnings("unchecked")
            BasicGridForm<T> form = (BasicGridForm<T>) instance;
            final int maxExactScrollValue = 120;
            form.setMaxExactScrollValue(maxExactScrollValue);
            LyraGridScrollBack scrollBack = new LyraGridScrollBack(this, dgridId);
            scrollBack.setBasicGridForm(form);
            form.setChangeNotifier(scrollBack);
            forms.put(dgridId, form);
            return form;
        } catch (Exception e) {
            throw new CelestaException(e);
        }
    }


    @CelestaTransaction
    public String getMetadata(LyraCallContext callContext, MetaDataParams params) throws Exception {

        BasicGridForm<? extends BasicCursor> basicGridForm = getFormInstance(callContext, params.getFormClass(), params.getInstanceId());

        JSONObject metadata = new JSONObject();

        JSONObject common = new JSONObject();
        common.put("gridWidth", Optional.ofNullable(basicGridForm.getFormProperties().getGridwidth()).filter(s -> !s.isEmpty()).orElse(GRID_WIDTH_DEF_VALUE));
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

        metadata.put("common", common);

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

        return metadata.toString();

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
    public String getData(LyraCallContext callContext, DataParams params) throws Exception {

        BasicGridForm<? extends BasicCursor> basicGridForm = getFormInstance(callContext, params.getFormClass(), params.getInstanceId());

/*
        if (basicGridForm.getChangeNotifier() == null) {
            throw new ValidateException(new UserMessage(
                    "Внимание! Произошло обновление скриптов решения. Для корректной работы необходимо перегрузить грид.",
                    MessageType.INFO, "Сообщение"));
        }
*/


        if (params.isSortingOrFilteringChanged()) {
            ((LyraGridScrollBack) basicGridForm.getChangeNotifier())
                    .setLyraGridAddInfo(new LyraGridAddInfo());
        }


        // ---------------------------------------------------


        LyraGridAddInfo lyraGridAddInfo =
                ((LyraGridScrollBack) basicGridForm.getChangeNotifier()).getLyraGridAddInfo();

        lyraGridAddInfo.setExcelExportType(null);


        int position = -1;
        int lyraApproxTotalCount = basicGridForm.getApproxTotalCount();
        int dgridDelta = params.getOffset() - params.getDgridOldPosition();


        List<LyraFormData> records;


        if (params.isFirstLoading()) {

            JSONObject json = new JSONObject(params.getContext());
            String selectKey = (String) ((JSONObject) json.get("refreshParams")).get("selectKey");

            if ((selectKey == null) || selectKey.trim().isEmpty()) {
                records = basicGridForm.getRows(0);
            } else {
                if (params.isSortingOrFilteringChanged()) {
                    basicGridForm.getRows(0);
                }
                records = basicGridForm.setPosition(getKeyValuesById(selectKey));
            }

        } else {

            if (params.getRefreshId() == null) {

                if (params.getOffset() == 0) {

                    position = 0;

                } else {

                    if (lyraApproxTotalCount <= LyraGridScrollBack.DGRID_MAX_TOTALCOUNT) {

                        position = params.getOffset();

                    } else {

                        if (Math.abs(dgridDelta) < LyraGridScrollBack.DGRID_SMALLSTEP) {

                            position = basicGridForm.getTopVisiblePosition() + dgridDelta;

                        } else {

                            if (Math.abs(params.getOffset()
                                    - LyraGridScrollBack.DGRID_MAX_TOTALCOUNT) < LyraGridScrollBack.DGRID_SMALLSTEP) {

                                position = lyraApproxTotalCount - params.getLimit();

                            } else {

                                double d = lyraApproxTotalCount;
                                d = d / LyraGridScrollBack.DGRID_MAX_TOTALCOUNT;
                                d = d * params.getOffset();
                                position = (int) d;

                            }

                        }

                    }

                }

                records = basicGridForm.getRows(position);
            } else {
                records = basicGridForm.setPosition(getKeyValuesById(params.getRefreshId()));
            }

        }


        if (records.size() < params.getLimit()) {
            params.setTotalCount(records.size());
        } else {
            if (basicGridForm.getApproxTotalCount() <= LyraGridScrollBack.DGRID_MAX_TOTALCOUNT) {
                params.setTotalCount(basicGridForm.getApproxTotalCount());
            } else {
                params.setTotalCount(LyraGridScrollBack.DGRID_MAX_TOTALCOUNT);
            }
        }


        System.out.println("LyraGridDataFactory.ddddddddddddd1");
        System.out.println("className: " + basicGridForm.getClass().getSimpleName());
        System.out.println("date: " + LocalDateTime.now());
        System.out.println("params.isFirstLoading(): " + params.isFirstLoading());
        System.out.println("position: " + position);
        System.out.println("lyraNewPosition: " + basicGridForm.getTopVisiblePosition());
        System.out.println("lyraOldPosition: " + lyraGridAddInfo.getLyraOldPosition());
        System.out.println("lyraApproxTotalCount(before getRows): " + lyraApproxTotalCount);
        System.out.println(
                "getApproxTotalCount(after getRows): " + basicGridForm.getApproxTotalCount());
        System.out.println("records.size(): " + records.size());
        System.out.println("dGridLimit(): " + params.getLimit());
        System.out.println("dGridTotalCount: " + params.getTotalCount());

        lyraGridAddInfo.setLyraOldPosition(basicGridForm.getTopVisiblePosition());
        lyraGridAddInfo.setDgridOldTotalCount(params.getTotalCount());


        // --------------------------------------------------------


        JSONArray data = new JSONArray();

        int length = Math.min(records.size(), params.getLimit());
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
        if (params.isFirstLoading() && (data.length() > 0)
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

        String ret = data.toString();
        if ((data.length() == 0) && (objAddData != null)) {
            ret = objAddData.toString();
        }
        return ret;

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
