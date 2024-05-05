package com.klaimz.util;

import com.klaimz.model.Claim;
import com.klaimz.model.FormField;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Constants {

//New→ CM Assigned → Evaluator Assigned → Evaluation in Progress → Claim Approval in Progress → Approved/Denied
    public static String STATUS_NEW = "New";
    public static String STATUS_CM_ASSIGNED = "Claim Manager Assigned";

    public static String STATUS_EVALUATOR_ASSIGNED = "Evaluator Assigned";

    public static String STATUS_EVALUATION_IN_PROGRESS = "Evaluation in Progress";

    public static String STATUS_APPROVAL_IN_PROGRESS = "Claim Approval in Progress";

    public static String STATUS_DENIED = "Denied";
    public static String STATUS_APPROVED = "Approved";

    public static List<String> STATUS_LIST = List.of(STATUS_NEW, STATUS_CM_ASSIGNED, STATUS_EVALUATOR_ASSIGNED, STATUS_EVALUATION_IN_PROGRESS, STATUS_APPROVAL_IN_PROGRESS, STATUS_DENIED, STATUS_APPROVED);

    public static final String FIRST_RUN = "first_run";

    public static final String LAST_RUN = "last_run";

    public static final String CHART_TYPE_PIE = "PIE_CHART";
    public static final String CHART_TYPE_BAR = "BAR_CHART";
    public static final String CHART_TYPE_LINE = "LINE_CHART";





    public static List<FormField> FORM_FIELDS = List.of(
            FormField.builder()
                    .key("text")
                    .defaultName("Text")
                    .regex(".*")
                    .build(),
            FormField.builder()
                    .key("number")
                    .defaultName("Number")
                    .regex("\\d+")
                    .build(),
            FormField.builder()
                    .key("date")
                    .defaultName("Date")
                    .regex("\\d{4}-\\d{2}-\\d{2}")
                    .build(),
            FormField.builder()
                    .key("image")
                    .defaultName("Images")
                    .regex("")
                    .build()
    );
}
