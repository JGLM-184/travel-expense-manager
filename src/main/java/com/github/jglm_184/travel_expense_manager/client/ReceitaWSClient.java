package com.github.jglm_184.travel_expense_manager.client;

import com.github.jglm_184.travel_expense_manager.dto.ReceitaWSResponse;
import com.github.jglm_184.travel_expense_manager.util.FormatterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class ReceitaWSClient {

    private final FormatterUtil formatterUtil;

    public ReceitaWSResponse findCnpj(String cnpj) {
        return new RestTemplate()
                .getForObject("https://receitaws.com.br/v1/cnpj/{CNPJ}",
                        ReceitaWSResponse.class,
                        formatterUtil.cleanNumbers(cnpj));
    }
}
