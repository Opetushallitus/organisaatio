package fi.vm.sade.varda.rekisterointi.controller;

import fi.vm.sade.varda.rekisterointi.TestService;
import fi.vm.sade.varda.rekisterointi.exception.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class ControllerExceptionTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TestService testService;

    @Test
    public void applicationException() throws Exception {
        doThrow(new ApplicationException() {}).when(testService).doSomething();

        mvc.perform(get("/test")).andExpect(status().isInternalServerError());
    }

    @Test
    public void systemException() throws Exception {
        doThrow(new SystemException() {}).when(testService).doSomething();

        mvc.perform(get("/test")).andExpect(status().isInternalServerError());
    }

    @Test
    public void dataInconsistencyException() throws Exception {
        doThrow(new DataInconsistencyException("test")).when(testService).doSomething();

        mvc.perform(get("/test")).andExpect(status().isInternalServerError());
    }

    @Test
    public void userException() throws Exception {
        doThrow(new UserException() {}).when(testService).doSomething();

        mvc.perform(get("/test")).andExpect(status().isInternalServerError());
    }

    @Test
    public void notFoundException() throws Exception {
        doThrow(new NotFoundException("test")).when(testService).doSomething();

        mvc.perform(get("/test")).andExpect(status().isNotFound());
    }

}
