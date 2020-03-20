package com.kbj.restapi.index;


import com.kbj.restapi.common.BaseControllerTests;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class IndexControllerTests extends BaseControllerTests {

    @Test
    public void index() throws Exception {
        mockMvc.perform(get("/api"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("_links.events").exists());
    }
}
