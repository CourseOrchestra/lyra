package ru.curs.lyra.service;

import java.util.Collections;
import java.util.Map;

/**
 * Parameters for form instantiation that come from client code.
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

    /**
     * Form class name.
     */
    public String getFormClass() {
        return formClass;
    }

    /**
     * Form instance id.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Application-specific parameters in the form of String-String map.
     */
    public Map<String, String> getClientParams() {
        return clientParams;
    }

    /**
     * Grid identifier.
     */
    public String getDgridId() {
        return dGridId;
    }
}
