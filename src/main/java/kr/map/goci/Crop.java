package kr.map.goci;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * Created by kiost on 2017-07-14.
 */
public class Crop {
    @Data
    @ToString
    public static class Request {
        private int startX;
        private int startY;
        private int endX;
        private int endY;
        private String date;
        private String type;
    }

    @Data
    @AllArgsConstructor
    public static class Response {
        private String path;
        private String name;
        private Links imgLink;
        private He5.Response satelliteDownInfo;

    }

    @Data
    @ToString
    public static class Show {
        private String path;
        private String name;
    }
}
