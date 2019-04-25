package ru.curs.lyra.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.curs.celesta.spring.boot.autoconfigure.CelestaAutoConfiguration;
import ru.curs.lyra.dto.FormInstantiationParams;
import ru.curs.lyra.service.LyraService;
import ru.curs.lyra.spring.boot.WebSocketConfig;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ContextConfiguration(classes = {LyraController.class, LyraService.class, WebSocketConfig.class, CelestaAutoConfiguration.class})
@WebMvcTest
//@CelestaTest
class LyraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMetadata() throws Exception {

        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestMetadataForm", "fooController1");

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(formInstantiationParams);
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


        //assertNotNull(resultString);
        //assertEquals("sfsdfsdfsdf", resultString);
    }

    @Test
    void getData() {
    }
}
