package ru.curs.lyra.controller;


import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.SystemCallContext;
import ru.curs.lyra.dto.DataResult;
import ru.curs.lyra.dto.MetaDataParams;
import ru.curs.lyra.dto.MetaDataResult;
import ru.curs.lyra.service.DataRetrievalParams;
import ru.curs.lyra.service.FormInstantiationParameters;
import ru.curs.lyra.service.LyraService;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements controller layer for Lyra forms backend.
 */
@RestController
@RequestMapping("/lyra")
public final class LyraController {
    private static final String REFRESH_PARAMS = "refreshParams";
    private static final String SELECT_KEY = "selectKey";

    private final LyraService srv;

    public LyraController(LyraService srv) {
        this.srv = srv;
    }

    @PostMapping("/metadata")
    public MetaDataResult getMetadata(@RequestParam Map<String, String> body) {
        //TODO: MetaDataParams should be deserialized automatically
        //we should smoke some Spring documentation here
        MetaDataParams params = new MetaDataParams();
        params.setContext(body.get("context"));
        params.setFormClass(body.get("formClass"));
        params.setInstanceId(body.get("instanceId"));
        //TODO: take into account user name here
        //call context should be created with real user name
        CallContext ctx = new SystemCallContext();

        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("context", params.getContext());
        FormInstantiationParameters formInstantiationParameters = new FormInstantiationParameters(params.getFormClass(),
                params.getInstanceId(),
                clientParams
        );

        return srv.getMetadata(ctx, formInstantiationParameters);
    }


    @PostMapping("/data")
    public ResponseEntity getData(@RequestParam Map<String, String> body) {

        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("context", body.get("context"));
        FormInstantiationParameters formInstantiationParameters = new FormInstantiationParameters(
                body.get("formClass"),
                body.get("instanceId"),
                clientParams
        );

        DataRetrievalParams dataRetrievalParams = new DataRetrievalParams();
        dataRetrievalParams.setOffset(Integer.parseInt(body.get("offset")));
        dataRetrievalParams.setLimit(Integer.parseInt(body.get("limit")));
        dataRetrievalParams.setDgridOldPosition(Integer.parseInt(body.get("dgridOldPosition")));
        dataRetrievalParams.setSortingOrFilteringChanged(Boolean.parseBoolean(body.get("sortingOrFilteringChanged")));
        dataRetrievalParams.setFirstLoading(Boolean.parseBoolean(body.get("firstLoading")));
        dataRetrievalParams.setRefreshId(getKeyValuesById(body.get("refreshId")));

        JSONObject json = new JSONObject(body.get("context"));
        String selectKey = ((JSONObject) json.get(REFRESH_PARAMS)).get(SELECT_KEY).toString();
        dataRetrievalParams.setSelectKey(getKeyValuesById(selectKey));


        //TODO: take into account user name here
        //call context should be created with real user name
        CallContext ctx = new SystemCallContext();

        DataResult dataResult = srv.getData(ctx, formInstantiationParameters, dataRetrievalParams);

        Object data = dataResult.getObjAddData() == null ? dataResult.getData() : dataResult.getObjAddData();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json;charset=UTF-8");

        int totalCount = dataRetrievalParams.getTotalCount();
        int firstIndex = dataRetrievalParams.getOffset();
        int lastIndex = dataRetrievalParams.getOffset() + dataRetrievalParams.getLimit() - 1;
        responseHeaders.set("Content-Range", "items " + firstIndex + "-"
                + lastIndex + "/" + totalCount);

        return new ResponseEntity<>(data, responseHeaders, HttpStatus.OK);

    }

    private Object[] getKeyValuesById(final String refreshId) {
        if ((refreshId == null) || refreshId.isEmpty()) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(refreshId);
        Object[] obj = new Object[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            obj[i] = jsonArray.get(i);
        }
        return obj;
    }


}
