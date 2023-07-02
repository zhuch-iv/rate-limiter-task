package org.zhu4.task.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureMockMvc
public class AbstractMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    ResultActions makeRequest(String path, String remoteAddr) throws Exception {
        return this.mockMvc.perform(get(path).with(request -> {
            request.setRemoteAddr(remoteAddr);
            return request;
        }));
    }

    int makeRequestAndGetStatusCode(String path, String remoteAddr) throws Exception {
        return makeRequest(path, remoteAddr).andReturn().getResponse().getStatus();
    }
}
