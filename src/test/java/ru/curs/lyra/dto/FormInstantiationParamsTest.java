package ru.curs.lyra.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormInstantiationParamsTest {

    @Test
    void getDgridId() {
        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestMetadataForm", "foo");
        assertEquals("ru-curs-lyra-service-forms-TestMetadataForm-foo", formInstantiationParams.getDgridId());
    }
}
