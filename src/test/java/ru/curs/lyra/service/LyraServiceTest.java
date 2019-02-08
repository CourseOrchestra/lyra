package ru.curs.lyra.service;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.curs.celesta.CallContext;
import ru.curs.celestaunit.CelestaTest;
import ru.curs.celestaunit.CelestaUnitExtension;
import ru.curs.lyra.kernel.LyraCallContext;

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
        JSONObject metadata = srv.getMetadata(ctx, "ru.curs.lyra.service.TestForm", "foo");
        System.out.println(metadata.toString());
        assertEquals("95%", metadata.getJSONObject(LyraService.COMMON).get(LyraService.GRID_WIDTH));
    }
}