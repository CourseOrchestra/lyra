package ru.curs.lyra.service;

import foo.FooCursor;
import org.junit.jupiter.api.Test;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celestaunit.CelestaTest;
import ru.curs.lyra.dto.DataResult;
import ru.curs.lyra.dto.DataRetrievalParams;
import ru.curs.lyra.dto.FormInstantiationParams;
import ru.curs.lyra.dto.Labels;
import ru.curs.lyra.kernel.BasicGridForm;
import ru.curs.lyra.kernel.grid.GridDriver;

import static org.junit.jupiter.api.Assertions.*;

@CelestaTest
class DataFactoryTest {
    private final FormFactory formFactory = new FormFactory();
    private final DataFactory dataFactory = new DataFactory();

    void initTotalCountTests(CallContext ctx, DataRetrievalParams dataRetrievalParams) {

        FooCursor fooCursor = new FooCursor(ctx);
        fooCursor.setId(1);
        fooCursor.setName("Name");
        fooCursor.insert();

        fooCursor.setId(20);
        fooCursor.setName("Name2");
        fooCursor.insert();

        fooCursor.setId(3);
        fooCursor.setName("Name3");
        fooCursor.insert();

        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestParameterizedForm", "foo", null);

        dataRetrievalParams.setLimit(50);
        dataRetrievalParams.setOffset(0);
        dataRetrievalParams.setDgridOldPosition(0);
        dataRetrievalParams.setSortingOrFilteringChanged(true);
        dataRetrievalParams.setFirstLoading(true);
        dataRetrievalParams.setRefreshId(null);

        formFactory.clearForms();
        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParams, null);

        dataFactory.buildData(basicGridForm, dataRetrievalParams);
        dataFactory.buildData(basicGridForm, dataRetrievalParams);
    }

    @Test
    void lyraExactTotalCount(CallContext ctx) {
        DataRetrievalParams dataRetrievalParams = new DataRetrievalParams();
        initTotalCountTests(ctx, dataRetrievalParams);

        if (dataFactory.getLyraApproxTotalCountBeforeGetRows() != GridDriver.DEFAULT_COUNT) {
            assertTrue(dataFactory.isLyraExactTotalCount());
            assertEquals(3, dataRetrievalParams.getTotalCount());
        }
    }

    @Test
    void lyraApproxTotalCount(CallContext ctx) {
        DataRetrievalParams dataRetrievalParams = new DataRetrievalParams();
        initTotalCountTests(ctx, dataRetrievalParams);

        if (dataFactory.getBasicGridForm().getApproxTotalCount() != GridDriver.DEFAULT_COUNT) {
            assertEquals(1, dataFactory.getBasicGridForm().getApproxTotalCount());
            // expected 3
        }
    }


    @Test
    void lyraApproxTotalCount2(CallContext ctx) {

        FooCursor fooCursor = new FooCursor(ctx);
        fooCursor.setId(1);
        fooCursor.setName("Name");
        fooCursor.insert();

        fooCursor.setId(2);
        fooCursor.setName("Name2");
        fooCursor.insert();

        fooCursor.setId(3);
        fooCursor.setName("Name3");
        fooCursor.insert();

        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestParameterizedForm", "foo");

        formFactory.clearForms();
        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParams, null);

        basicGridForm.getApproxTotalCount();
        basicGridForm.getRows(0);

        if (basicGridForm.getApproxTotalCount() != GridDriver.DEFAULT_COUNT) {
            assertEquals(1, basicGridForm.getApproxTotalCount());
            // expected 3
        }
    }


    @Test
    void buildDataLabelsWithData(CallContext ctx) {

        FooCursor fooCursor = new FooCursor(ctx);
        fooCursor.setId(1);
        fooCursor.setName("Name");
        fooCursor.insert();

        fooCursor.setId(2);
        fooCursor.setName("Name2");
        fooCursor.insert();

        fooCursor.setId(3);
        fooCursor.setName("Name3");
        fooCursor.insert();

        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestDataForm", "foo");

        formFactory.clearForms();
        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParams, null);

        DataRetrievalParams dataRetrievalParams = new DataRetrievalParams();
        dataRetrievalParams.setLimit(50);
        dataRetrievalParams.setOffset(0);
        dataRetrievalParams.setDgridOldPosition(0);
        dataRetrievalParams.setSortingOrFilteringChanged(true);
        dataRetrievalParams.setFirstLoading(true);
        dataRetrievalParams.setRefreshId(null);

        DataResult dataResult = dataFactory.buildData(basicGridForm, dataRetrievalParams);

        assertNotNull(dataResult.getData());
        assertNull(dataResult.getObjAddData());

        assertTrue(dataResult.getData().get(0).get(DataFactory.INTERNAL_COLUMN_ADDDATA) instanceof Labels);

        Labels labels = (Labels) dataResult.getData().get(0).get(DataFactory.INTERNAL_COLUMN_ADDDATA);
        assertEquals("<h2>Header2</h2>", labels.getHeader());
        assertEquals("<h2>Footer2</h2>", labels.getFooter());

    }


    @Test
    void buildDataLabelsNoData(CallContext ctx) {

        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestDataForm", "foo");

        formFactory.clearForms();
        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParams, null);

        DataRetrievalParams dataRetrievalParams = new DataRetrievalParams();
        dataRetrievalParams.setLimit(50);
        dataRetrievalParams.setOffset(0);
        dataRetrievalParams.setDgridOldPosition(0);
        dataRetrievalParams.setSortingOrFilteringChanged(true);
        dataRetrievalParams.setFirstLoading(true);
        dataRetrievalParams.setRefreshId(null);

        DataResult dataResult = dataFactory.buildData(basicGridForm, dataRetrievalParams);

        assertNotNull(dataResult.getObjAddData());
        assertNull(dataResult.getData());

        Labels labels = dataResult.getObjAddData().get(DataFactory.INTERNAL_COLUMN_ADDDATA);
        assertEquals("<h2>Header2</h2>", labels.getHeader());
        assertEquals("<h2>Footer2</h2>", labels.getFooter());

    }


}
