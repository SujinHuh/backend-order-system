package com.example.ordersystem.global.error;

import com.example.ordersystem.global.error.exception.BusinessException;
import com.example.ordersystem.global.error.exception.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExceptionTestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void validation_failed_should_return_400() throws Exception {
        // given
        String content = "{\"email\":\"invalid-email\", \"name\":\"\"}";

        // when
        ResultActions result = mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("C001"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[?(@.field == 'email')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'name')]").exists());
    }

    @Test
    void business_exception_should_return_defined_status() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/test/business-exception"));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("C003"))
                .andExpect(jsonPath("$.message").value("Entity Not Found"));
    }

    @Test
    void unhandled_exception_should_return_500() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/test/unhandled-exception"));

        // then
        result.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("C004"));
    }

    @Test
    void method_not_supported_should_return_405() throws Exception {
        // when
        ResultActions result = mockMvc.perform(post("/test/business-exception")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.code").value("C002"));
    }
}

@RestController
@RequestMapping("/test")
class ExceptionTestController {

    @PostMapping("/validation")
    public void validation(@Valid @RequestBody ExceptionTestRequest request) {
    }

    @GetMapping("/business-exception")
    public void businessException() {
        throw new EntityNotFoundException("Entity not found");
    }

    @GetMapping("/unhandled-exception")
    public void unhandledException() {
        throw new RuntimeException("Unhandled");
    }
}

@Getter
@Setter
class ExceptionTestRequest {
    @Email
    private String email;
    @NotBlank
    private String name;
}
