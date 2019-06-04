package ru.curs.lyra.service;

import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.transaction.CelestaTransaction;
import ru.curs.lyra.dto.*;
import ru.curs.lyra.kernel.BasicGridForm;

/**
 * Implements service layer for Lyra forms backend.
 */
@Service
public class LyraService {

    private final FormFactory formFactory = new FormFactory();
    private final MetadataFactory metadataFactory = new MetadataFactory();

    private SimpMessageSendingOperations messagingTemplate;

    public LyraService(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    void sendScrollBackPosition(ScrollBackParams params) {
        messagingTemplate.convertAndSend("/position", params);
    }


    /**
     * Get metadata for the given form.
     *
     * @param callContext             Celesta call context
     * @param formInstantiationParams Parameters of form instantiation (Form Factory will either create a new form,
     *                                or will use the existing form in cache)
     */
    //TODO: get rid of transaction here. Maybe this requires changing the API for BasicGridForm
    @CelestaTransaction
    public MetaDataResult getMetadata(CallContext callContext, FormInstantiationParams formInstantiationParams) {

        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(callContext, formInstantiationParams, this);

        return metadataFactory.buildMetadata(basicGridForm);

    }

    /**
     * Get data for the given form.
     *
     * @param callContext             Celesta call context
     * @param formInstantiationParams Parameters of form instantiation (Form Factory will either create a new form,
     *                                or will use the existing form in cache)
     * @param dataRetrievalParams     DataRetrievalParams
     */
    @CelestaTransaction
    public DataResult getData(CallContext callContext,
                              FormInstantiationParams formInstantiationParams,
                              DataRetrievalParams dataRetrievalParams) {

        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(callContext, formInstantiationParams, this);

        return new DataFactory(callContext, basicGridForm, dataRetrievalParams).dataResult();
    }


}
