package ru.curs.lyra.dto;

import java.util.Collections;
import java.util.Map;

/**
 * Parameters for form instantiation that come from client code.
 */
public class FormInstantiationParams {
    private final String formClass;
    private final String instanceId;
    private final Map<String, Object> clientParams;


    public FormInstantiationParams() {
        formClass = null;
        instanceId = null;
        clientParams = null;
    }

    public FormInstantiationParams(String formClass,
                                   String instanceId) {
        this(formClass, instanceId, Collections.emptyMap());
    }

    public FormInstantiationParams(String formClass,
                                   String instanceId,
                                   Map<String, Object> clientParams) {
        this.formClass = formClass;
        this.instanceId = instanceId;
        this.clientParams = clientParams;
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
     * Application-specific parameters in the form of String-Object map.
     */
    public Map<String, Object> getClientParams() {
        return clientParams;
    }

    /**
     * Grid identifier.
     */
    public String getDgridId() {
        return formClass + "." + instanceId;
    }
}
