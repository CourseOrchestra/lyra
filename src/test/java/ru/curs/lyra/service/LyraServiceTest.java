package ru.curs.lyra.service;

import foo.FooCursor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.curs.celesta.CallContext;
import ru.curs.celestaunit.CelestaUnitExtension;

import static org.junit.jupiter.api.Assertions.*;


class LyraServiceTest {
    public static final String SCORE_PATH = "src/test/resources/score";
    @RegisterExtension
    static CelestaUnitExtension ext =
            CelestaUnitExtension.builder()
                    .withScorePath(SCORE_PATH).build();
    private LyraService srv = new LyraService(null);

    @Test
    void getMetadata(CallContext ctx) {
        FormInstantiationParameters ip = new FormInstantiationParameters("ru.curs.lyra.service.TestForm", "foo");
        JSONObject metadata = srv.getMetadata(ctx, ip);
        System.out.println(metadata.toString());
        assertEquals("95%", metadata.getJSONObject(LyraService.COMMON).get(LyraService.GRID_WIDTH));
    }

    @Test
    void getData(CallContext ctx){
        FormInstantiationParameters ip = new FormInstantiationParameters("ru.curs.lyra.service.TestParameterizedForm", "foo");
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

        System.out.println(data.get(1));
    }
}