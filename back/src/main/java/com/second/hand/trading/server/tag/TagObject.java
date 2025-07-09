package com.second.hand.trading.server.tag;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
public class TagObject {

    private String text;

    private Long count;

    private String time;

    public TagObject() {}

    public TagObject(String text, Long count, String time) {
        this.text = text;
        this.count = count;
        this.time = time;
    }

    public void setTime(){
        LocalDateTime currentDatetime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.time = formatter.format(currentDatetime);
    }

    public void addCount(){
        this.count++;
    }

    public void subtractCount(){
        this.count--;
    }

    public Long calculateTimeDayDistance(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.parse(this.time, formatter);
        LocalDateTime currentDateTime = LocalDateTime.now();
        return ChronoUnit.DAYS.between(time,currentDateTime);
    }

    public Long calculateTimeMinuteDistance(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.parse(this.time, formatter);
        LocalDateTime currentDateTime = LocalDateTime.now();
        return ChronoUnit.MINUTES.between(time,currentDateTime);
    }

}
