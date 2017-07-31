package kr.map.goci;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Scanner;

@Service
@Slf4j
public class MapService {
    public String[][] getValue(String date, String pos, String zoom, String type) throws FileNotFoundException {
        String[] dateInfo = date.split("-");
        String[][] arr;
        Scanner scan = new Scanner(new File("C:/mat/output/" + dateInfo[0] + "/" + dateInfo[1] + "/" + dateInfo[2] + "/" + dateInfo[3] + "/" + type + "/" + zoom + "/" + pos + ".db"));
        String txt = "";

        int x = 0;
        int y = 0;
        arr = new String[100][100];

        while (scan.hasNext()) {
            txt = scan.nextLine();
            if (txt.equals("NaN"))
                txt = "";

            arr[x][y++] = txt;

            if (y == 100) {
                x++;
                y = 0;
            }
        }
        return arr;
    }

    public String[][][] getLonLat(String pos, String zoom) throws FileNotFoundException {
        String[] path = {"C:/mat/output/lon" + zoom + "/" + pos + ".db", "C:/mat/output/lat" + zoom + "/" + pos + ".db"};
        String[][][] arr = new String[2][100][100];

        for (int i = 0; i < 2; i++) {
            Scanner scan;
            scan = new Scanner(new File(path[i]));
            String txt = "";
            int x = 0;
            int y = 0;
            while (scan.hasNext()) {
                txt = scan.nextLine();

                arr[i][x][y++] = txt;
                if (y == 100) {
                    x++;
                    y = 0;
                }
            }
        }
        return arr;
    }

    public byte[] displayImage(String path, String name) throws IOException {
        log.info("path : {} name : {}", path, name);
        File imgPath = new File("C:/OUT_IMAGE/" + path.replaceAll("-", "/") + "/" + name + ".JPG");

        return Files.readAllBytes(imgPath.toPath());
    }
}