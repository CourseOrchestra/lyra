package ru.curs.lyra.service;

import org.junit.jupiter.api.Test;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celestaunit.CelestaTest;
import ru.curs.lyra.dto.Column;
import ru.curs.lyra.dto.Common;
import ru.curs.lyra.dto.FormInstantiationParams;
import ru.curs.lyra.dto.MetaDataResult;
import ru.curs.lyra.kernel.BasicGridForm;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@CelestaTest
class MetadataFactoryTest {

    private final FormFactory formFactory = new FormFactory();
    private final MetadataFactory metadataFactory = new MetadataFactory();

    @Test
    void buildMetadataDefaultFormProperties(CallContext ctx) {
        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestMetadataDefaultPropertiesForm", "foo");
        formFactory.clearForms();
        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParams, null);
        MetaDataResult metaDataResult = metadataFactory.buildMetadata(basicGridForm);

        Common common = metaDataResult.getCommon();
        assertEquals("95%", common.getGridWidth());
        assertEquals("400px", common.getGridHeight());
        assertEquals(50, common.getLimit());
        assertEquals("RECORDS", common.getSelectionModel());
        assertTrue(common.isVisibleColumnsHeader());
        assertTrue(common.isAllowTextSelection());
        assertNull(common.getSummaryRow());
    }

    @Test
    void buildMetadataDefaultColumnProperties(CallContext ctx) {
        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestMetadataDefaultPropertiesForm", "foo");
        formFactory.clearForms();
        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParams, null);
        MetaDataResult metaDataResult = metadataFactory.buildMetadata(basicGridForm);

        Column column = metaDataResult.getColumns().get("1");
        assertEquals("id", column.getId());
        assertTrue(column.isVisible());
        assertEquals("lyra-type-int", column.getCssClassName());
        assertNull(column.getCssStyle());

        column = metaDataResult.getColumns().get("2");
        assertEquals("name", column.getId());
        assertTrue(column.isVisible());
        assertEquals("lyra-type-varchar", column.getCssClassName());
        assertNull(column.getCssStyle());
    }


    @Test
    void buildMetadataDefaultUnboundColumnProperties(CallContext ctx) {
        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestMetadataDefaultPropertiesForm", "foo");
        formFactory.clearForms();
        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParams, null);
        MetaDataResult metaDataResult = metadataFactory.buildMetadata(basicGridForm);

        Column column = metaDataResult.getColumns().get("5");
        assertEquals("unboundField1", column.getId());
        assertTrue(column.isVisible());
        assertEquals("lyra-type-real", column.getCssClassName());
        assertEquals("", column.getCssStyle());

        column = metaDataResult.getColumns().get("6");
        assertEquals("unboundField2", column.getId());
        assertTrue(column.isVisible());
        assertEquals("lyra-type-datetime", column.getCssClassName());
        assertEquals("", column.getCssStyle());
    }


    @Test
    void buildMetadataCommon(CallContext ctx) {
        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestMetadataForm", "foo");
        formFactory.clearForms();
        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParams, null);
        MetaDataResult metaDataResult = metadataFactory.buildMetadata(basicGridForm);

        Common common = metaDataResult.getCommon();
        assertEquals("500px", common.getGridWidth());
        assertEquals("470px", common.getGridHeight());
        assertEquals(10, common.getLimit());
        assertFalse(common.isVisibleColumnsHeader());
        assertFalse(common.isAllowTextSelection());
        assertEquals("id", common.getPrimaryKey());

        Map<String, String> sum = common.getSummaryRow();
        assertNotNull(sum);
        assertEquals(2, sum.size());
        assertEquals("ID", sum.get("id"));
        assertEquals("NAME", sum.get("name"));
    }


    @Test
    void buildMetadataColumns(CallContext ctx) {
        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestMetadataForm", "foo");
        formFactory.clearForms();
        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParams, null);
        MetaDataResult metaDataResult = metadataFactory.buildMetadata(basicGridForm);

        assertEquals(6, metaDataResult.getColumns().size());

        Column column = metaDataResult.getColumns().get("1");
        assertEquals("id", column.getId());
        assertEquals("id", column.getCaption());
        assertTrue(column.isVisible());
        assertEquals("lyra-type-int className1", column.getCssClassName());
        assertEquals("width:100px;text-align:right;", column.getCssStyle());

        column = metaDataResult.getColumns().get("2");
        assertEquals("name", column.getId());
        assertEquals("name field caption", column.getCaption());
        assertFalse(column.isVisible());
        assertEquals("lyra-type-varchar className2", column.getCssClassName());
        assertEquals("width:300px;text-align:left;", column.getCssStyle());
    }


    @Test
    void buildMetadataUnboundColumns(CallContext ctx) {
        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestMetadataForm", "foo");
        formFactory.clearForms();
        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParams, null);
        MetaDataResult metaDataResult = metadataFactory.buildMetadata(basicGridForm);

        assertEquals(6, metaDataResult.getColumns().size());

        Column column = metaDataResult.getColumns().get("5");
        assertEquals("unboundField1", column.getId());
        assertEquals("REAL", column.getCaption());
        assertTrue(column.isVisible());
        assertEquals("lyra-type-real", column.getCssClassName());
        assertEquals("white-space:nowrap;width:100px;text-align:right;", column.getCssStyle());

        column = metaDataResult.getColumns().get("6");
        assertEquals("unboundField2", column.getId());
        assertEquals("DATETIME", column.getCaption());
        assertFalse(column.isVisible());
        assertEquals("lyra-type-datetime", column.getCssClassName());
        assertEquals("white-space:nowrap;width:70px;text-align:center;", column.getCssStyle());
    }


}



