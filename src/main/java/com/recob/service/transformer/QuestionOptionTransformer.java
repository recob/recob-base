package com.recob.service.transformer;

import com.recob.domain.question.QuestionOption;
import com.recob.service.question.dto.QuestionOptionResponse;
import org.springframework.stereotype.Component;

@Component
public class QuestionOptionTransformer implements ITransformer<QuestionOptionResponse, QuestionOption> {

    @Override
    public QuestionOptionResponse transform(QuestionOption questionOption) {
        QuestionOptionResponse response = new QuestionOptionResponse();

        response.setValue(questionOption.getValue());
        response.setId(questionOption.getId());

        return response;
    }
}
