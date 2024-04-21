package com.klaimz.api;


import com.klaimz.model.api.ChartAnalyticsRequest;
import com.klaimz.model.http.MessageBean;
import com.klaimz.service.AnalyticsService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import jakarta.inject.Inject;

import static com.klaimz.util.HttpUtils.success;
import static io.micronaut.security.rules.SecurityRule.IS_AUTHENTICATED;

@Controller("/analytics")
@Secured(IS_AUTHENTICATED)
@ExecuteOn(TaskExecutors.BLOCKING)
public class AnalyticsController {

    @Inject
    private AnalyticsService analyticsService;

    @Post("/claim")
    public HttpResponse<MessageBean> getClaimAnalytics(@Body ChartAnalyticsRequest request) {

        var result = analyticsService.getChartAnalytics(request);
        return success(result, "Claim analytics fetched");
    }

}
