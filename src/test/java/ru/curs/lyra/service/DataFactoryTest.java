package ru.curs.lyra.service;

import foo.FooCursor;
import org.junit.jupiter.api.Test;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celestaunit.CelestaTest;
import ru.curs.lyra.kernel.BasicGridForm;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CelestaTest
class DataFactoryTest {
    private final FormFactory formFactory = new FormFactory();
    private final DataFactory dataFactory = new DataFactory();

    void initTotalCountTests(CallContext ctx, DataRetrievalParams dataRetrievalParams) {

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


        Map<String, String> clientParams = new HashMap<>();
        clientParams.put(DataFactory.CONTEXT, "{\"refreshParams\": {\"selectKey\": \"\",\"sort\": \"name,id\"}}");
        FormInstantiationParameters formInstantiationParameters
                = new FormInstantiationParameters("ru.curs.lyra.service.forms.TestParameterizedForm", "foo", clientParams);

        dataRetrievalParams.setLimit(50);
        dataRetrievalParams.setOffset(0);
        dataRetrievalParams.setDgridOldPosition(0);
        dataRetrievalParams.setSortingOrFilteringChanged(true);
        dataRetrievalParams.setFirstLoading(true);
        dataRetrievalParams.setRefreshId(null);

        formFactory.clearForms();
        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParameters, null);

        dataFactory.setParameters(basicGridForm, formInstantiationParameters, dataRetrievalParams);
        dataFactory.buildData();
        dataFactory.buildData();
    }

    @Test
    void lyraExactTotalCount(CallContext ctx) {
        DataRetrievalParams dataRetrievalParams = new DataRetrievalParams();
        initTotalCountTests(ctx, dataRetrievalParams);

        assertTrue(dataFactory.isLyraExactTotalCount());
        assertEquals(3, dataRetrievalParams.getTotalCount());
    }

    @Test
    void lyraApproxTotalCount(CallContext ctx) {
        DataRetrievalParams dataRetrievalParams = new DataRetrievalParams();
        initTotalCountTests(ctx, dataRetrievalParams);

        assertEquals(1, dataFactory.getBasicGridForm().getApproxTotalCount());
        // expected 3
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

        FormInstantiationParameters formInstantiationParameters
                = new FormInstantiationParameters("ru.curs.lyra.service.forms.TestParameterizedForm", "foo");

        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(ctx, formInstantiationParameters, null);

        basicGridForm.getApproxTotalCount();
        basicGridForm.getRows(0);

        assertEquals(1, basicGridForm.getApproxTotalCount());
        // expected 3

    }


}
