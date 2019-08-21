package com.appodeal.time_report;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.JiraWorkflow;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FilterStatusValuesGenerator implements ValuesGenerator {

    @Override
    public Map getValues(Map userParams) {
        GenericValue projectGV = (GenericValue) userParams.get("project");

        Map statusesMap = ListOrderedMap.decorate(new HashMap());

        Project project = ComponentAccessor.getProjectManager().getProjectObj(projectGV.getLong("id"));
        Collection<IssueType> issueTypes = project.getIssueTypes();

        HashSet<Status> statusSet = new HashSet<>();
        for (IssueType issueType : issueTypes) {
            JiraWorkflow jiraWorkflow = ComponentAccessor.getWorkflowManager().getWorkflow(project.getId(), issueType.getId());
            statusSet.addAll(jiraWorkflow.getLinkedStatusObjects());
        }

        for (Status status : statusSet) {
            statusesMap.put(status.getId(), status.getName());
        }

        return statusesMap;
    }
}
