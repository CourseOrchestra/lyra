package ru.curs.lyra.service;

import org.json.JSONObject;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.score.Index;
import ru.curs.celesta.score.Table;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.LyraFormField;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Build metadata.
 */
class MetadataFactory {
    static final String COMMON = "common";
    static final String COLUMNS = "columns";
    private static final String LIMIT = "limit";
    private static final String GRID_WIDTH_DEF_VALUE = "95%";
    private static final String GRID_HEIGHT_DEF_VALUE = "400px";
    static final String GRID_WIDTH = "gridWidth";
    private static final String GRID_HEIGHT = "gridHeight";
    private static final String TOTAL_COUNT = "totalCount";
    private static final String SELECTION_MODEL = "selectionModel";
    private static final String RECORDS = "RECORDS";
    private static final String IS_VISIBLE_COLUMNS_HEADER = "isVisibleColumnsHeader";
    private static final String IS_ALLOW_TEXT_SELECTION = "isAllowTextSelection";
    private static final String PRIMARY_KEY = "primaryKey";
    private static final String SUMMARY_ROW = "summaryRow";
    private static final String ID = "id";
    private static final String CAPTION = "caption";
    private static final String VISIBLE = "visible";
    private static final String CSS_CLASS_NAME = "cssClassName";
    private static final String CSS_STYLE = "cssStyle";
    private static final String CSS_LYRA_TYPE = "lyra-type-";
    private static final String SORTING_AVAILABLE = "sortingAvailable";


    /**
     * Get metadata for the given form.
     *
     * @param basicGridForm Lyra BasicGridForm
     */
    JSONObject buildMetadata(BasicGridForm<? extends BasicCursor> basicGridForm) {

        JSONObject metadata = new JSONObject();

        JSONObject common = new JSONObject();
        common.put(GRID_WIDTH, Optional.ofNullable(basicGridForm.getFormProperties().getGridwidth()).filter(s -> !s.isEmpty()).orElse(GRID_WIDTH_DEF_VALUE));
        common.put(GRID_HEIGHT, Optional.ofNullable(basicGridForm.getFormProperties().getGridheight()).filter(s -> !s.isEmpty()).orElse(GRID_HEIGHT_DEF_VALUE));
        common.put(LIMIT, String.valueOf(basicGridForm.getGridHeight()));
        common.put(TOTAL_COUNT, basicGridForm.getApproxTotalCount());
        common.put(SELECTION_MODEL, RECORDS);

        common.put(IS_VISIBLE_COLUMNS_HEADER, basicGridForm.getFormProperties().getVisibleColumnsHeader());
        common.put(IS_ALLOW_TEXT_SELECTION, basicGridForm.getFormProperties().getAllowTextSelection());

        if (basicGridForm.meta() instanceof Table) {
            Object[] arr = ((Table) basicGridForm.meta()).getPrimaryKey().keySet().toArray();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) {
                    sb.append(",");
                }
                sb.append(arr[i]);
            }

            common.put(PRIMARY_KEY, sb);
        }

        String summaryRow = basicGridForm.getSummaryRow();
        if (summaryRow != null) {
            common.put(SUMMARY_ROW, summaryRow);
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

            if (BasicGridForm.PROPERTIES.equals(field.getName())) {
                continue;
            }

            JSONObject column = new JSONObject();
            column.put(ID, field.getName());
            column.put(CAPTION, field.getCaption());

            column.put(VISIBLE, field.isVisible());

            column.put(CSS_CLASS_NAME, CSS_LYRA_TYPE + field.getType().toString().toLowerCase()
                    + Optional.ofNullable(field.getCssClassName()).map(s -> s.isEmpty() ? "" : " " + s).orElse(""));
            column.put(CSS_STYLE, field.getCssStyle());

            if (lyraGridAvailableSorting.contains(field.getName())) {
                column.put(SORTING_AVAILABLE, true);
            }

            columns.put(String.valueOf(++count), column);

        }

        metadata.put(COLUMNS, columns);

        return metadata;

    }


}
