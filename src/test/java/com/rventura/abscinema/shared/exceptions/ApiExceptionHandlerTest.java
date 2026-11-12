package com.rventura.abscinema.shared.exceptions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import com.rventura.abscinema.security.SecurityConfig;

@WebMvcTest(ApiExceptionHandlerTest.RejectingController.class)
@Import({SecurityConfig.class, ApiExceptionHandlerTest.RejectingController.class})
class ApiExceptionHandlerTest {

    // Request validation always runs before the repository, so a real endpoint can't reach this path.
    @RestController
    static class RejectingController {
        @PostMapping("/internal/test/reject")
        void reject() {
            throw new InvalidInputOnSaveException(new IllegalStateException("constraint violated"));
        }
    }

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockUser(authorities = "ADMIN")
    void invalidInputOnSaveBecomesFormLevelBadRequest() throws Exception {
        mvc.perform(post("/internal/test/reject").with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors[0].field").isEmpty())
            .andExpect(jsonPath("$.errors[0].message").value("This could not be saved, check the fields and try again"));
    }
}
