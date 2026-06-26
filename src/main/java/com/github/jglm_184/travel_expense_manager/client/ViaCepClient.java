package com.github.jglm_184.travel_expense_manager.client;

import com.github.jglm_184.travel_expense_manager.dto.ViaCepResponse;
import com.github.jglm_184.travel_expense_manager.util.FormatterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class ViaCepClient {

    private final FormatterUtil formatterUtil;

    public ViaCepResponse findAddress(String zipCode) {
        return new RestTemplate()
                .getForObject("https://viacep.com.br/ws/{zipCode}/json/",
                        ViaCepResponse.class,
                        formatterUtil.cleanNumbers(zipCode));
    }
}