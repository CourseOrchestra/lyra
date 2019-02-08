package ru.curs.lyra.service;

import java.util.Collections;
import java.util.Map;

/**
 * Parameters for form instantiation.
 */
public class FormInstantiationParameters {
    private final String formClass;
    private final String instanceId;
    private final Map<String, String> clientParams;
    private final String dGridId;

    public FormInstantiationParameters(String formClass,
                                       String instanceId) {
        this(formClass, instanceId, Collections.emptyMap());
    }

    public FormInstantiationParameters(String formClass,
                                       String instanceId,
                                       Map<String, String> clientParams) {
        this.formClass = formClass;
        this.instanceId = instanceId;
        this.clientParams = clientParams;
        this.dGridId = formClass + "." + instanceId;
    }

    public String getFormClass() {
        return formClass;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public Map<String, String> getClientParams() {
        return clientParams;
    }

    public String getDgridId() {
        return dGridId;
    }
}
