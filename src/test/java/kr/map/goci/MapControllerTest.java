package kr.map.goci;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest
public class MapControllerTest {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void getValue() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/2017-7-9-0/3-2/3/CDOM"));

        result.andDo(print());
        result.andExpect(status().isOk());
    }

    @Test
    public void getLatLon() throws Exception {
        ResultActions result = mockMvc.perform(get("/api/lonlat/3-2/3"));

        result.andDo(print());
        result.andExpect(status().isOk());
    }

    @Test
    public void makeImage() throws Exception {
        Crop.Request crop = new Crop.Request();
        crop.setDate("2017-7-9-0");
        crop.setStartX(1028);
        crop.setStartY(1436);
        crop.setEndX(2132);
        crop.setEndY(2512);
        crop.setType("CDOM");
        ResultActions result = mockMvc.perform(post("/api/crop").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(crop)));

        result.andDo(print());
        result.andExpect(status().isOk());
    }
}