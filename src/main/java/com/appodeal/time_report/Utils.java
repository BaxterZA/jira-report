package com.appodeal.time_report;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.util.ParameterUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.TimeUnit;

class Utils {
    private static final Logger log = Logger.getLogger(Utils.class);

    static String getTimeString(long intervalInMills) {
        long days = TimeUnit.MILLISECONDS.toDays(intervalInMills);
        long hours = TimeUnit.MILLISECONDS.toHours(intervalInMills - TimeUnit.DAYS.toMillis(days));
        long minutes = TimeUnit.MILLISECONDS.toMinutes(intervalInMills - TimeUnit.HOURS.toMillis(hours) - TimeUnit.DAYS.toMillis(days));

        return ((days>0) ? days + "d " : "") +
                ((hours>0) ? hours + "h " : "") +
                (minutes + "m");
    }


    static String[] getArrayParam(String name, Map params) {
        if (params.get(name) instanceof String) {
            return new String[]{(String) params.get(name)};
        } else {
            return (String[]) params.get(name);
        }
    }

    static Date getDateParam(String name, Map params) {
        DateTimeFormatterFactory dateTimeFormatterFactory = ComponentAccessor.getComponent(DateTimeFormatterFactory.class);
        DateTimeFormatter userFormatter = dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.DATE_PICKER).forLoggedInUser();

        return userFormatter.parse(ParameterUtils.getStringParam(params, name));
    }

    static String formatDate(Date date) {
        DateTimeFormatterFactory dateTimeFormatterFactory = ComponentAccessor.getComponent(DateTimeFormatterFactory.class);
        DateTimeFormatter userFormatter = dateTimeFormatterFactory.formatter().withStyle(DateTimeStyle.DATE_PICKER).forLoggedInUser();

        return userFormatter.format(date);
    }

    static long getWorkingIntervalBetweenToTimestampsInMills(Long start, Long finish) {
        Date startDate = new Date(start);
        Date finishDate = new Date(finish);

        Calendar startDateCalendar = Calendar.getInstance();
        startDateCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        startDateCalendar.setTime(startDate);
        Calendar finishDateCalendar = Calendar.getInstance();
        finishDateCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        finishDateCalendar.setTime(finishDate);

        if (isSameDay(startDateCalendar, finishDateCalendar)) {
            return finish - start;
        }

        long workingPeriodInMills = 0L;
        while (!isSameDay(startDateCalendar, finishDateCalendar)) {
            if (startDateCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && startDateCalendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                workingPeriodInMills += 8 * 60 * 60 * 1000;
            }
            startDateCalendar.add(Calendar.DATE, 1);
        }

        long workingPeriodLastDayInMills = finishDateCalendar.getTimeInMillis() - startDateCalendar.getTimeInMillis();

        return Math.max(workingPeriodInMills + workingPeriodLastDayInMills, 0L);
    }

    static boolean isSameDay(Calendar startDateCalendar, Calendar finishDateCalendar) {
        return startDateCalendar.get(Calendar.DAY_OF_YEAR) == finishDateCalendar.get(Calendar.DAY_OF_YEAR) &&
                startDateCalendar.get(Calendar.YEAR) == finishDateCalendar.get(Calendar.YEAR);
    }

    static double median(List<Double> values) {
        if (values.isEmpty()) {
            return 0;
        }
        Double[] array = values.toArray(new Double[0]);
        Arrays.sort(array);
        double median;
        int totalElements = array.length;
        if (totalElements % 2 == 0) {
            double sumOfMiddleElements = array[totalElements / 2] + array[totalElements / 2 - 1];
            median = sumOfMiddleElements / 2;
        } else {
            median = array[array.length / 2];
        }
        return median;
    }

    static String getStatusNamesFromArray(String[] statuses) {
        com.atlassian.jira.config.ConstantsManager constantsManager = ComponentAccessor.getConstantsManager();

        StringBuilder stringBuilder = new StringBuilder();
        for (String statusId : statuses) {
            stringBuilder.append(constantsManager.getStatus(statusId).getName());
            stringBuilder.append(", ");
        }
        stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());
        return stringBuilder.toString();
    }

    static String getComponentsNamesFromArray(String[] components) {
        ProjectComponentManager projectComponentManager = ComponentAccessor.getProjectComponentManager();

        StringBuilder stringBuilder = new StringBuilder();
        for (String componentId : components) {
            try {
                stringBuilder.append(projectComponentManager.find(Long.parseLong(componentId)).getName());
                stringBuilder.append(", ");
            } catch (EntityNotFoundException e) {
                log.error("Can't find component", e);
            }
        }
        stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());
        return stringBuilder.toString();
    }
}
