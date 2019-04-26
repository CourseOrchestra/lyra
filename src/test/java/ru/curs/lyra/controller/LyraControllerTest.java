package ru.curs.lyra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import foo.FooCursor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.curs.celesta.CallContext;
import ru.curs.celesta.Celesta;
import ru.curs.celesta.SystemCallContext;
import ru.curs.celesta.spring.boot.autoconfigure.CelestaAutoConfiguration;
import ru.curs.lyra.dto.FormInstantiationParams;
import ru.curs.lyra.service.LyraService;
import ru.curs.lyra.spring.boot.WebSocketConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@ContextConfiguration(classes = {LyraController.class, LyraService.class, WebSocketConfig.class, CelestaAutoConfiguration.class})
@WebMvcTest
class LyraControllerTest {

    @Autowired
    private Celesta celesta;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        try (CallContext callContext = new SystemCallContext(celesta)) {
            FooCursor fooCursor = new FooCursor(callContext);

            fooCursor.setId(1);
            fooCursor.setName("Name");
            fooCursor.insert();

            fooCursor.setId(2);
            fooCursor.setName("Name2");
            fooCursor.insert();
        }
    }

    @AfterEach
    void tearDown() {
        try (CallContext callContext = new SystemCallContext(celesta)) {
            FooCursor fooCursor = new FooCursor(callContext);
            fooCursor.deleteAll();
        }
    }

    @Test
    void getMetadata() throws Exception {

        String jsonString = objectMapper.writeValueAsString(new FormInstantiationParams(
                "ru.curs.lyra.service.forms.TestMetadataForm",
                "fooController1"));
        System.out.println("json: " + jsonString);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/lyra/metadata")
                .content(jsonString)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String resultString = result.getResponse().getContentAsString();
        System.out.println("resultString: " + resultString);

        assertNotNull(resultString);

    }

    @Test
    void getData() {
    }
}
