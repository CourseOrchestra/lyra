package ru.curs.lyra.kernel;

import org.json.JSONObject;
import ru.curs.celesta.SystemCallContext;

public class LyraCallContext extends SystemCallContext {

    public LyraCallContext() {
        super();
    }

    private JSONObject lyraContext;

    @SuppressWarnings("unused")
    public JSONObject getLyraContext() {
        return lyraContext;
    }

    public void setLyraContext(JSONObject lyraContext) {
        this.lyraContext = lyraContext;
    }


}
