package kr.map.goci;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * Created by kiost on 2017-07-14.
 */
public class He5 {

    @Data
    @AllArgsConstructor
    public static class Response {
        private Crop.Request properties;
        private Links links;
    }

    @Data
    @AllArgsConstructor
    @ToString
    public static class Attributes {
        private String startX;
        private String startY;
        private String endX;
        private String endY;
        private String date;
        private String type;
        private String outputType;
    }
}
