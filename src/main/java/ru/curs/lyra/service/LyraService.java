package ru.curs.lyra.service;

import org.json.JSONObject;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.dbutils.BasicCursor;
import ru.curs.celesta.transaction.CelestaTransaction;
import ru.curs.lyra.dto.ScrollBackParams;
import ru.curs.lyra.kernel.BasicGridForm;

/**
 * Implements service layer for Lyra forms backend.
 */
@Service
public class LyraService {

    private final FormFactory formFactory = new FormFactory();
    private final MetadataFactory metadataFactory = new MetadataFactory();
    private final DataFactory dataFactory = new DataFactory();

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
     * @param callContext                 Celesta call context
     * @param formInstantiationParameters Parameters of form instantiation (Form Factory will either create a new form,
     *                                    or will use the existing form in cache)
     */
    //TODO: get rid of transaction here. Maybe this requires changing the API for BasicGridForm
    @CelestaTransaction
    public JSONObject getMetadata(CallContext callContext, FormInstantiationParameters formInstantiationParameters) {

        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(callContext, formInstantiationParameters, this);

        return metadataFactory.buildMetadata(basicGridForm);

    }

    /**
     * Get data for the given form.
     *
     * @param callContext                 Celesta call context
     * @param formInstantiationParameters Parameters of form instantiation (Form Factory will either create a new form,
     *                                    or will use the existing form in cache)
     * @param dataRetrievalParams         DataRetrievalParams
     */
    @CelestaTransaction
    public Object getData(CallContext callContext,
                          FormInstantiationParameters formInstantiationParameters,
                          DataRetrievalParams dataRetrievalParams) {

        BasicGridForm<? extends BasicCursor> basicGridForm =
                formFactory.getFormInstance(callContext, formInstantiationParameters, this);

        dataFactory.setParameters(basicGridForm, formInstantiationParameters, dataRetrievalParams);
        return dataFactory.buildData();

    }


}
