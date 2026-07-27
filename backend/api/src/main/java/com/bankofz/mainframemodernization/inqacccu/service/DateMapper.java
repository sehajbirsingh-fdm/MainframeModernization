package com.bankofz.mainframemodernization.inqacccu.service;

import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDate;

@Component
public class DateMapper {

    public String toIsoDate(Integer yyyymmdd) {
        if (yyyymmdd == null) {
            return null;
        }

        String value = String.format("%08d", yyyymmdd);
        int year = Integer.parseInt(value.substring(0, 4));
        int month = Integer.parseInt(value.substring(4, 6));
        int day = Integer.parseInt(value.substring(6, 8));

        try {
            return LocalDate.of(year, month, day).toString();
        } catch (DateTimeException exception) {
            return null;
        }
    }
}
