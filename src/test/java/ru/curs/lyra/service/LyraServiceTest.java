package ru.curs.lyra.service;

import foo.FooCursor;
import org.junit.jupiter.api.Test;
import ru.curs.celesta.CallContext;
import ru.curs.celestaunit.CelestaTest;
import ru.curs.lyra.dto.DataResult;
import ru.curs.lyra.dto.DataRetrievalParams;
import ru.curs.lyra.dto.FormInstantiationParams;
import ru.curs.lyra.dto.MetaDataResult;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CelestaTest
class LyraServiceTest {
    private LyraService srv = new LyraService(null);

    @Test
    void getMetadata(CallContext ctx) {
        FormInstantiationParams ip = new FormInstantiationParams("ru.curs.lyra.service.forms.TestForm", "foo");
        MetaDataResult metadata = srv.getMetadata(ctx, ip);
        assertEquals("95%", metadata.getCommon().getGridWidth());
    }

    @Test
    void getMetadataCssClassNameByFieldType(CallContext ctx) {
        FormInstantiationParams ip = new FormInstantiationParams("ru.curs.lyra.service.forms.TestForm", "foo");
        MetaDataResult metadata = srv.getMetadata(ctx, ip);
        assertEquals("lyra-type-varchar", metadata.getColumns().get(0).getCssClassName());
        assertEquals("lyra-type-int", metadata.getColumns().get(2).getCssClassName());
        assertEquals("lyra-type-datetime", metadata.getColumns().get(5).getCssClassName());
    }


    @Test
    void getTwoRecordsData(CallContext ctx) {
        Map<String, String> refreshParams = new HashMap<>();
        refreshParams.put("selectKey", "");
        refreshParams.put("sort", "name,id");
        Map<String, Object> clientParams = new HashMap<>();
        clientParams.put("refreshParams", refreshParams);
        clientParams.put("part1", "part1");
        clientParams.put("part2", 5);
        FormInstantiationParams ip = new FormInstantiationParams("ru.curs.lyra.service.forms.TestParameterizedForm", "foo", clientParams);
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
