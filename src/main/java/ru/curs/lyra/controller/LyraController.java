package ru.curs.lyra.controller;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.SystemCallContext;
import ru.curs.lyra.service.DataRetrievalParams;
import ru.curs.lyra.dto.MetaDataParams;
import ru.curs.lyra.service.FormInstantiationParameters;
import ru.curs.lyra.service.LyraService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/lyra")
@SuppressWarnings("unused")
public class LyraController {
    private final LyraService srv;

    public LyraController(LyraService srv) {
        this.srv = srv;
    }


    @PostMapping("/metadata")
    public String getMetadata(@RequestParam Map<String, String> body) throws Exception {
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

        return srv.getMetadata(ctx, formInstantiationParameters).toString();
    }


    @PostMapping("/data")
    public ResponseEntity getData(@RequestParam Map<String, String> body) throws Exception {

        DataRetrievalParams params = new DataRetrievalParams();
        params.setOffset(Integer.parseInt(body.get("offset")));
        params.setLimit(Integer.parseInt(body.get("limit")));
        params.setDgridOldPosition(Integer.parseInt(body.get("dgridOldPosition")));
        params.setSortingOrFilteringChanged(Boolean.parseBoolean(body.get("sortingOrFilteringChanged")));
        params.setFirstLoading(Boolean.parseBoolean(body.get("firstLoading")));
        params.setRefreshId(body.get("refreshId"));

        //TODO: take into account user name here
        //call context should be created with real user name
        CallContext ctx = new SystemCallContext();

        Map<String, String> clientParams = new HashMap<>();
        clientParams.put("context", body.get("context"));
        FormInstantiationParameters formInstantiationParameters = new FormInstantiationParameters(
                body.get("formClass"),
                body.get("instanceId"),
                clientParams
        );
        String data = srv.getData(ctx, formInstantiationParameters, params).toString();


        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json;charset=UTF-8");

        int totalCount = params.getTotalCount();
        int firstIndex = params.getOffset();
        int lastIndex = params.getOffset() + params.getLimit() - 1;
        responseHeaders.set("Content-Range", "items " + firstIndex + "-"
                + lastIndex + "/" + totalCount);

        ResponseEntity<String> responseEntity = new ResponseEntity<>(data, responseHeaders, HttpStatus.OK);

        return responseEntity;

    }


}
