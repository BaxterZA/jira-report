package com.appodeal.time_report;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.report.impl.AbstractReport;
import com.atlassian.jira.web.action.ProjectActionSupport;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.query.operator.Operator;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class TimeReport extends AbstractReport {
    private static final Logger log = Logger.getLogger(TimeReport.class);

    @JiraImport
    private final SearchService searchService;

    public TimeReport(final SearchService searchService) {
        this.searchService = searchService;
    }

    public String generateReportHtml(ProjectActionSupport action, Map params) throws Exception {
        String projectId = (String) params.get("selectedProjectId");
        String[] components = Utils.getArrayParam("component", params);
        Date startDate = Utils.getDateParam("startDate", params);
        Date endDate = Utils.getDateParam("endDate", params);

        String[] usefulStatuses = Utils.getArrayParam("useful", params);
        String[] workingStatuses = Utils.getArrayParam("working", params);

        List<Double> spentAccuracyList = new ArrayList<>();
        List<Double> usefulTimeAccuracyList = new ArrayList<>();
        List<Double> leadTimeAccuracyList = new ArrayList<>();
        List<Double> effectivenessList = new ArrayList<>();

        Map<String, String> currentFilter = new HashMap<>();
        currentFilter.put("Interval", Utils.formatDate(startDate) + " to " + Utils.formatDate(endDate));
        currentFilter.put("Useful statuses", Utils.getStatusNamesFromArray(usefulStatuses));
        currentFilter.put("Working statuses", Utils.getStatusNamesFromArray(workingStatuses));
        if (components != null) {
            currentFilter.put("Components", Utils.getComponentsNamesFromArray(components));
        }


        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();
        builder.where()
                .project(projectId)
                .and()
                .addStringCondition("resolution", Operator.EQUALS, "Done")
                .and()
                .addDateRangeCondition("resolutiondate", startDate, endDate);
        if (components != null) {
            builder.where().and().component(components);
        }

        SearchResults results = searchService.search(action.getLoggedInUser(), builder.buildQuery(), PagerFilter.getUnlimitedFilter());

        List<Issue> issues = results.getResults();

        List<IssueReport> issueReports = new ArrayList<>();
        for (Issue issue : issues) {
            long originalEstimateInSeconds = issue.getOriginalEstimate() != null ? issue.getOriginalEstimate() : 0;
            long timeSpentInSeconds = issue.getTimeSpent() != null ? issue.getTimeSpent() : 0;

            Map<String, Long> intervals = new HashMap<>();
            long loopTimestamp = issue.getCreated().getTime();
            List<ChangeHistory> history = ComponentAccessor.getChangeHistoryManager().getChangeHistories(issue);
            for (ChangeHistory events : history) {
                List<ChangeItemBean> items = events.getChangeItemBeans();
                for (ChangeItemBean item : items) {
                    String field = item.getField();
                    if (field.equals("status")) {
                        Long transitionTime = events.getTimePerformed().getTime();
                        long savedInterval = intervals.getOrDefault(item.getFrom(), 0L);
                        long interval = Utils.getWorkingIntervalBetweenToTimestampsInMills(loopTimestamp, transitionTime);
                        intervals.put(item.getFrom(), interval + savedInterval);
                        loopTimestamp = transitionTime;
                    }
                }
            }
            long usefulTime = 0;
            for (String usefulStatus : usefulStatuses) {
                usefulTime += intervals.getOrDefault(usefulStatus, 0L);
            }
            long leadTime = 0;
            for (String workingStatus : workingStatuses) {
                leadTime += intervals.getOrDefault(workingStatus, 0L);
            }
            double effectiveness = (double) usefulTime / leadTime;

            IssueReport issueReport = new IssueReport(issue.getKey(), issue, originalEstimateInSeconds * 1000, timeSpentInSeconds * 1000, usefulTime, leadTime, effectiveness);
            issueReports.add(issueReport);

            spentAccuracyList.add(issueReport.getSpentAccuracy());
            usefulTimeAccuracyList.add(issueReport.getUsefulTimeAccuracy());
            leadTimeAccuracyList.add(issueReport.getLeadTimeAccuracy());
            effectivenessList.add(issueReport.getEffectiveness());
        }

        final Map reportParams = ImmutableMap.builder()
                .put("issues", issueReports)
                .put("spentAccuracyMedian", String.format("%.2f%%", Utils.median(spentAccuracyList)))
                .put("usefulTimeAccuracyMedian", String.format("%.2f%%", Utils.median(usefulTimeAccuracyList)))
                .put("leadTimeAccuracyMedian", String.format("%.2f%%", Utils.median(leadTimeAccuracyList)))
                .put("effectivenessMedian", String.format("%.2f%%", Utils.median(effectivenessList)))
                .put("currentFilter", currentFilter)
                .build();

        return descriptor.getHtml("view", reportParams);
    }

    public void validate(ProjectActionSupport action, Map params) {
        super.validate(action, params);

        if (!params.containsKey("selectedProjectId") && !StringUtils.isEmpty((String) params.get("selectedProjectId"))) {
            action.addErrorMessage(action.getText("time-report.project.field.is.missing"));
        }
        if (!params.containsKey("useful")) {
            action.addError("useful", action.getText("time-report.field.is.required"));
        }
        if (!params.containsKey("working")) {
            action.addError("working", action.getText("time-report.field.is.required"));
        }
        if (!params.containsKey("startDate") || StringUtils.isEmpty((String) params.get("startDate"))) {
            action.addError("startDate", action.getText("time-report.field.is.required"));
        }
        if (!params.containsKey("endDate") || StringUtils.isEmpty((String) params.get("endDate"))) {
            action.addError("endDate", action.getText("time-report.field.is.required"));
        }
    }
}