package com.necom.controller.client;

import com.necom.config.security.JwtUtils;
import com.necom.config.security.UserDetailsServiceImpl;
import com.necom.dto.cashbook.ClientPaymentMethodResponse;
import com.necom.entity.cashbook.PaymentMethod;
import com.necom.entity.cashbook.PaymentMethodType;
import com.necom.mapper.cashbook.PaymentMethodMapper;
import com.necom.repository.cashbook.PaymentMethodRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClientPaymentMethodController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClientPaymentMethodControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentMethodRepository paymentMethodRepository;
    @MockBean
    private PaymentMethodMapper paymentMethodMapper;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void getAllPaymentMethodsShouldReturnWrappedContent() throws Exception {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setId(1L);
        paymentMethod.setName("Cash");
        paymentMethod.setCode(PaymentMethodType.CASH);
        paymentMethod.setStatus(1);
        ClientPaymentMethodResponse response = new ClientPaymentMethodResponse();
        response.setPaymentMethodId(1L);
        response.setPaymentMethodName("Cash");
        response.setPaymentMethodCode(PaymentMethodType.CASH);

        when(paymentMethodRepository.findAllByStatus(1)).thenReturn(List.of(paymentMethod));
        when(paymentMethodMapper.entityToClientResponse(eq(List.of(paymentMethod)))).thenReturn(List.of(response));

        mockMvc.perform(get("/client-api/payment-methods").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].paymentMethodName").value("Cash"))
                .andExpect(jsonPath("$.content[0].paymentMethodCode").value("CASH"));
    }
}
