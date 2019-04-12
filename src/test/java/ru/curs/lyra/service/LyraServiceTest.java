package ru.curs.lyra.service;

import foo.FooCursor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import ru.curs.celesta.CallContext;
import ru.curs.celestaunit.CelestaTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@CelestaTest
class LyraServiceTest {
    private LyraService srv = new LyraService(null);

    @Test
    void getMetadata(CallContext ctx) {
        FormInstantiationParameters ip = new FormInstantiationParameters("ru.curs.lyra.service.forms.TestForm", "foo");
        JSONObject metadata = srv.getMetadata(ctx, ip);
        System.out.println(metadata.toString());
        assertEquals("95%", metadata.getJSONObject(MetadataFactory.COMMON).get(MetadataFactory.GRID_WIDTH));
    }

    @Test
    void getMetadataCssClassNameByFieldType(CallContext ctx) {
        FormInstantiationParameters ip = new FormInstantiationParameters("ru.curs.lyra.service.forms.TestForm", "foo");
        JSONObject metadata = srv.getMetadata(ctx, ip);
        System.out.println(metadata.toString());
        JSONObject columns = metadata.getJSONObject(MetadataFactory.COLUMNS);
        assertEquals("lyra-type-varchar", columns.getJSONObject("1").getString("cssClassName"));
        assertEquals("lyra-type-int", columns.getJSONObject("3").getString("cssClassName"));
        assertEquals("lyra-type-datetime", columns.getJSONObject("6").getString("cssClassName"));
    }

    @Test
    void getTwoRecordsData(CallContext ctx) {
        Map<String, String> clientParams = new HashMap<>();
        clientParams.put(DataFactory.CONTEXT, "{\"refreshParams\": {\"selectKey\": \"\",\"sort\": \"name,id\"}}");
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
        JSONArray data = (JSONArray) srv.getData(ctx, ip, drp);

        assertEquals(2, data.length());
        JSONObject rec = data.getJSONObject(0);
        assertEquals(1, rec.getInt("id"));
        assertEquals("Name", rec.getString("name"));
        rec = data.getJSONObject(1);
        assertEquals(2, rec.getInt("id"));
        assertEquals("Name 2", rec.getString("name"));

    }
}
