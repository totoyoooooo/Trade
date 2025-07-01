package com.second.hand.trading.server.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class ChatModel {

    private String id;

    private Long user1_id;

    private Long user2_id;

    private Long getterId;

    private Long otherId;

    private String name;

    private String avatar;

    private String lastMessage;

    private String timestamp;

    private String status;

    private Long unread;

    public Long getOtherUserId(){
        if(!user1_id.equals(getterId)) otherId = user1_id;
        else otherId = user2_id;
        return otherId;
    }

    public void setTimestamp(String timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate sendDate = LocalDate.parse(timestamp,formatter);
        LocalDate nowDate = LocalDate.now();
        if(sendDate.getYear() == nowDate.getYear() && sendDate.getMonth() == nowDate.getMonth() && sendDate.getDayOfMonth() == nowDate.getDayOfMonth()){
            this.timestamp = timestamp.split(" ")[1].split(":")[0] + ":" + timestamp.split(" ")[1].split(":")[1];
        }else if(sendDate.getYear() == nowDate.getYear()){
            this.timestamp = timestamp.split(" ")[0].split("-")[1] + "月" +
                    timestamp.split(" ")[0].split("-")[2] + "日";
        }else{
            this.timestamp = timestamp;
        }
    }

    @Override
    public String toString() {
        return "id:" + id + "user1Id:" +user1_id + "user2Id:" + user2_id + "getterId:" + getterId;
    }
}
