package ru.curs.lyra.service;

import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.score.Index;
import ru.curs.celesta.score.Table;
import ru.curs.lyra.dto.Column;
import ru.curs.lyra.dto.Common;
import ru.curs.lyra.dto.MetaDataResult;
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
    private static final String GRID_WIDTH_DEF_VALUE = "95%";
    private static final String GRID_HEIGHT_DEF_VALUE = "400px";
    private static final String RECORDS = "RECORDS";
    private static final String CSS_LYRA_TYPE = "lyra-type-";


    /**
     * Get metadata for the given form.
     *
     * @param basicGridForm Lyra BasicGridForm
     */
    MetaDataResult buildMetadata(BasicGridForm<? extends BasicCursor> basicGridForm) {

        MetaDataResult metadata = new MetaDataResult();

        Common common = new Common();
        common.setGridWidth(Optional.ofNullable(basicGridForm.getFormProperties().getGridwidth()).filter(s -> !s.isEmpty()).orElse(GRID_WIDTH_DEF_VALUE));
        common.setGridHeight(Optional.ofNullable(basicGridForm.getFormProperties().getGridheight()).filter(s -> !s.isEmpty()).orElse(GRID_HEIGHT_DEF_VALUE));
        common.setLimit(basicGridForm.getGridHeight());
        common.setTotalCount(basicGridForm.getApproxTotalCount());
        common.setSelectionModel(RECORDS);
        common.setVisibleColumnsHeader(basicGridForm.getFormProperties().getVisibleColumnsHeader());
        common.setAllowTextSelection(basicGridForm.getFormProperties().getAllowTextSelection());

        if (basicGridForm.meta() instanceof Table) {
            common.setPrimaryKey(((Table) basicGridForm.meta()).getPrimaryKey().keySet().stream().toArray(String[]::new));
        }

        common.setSummaryRow(basicGridForm.getSummaryRow());

        metadata.setCommon(common);


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
        for (LyraFormField field : lyraFields.values()) {

            if (BasicGridForm.PROPERTIES.equals(field.getName())) {
                continue;
            }

            Column column = new Column();
            column.setId(field.getName());
            column.setCaption(field.getCaption());
            column.setVisible(field.isVisible());
            column.setSortable(field.isSortable());
            column.setCssClassName(CSS_LYRA_TYPE + field.getType().toString().toLowerCase()
                    + Optional.ofNullable(field.getCssClassName()).map(s -> s.isEmpty() ? "" : " " + s).orElse(""));
            column.setCssStyle(field.getCssStyle());
            column.setSortingAvailable(lyraGridAvailableSorting.contains(field.getName()));

            metadata.getColumns().add(column);

        }

        return metadata;

    }


}
