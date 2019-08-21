package com.appodeal.time_report;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.issuetype.IssueType;

public class IssueReport {
    private final String key;
    private final Issue issue;
    private final long estimate;
    private final long spent;
    private final long usefulTime;
    private final long leadTime;
    private final double effectiveness;

    IssueReport(String key, Issue issue, long estimate, long spent, long usefulTime, long leadTime, double effectiveness) {
        this.key = key;
        this.issue = issue;
        this.estimate = estimate;
        this.spent = spent;
        this.usefulTime = usefulTime;
        this.leadTime = leadTime;
        this.effectiveness = effectiveness;
    }

    public String getKey() {
        return key;
    }

    public Issue getIssue() {
        return issue;
    }

    public IssueType getIssueType() {
        return issue.getIssueType();
    }

    public String getSummary() {
        return issue.getSummary();
    }

    public String getComponentNames() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ProjectComponent component : issue.getComponents()) {
            stringBuilder.append(component.getName());
            stringBuilder.append(", ");
        }
        stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());
        return stringBuilder.toString();
    }

    public String getEstimate() {
        return Utils.getTimeString(estimate);
    }

    public String getSpent() {
        return Utils.getTimeString(spent);
    }

    public double getSpentAccuracy() {
        return 100 * estimate/ (double) (spent + 1000);
    }

    public String getSpentAccuracyString() {
        return String.format("%.2f%%", getSpentAccuracy());
    }

    public String getUsefulTime() {
        return Utils.getTimeString(usefulTime);
    }

    public double getUsefulTimeAccuracy() {
        return 100 * estimate/ (double) (usefulTime + 1000);
    }

    public String getUsefulTimeAccuracyString() {
        return String.format("%.2f%%", getUsefulTimeAccuracy());
    }

    public String getLeadTime() {
        return Utils.getTimeString(leadTime);
    }

    public double getLeadTimeAccuracy() {
        return 100 * estimate/ (double) (leadTime + 1000);
    }

    public String getLeadTimeAccuracyString() {
        return String.format("%.2f%%", getLeadTimeAccuracy());
    }

    public double getEffectiveness() {
        return 100 * effectiveness;
    }

    public String getEffectivenessString() {
        return String.format("%.2f%%", getEffectiveness());
    }


    @Override
    public String toString() {
        return "IssueReport{" +
                "key='" + key + '\'' +
                ", estimate=" + estimate +
                ", spent=" + spent +
                ", usefulTime=" + usefulTime +
                ", leadTime=" + leadTime +
                ", effectiveness=" + effectiveness +
                '}';
    }

}
