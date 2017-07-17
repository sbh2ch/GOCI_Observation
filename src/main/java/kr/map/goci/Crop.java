package kr.map.goci;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by kiost on 2017-07-14.
 */
public class Crop {
    @Data
    public static class Request {
        private int startX;
        private int startY;
        private int endX;
        private int endY;
        private String date;
        private String type;

        @Override
        public String toString() {
            return "Crop{" +
                    "startX='" + startX + '\'' +
                    ", startY='" + startY + '\'' +
                    ", endX='" + endX + '\'' +
                    ", endY='" + endY + '\'' +
                    ", date='" + date + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    @Data
    @AllArgsConstructor
    public static class Response {
        private String path;
        private String name;
        private Links links;

        @Override
        public String toString() {
            return "Response{" +
                    "path='" + path + '\'' +
                    ", name='" + name + '\'' +
                    ", links=" + links +
                    '}';
        }
    }

    @Data
    public static class Show {
        private String path;
        private String name;

        @Override
        public String toString() {
            return "Show{" +
                    "path='" + path + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
