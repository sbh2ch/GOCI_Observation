package kr.map.goci;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by kiost on 2017-07-14.
 */
public class He5 {

    @Data
    @AllArgsConstructor
    public static class Response {
        private Crop.Request downInfo;
        private Links links;
    }

    @Data
    @AllArgsConstructor
    public static class Attributes {
        private String startX;
        private String startY;
        private String endX;
        private String endY;
        private String date;
        private String type;
        private String outputType;

        @Override
        public String toString() {
            return "He5{" +
                    "startX='" + startX + '\'' +
                    ", startY='" + startY + '\'' +
                    ", endX='" + endX + '\'' +
                    ", endY='" + endY + '\'' +
                    ", date='" + date + '\'' +
                    ", type='" + type + '\'' +
                    ", outputType='" + outputType + '\'' +
                    '}';
        }
    }
}
