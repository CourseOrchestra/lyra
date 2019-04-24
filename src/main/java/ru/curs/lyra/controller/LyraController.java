package ru.curs.lyra.controller;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.SystemCallContext;
import ru.curs.lyra.dto.DataParams;
import ru.curs.lyra.dto.DataResult;
import ru.curs.lyra.dto.FormInstantiationParams;
import ru.curs.lyra.dto.MetaDataResult;
import ru.curs.lyra.service.LyraService;


/**
 * Implements controller layer for Lyra forms backend.
 */
@RestController
@RequestMapping("/lyra")
public final class LyraController {
    private final LyraService srv;

    public LyraController(LyraService srv) {
        this.srv = srv;
    }

    /**
     * Get metadata.
     *
     * @param formInstantiationParams FormInstantiationParams
     */
    @PostMapping("/metadata")
    public MetaDataResult getMetadata(@RequestBody FormInstantiationParams formInstantiationParams) {
        //TODO: take into account user name here
        //call context should be created with real user name
        CallContext ctx = new SystemCallContext();

        return srv.getMetadata(ctx, formInstantiationParams);
    }


    /**
     * Get data.
     *
     * @param dataParams DataParams
     */
    @PostMapping("/data")
    public ResponseEntity getData(@RequestBody DataParams dataParams) {
        //TODO: take into account user name here
        //call context should be created with real user name
        CallContext ctx = new SystemCallContext();

        DataResult dataResult = srv.getData(ctx, dataParams.getFormInstantiationParams(), dataParams.getDataRetrievalParams());

        Object data = dataResult.getObjAddData() == null ? dataResult.getData() : dataResult.getObjAddData();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json;charset=UTF-8");

        int totalCount = dataParams.getDataRetrievalParams().getTotalCount();
        int firstIndex = dataParams.getDataRetrievalParams().getOffset();
        int lastIndex = dataParams.getDataRetrievalParams().getOffset() + dataParams.getDataRetrievalParams().getLimit() - 1;
        responseHeaders.set("Content-Range", "items " + firstIndex + "-"
                + lastIndex + "/" + totalCount);

        return new ResponseEntity<>(data, responseHeaders, HttpStatus.OK);
    }


}
