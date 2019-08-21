package com.appodeal.time_report;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FilterComponentValuesGenerator implements ValuesGenerator {

    public Map getValues(Map userParams) {
        GenericValue projectGV = (GenericValue) userParams.get("project");

        Map componentsMap = ListOrderedMap.decorate(new HashMap());
        if (projectGV != null) {
            Collection components = ComponentAccessor.getProjectComponentManager().findAllForProject(projectGV.getLong("id"));
            for (Object component : components) {
                ProjectComponent projectComponent = (ProjectComponent) component;
                componentsMap.put(projectComponent.getId(), projectComponent.getName());
            }
        }

        return componentsMap;
    }
}
