package com.recob.controller;

import com.recob.client.ManagerClient;
import com.recob.domain.launch.SurveyLaunch;
import com.recob.domain.model.SurveyModel;
import com.recob.domain.model.SurveyResponse;
import com.recob.domain.survey.Question;
import com.recob.domain.survey.Survey;
import com.recob.service.launch.ISurveyLaunchService;
import com.recob.service.survey.ISurveyService;
import com.recob.service.transform.ITransformer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@RestController
@AllArgsConstructor
@CrossOrigin
@Slf4j
public class SurveysController {

    private ISurveyService       surveyService;
    private ISurveyLaunchService surveyLaunchService;
    private ManagerClient        managerClient;
    private ITransformer<SurveyResponse, Survey> surveyTransformer;

    @GetMapping("/surveys")
    public Flux<SurveyResponse> getSurveys() {
        return surveyService.findAll().map(surveyTransformer::transform);
    }

    @GetMapping("/surveys/{id}")
    public Mono<Survey> getById(@PathVariable(name = "id") String id) {
        return surveyService.findById(id);
    }

    @PostMapping("/surveys")
    public Mono<SurveyResponse> saveSurvey(@RequestBody Mono<SurveyModel> saveSurveyRequest) {
        return saveSurveyRequest
                .publishOn(Schedulers.parallel())
                .map(this::transformSaveRequest)
                .flatMap(surveyService::save)
                .map(surveyTransformer::transform);
    }

    @GetMapping("/surveys/search")
    public Flux<SurveyResponse> findByName(@RequestParam(name = "title") String name) {
        return surveyService.findByTitle(name)
                .map(surveyTransformer::transform);
    }

    @GetMapping("/surveys/{id}/launches")
    public Flux<SurveyLaunch> getSurveyLaunches(@PathVariable(name = "id") String surveyId) {
        return surveyLaunchService.findBySurveyId(surveyId);
    }

    @GetMapping("/surveys/{id}/start")
    public Map<String, Object> createService(@PathVariable(name = "id") String surveyId) {
        Map<String, Object> body = new HashMap<>();
        body.put("surveyId", surveyId);
        return managerClient.create(body);
    }


    private Survey transformSaveRequest(SurveyModel surveyModel) {
        log.debug("[transformSaveRequest] transforming request");
        Survey survey = new Survey();

        try {
            survey.setAvailableTime(surveyModel.getAvailableTime());
            // key - question number, value - question
            Map<Long, Question> questionMap = LongStream.range(0, surveyModel.getQuestions().size())
                    .boxed()
                    .collect(Collectors.toMap(Function.identity(), l -> {

                        Question question = surveyModel.getQuestions().get(l.intValue());
                        question.setPosition(l);

                        if (!CollectionUtils.isEmpty(question.getOptions())) {
                            question.getOptions().forEach(o -> o.setId(UUID.randomUUID().toString()));
                        }

                        return question;
                    }));

            survey.setTitle(surveyModel.getTitle());
            survey.setQuestions(questionMap);
            survey.setThumbnail(surveyModel.getThumbnail());
        } catch (Exception e) {
            log.error("[transformSaveRequest] error ", e);
        }

        return survey;
    }


}
