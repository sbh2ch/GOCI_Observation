package kr.map.goci;

import lombok.Data;

/**
 * Created by kiost on 2017-07-14.
 */
@Data
public class Crop {
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
