package ru.curs.lyra.service;

import foo.FooCursor;
import org.junit.jupiter.api.Test;
import ru.curs.celesta.CallContext;
import ru.curs.celestaunit.CelestaTest;
import ru.curs.lyra.dto.DataResult;
import ru.curs.lyra.dto.MetaDataResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CelestaTest
class LyraServiceTest {
    static final String CONTEXT = "context";

    private LyraService srv = new LyraService(null);

    @Test
    void getMetadata(CallContext ctx) {
        FormInstantiationParameters ip = new FormInstantiationParameters("ru.curs.lyra.service.forms.TestForm", "foo");
        MetaDataResult metadata = srv.getMetadata(ctx, ip);
        assertEquals("95%", metadata.getCommon().getGridWidth());
    }

    @Test
    void getMetadataCssClassNameByFieldType(CallContext ctx) {
        FormInstantiationParameters ip = new FormInstantiationParameters("ru.curs.lyra.service.forms.TestForm", "foo");
        MetaDataResult metadata = srv.getMetadata(ctx, ip);
        assertEquals("lyra-type-varchar", metadata.getColumns().get("1").getCssClassName());
        assertEquals("lyra-type-int", metadata.getColumns().get("3").getCssClassName());
        assertEquals("lyra-type-datetime", metadata.getColumns().get("6").getCssClassName());
    }


    @Test
    void getTwoRecordsData(CallContext ctx) {
        Map<String, String> clientParams = new HashMap<>();
        clientParams.put(CONTEXT, "{\"refreshParams\": {\"selectKey\": \"\",\"sort\": \"name,id\"}}");
        FormInstantiationParameters ip = new FormInstantiationParameters("ru.curs.lyra.service.forms.TestParameterizedForm", "foo", clientParams);
        DataRetrievalParams drp = new DataRetrievalParams();
        drp.setLimit(50);
        FooCursor fooCursor = new FooCursor(ctx);
        fooCursor.setId(1);
        fooCursor.setName("Name");
        fooCursor.insert();

        fooCursor.setId(2);
        fooCursor.setName("Name 2");
        fooCursor.insert();

        DataResult dataResult = srv.getData(ctx, ip, drp);

        assertEquals(2, dataResult.getData().size());
        Map<String, Object> rec = dataResult.getData().get(0);
        assertEquals("1", rec.get("id"));
        assertEquals("Name", rec.get("name"));
        rec = dataResult.getData().get(1);
        assertEquals("2", rec.get("id"));
        assertEquals("Name 2", rec.get("name"));

    }

}
