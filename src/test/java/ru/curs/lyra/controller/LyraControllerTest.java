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
import ru.curs.lyra.dto.*;
import ru.curs.lyra.service.LyraService;
import ru.curs.lyra.spring.boot.WebSocketConfig;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpHeaders.CONTENT_RANGE;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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

            fooCursor.setId(3);
            fooCursor.setName("Name3");
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
                "fooController"));
        System.out.println("input: " + jsonString);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/lyra/metadata")
                .content(jsonString)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String resultString = result.getResponse().getContentAsString();
        System.out.println("output: " + resultString);
        assertNotNull(resultString);

        MetaDataResult metaDataResult = objectMapper.readValue(resultString, MetaDataResult.class);
        assertNotNull(metaDataResult);

        Common common = metaDataResult.getCommon();
        assertEquals("500px", common.getGridWidth());
        assertEquals("470px", common.getGridHeight());
        assertEquals(10, common.getLimit());
        assertFalse(common.isVisibleColumnsHeader());
        assertFalse(common.isAllowTextSelection());
        assertEquals("id", common.getPrimaryKey());

        Map<String, String> sum = common.getSummaryRow();
        assertNotNull(sum);
        assertEquals(2, sum.size());
        assertEquals("ID", sum.get("id"));
        assertEquals("NAME", sum.get("name"));

        assertEquals(4, metaDataResult.getColumns().size());

        Column column = metaDataResult.getColumns().get("1");
        assertEquals("id", column.getId());
        assertEquals("id", column.getCaption());
        assertTrue(column.isVisible());
        assertEquals("lyra-type-int className1", column.getCssClassName());
        assertEquals("width:100px;text-align:right;", column.getCssStyle());

        column = metaDataResult.getColumns().get("2");
        assertEquals("name", column.getId());
        assertEquals("name field caption", column.getCaption());
        assertFalse(column.isVisible());
        assertEquals("lyra-type-varchar className2", column.getCssClassName());
        assertEquals("width:300px;text-align:left;", column.getCssStyle());

        column = metaDataResult.getColumns().get("3");
        assertEquals("unboundField1", column.getId());
        assertEquals("REAL", column.getCaption());
        assertTrue(column.isVisible());
        assertEquals("lyra-type-real", column.getCssClassName());
        assertEquals("white-space:nowrap;width:100px;text-align:right;", column.getCssStyle());

        column = metaDataResult.getColumns().get("4");
        assertEquals("unboundField2", column.getId());
        assertEquals("DATETIME", column.getCaption());
        assertFalse(column.isVisible());
        assertEquals("lyra-type-datetime", column.getCssClassName());
        assertEquals("white-space:nowrap;width:70px;text-align:center;", column.getCssStyle());

    }

    @Test
    void getData() throws Exception {

        final class MetaDataParams {
            FormInstantiationParams formInstantiationParams;
            DataRetrievalParams dataRetrievalParams;

            MetaDataParams(FormInstantiationParams formInstantiationParams, DataRetrievalParams dataRetrievalParams) {
                this.formInstantiationParams = formInstantiationParams;
                this.dataRetrievalParams = dataRetrievalParams;
            }

            public FormInstantiationParams getFormInstantiationParams() {
                return formInstantiationParams;
            }

            public DataRetrievalParams getDataRetrievalParams() {
                return dataRetrievalParams;
            }
        }

        FormInstantiationParams formInstantiationParams
                = new FormInstantiationParams("ru.curs.lyra.service.forms.TestDataForm", "fooController");

        DataRetrievalParams dataRetrievalParams = new DataRetrievalParams();
        dataRetrievalParams.setLimit(50);
        dataRetrievalParams.setOffset(0);
        dataRetrievalParams.setDgridOldPosition(0);
        dataRetrievalParams.setSortingOrFilteringChanged(true);
        dataRetrievalParams.setFirstLoading(true);
        dataRetrievalParams.setRefreshId(null);

        String jsonString = objectMapper.writeValueAsString(new MetaDataParams(formInstantiationParams, dataRetrievalParams));
        System.out.println("input: " + jsonString);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/lyra/data")
                .content(jsonString)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        assertEquals("items 0-49/3", result.getResponse().getHeaderValue(CONTENT_RANGE));

        String resultString = result.getResponse().getContentAsString();
        System.out.println("output: " + resultString);
        assertNotNull(resultString);

        List<Map<String, Object>> data = objectMapper.readValue(resultString, List.class);
        assertNotNull(data);

        assertEquals(3, data.size());

        assertEquals("1", data.get(0).get("id"));
        assertEquals("Name", data.get(0).get("name"));
        assertEquals("1", data.get(0).get("recversion"));

        assertEquals("2", data.get(1).get("id"));
        assertEquals("Name2", data.get(1).get("name"));
        assertEquals("1", data.get(1).get("recversion"));

        assertEquals("3", data.get(2).get("id"));
        assertEquals("Name3", data.get(2).get("name"));
        assertEquals("1", data.get(2).get("recversion"));
    }
}
