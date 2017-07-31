package kr.map.goci;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

@Service
@Slf4j
public class MapService {
    @Autowired
    private DownLogRepository downLogRepository;

    private String SERVER_NAME = "http://localhost:9090";

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
        File imgPath = new File("C:/OUT_IMAGE/" + path.replaceAll("-", "/") + "/" + name + ".JPG");
        byte[] result = Files.readAllBytes(imgPath.toPath());
        log.info("path : {} name : {}", path, name);

        return result;
    }

    public Crop.Response makeImage(Crop.Request crop) throws IOException {
        String[] dates = crop.getDate().split("-");
        for (int i = 1; i < dates.length; i++) {
            if (dates[i].length() == 1)
                dates[i] = "0" + dates[i];
        }

        BufferedImage originalImage = ImageIO.read(new File("C:\\ORI_IMAGE\\" + dates[0] + "\\" + dates[1] + "\\" + dates[2] + "\\" + "COMS_GOCI_L2A_GA_" + dates[0] + dates[1] + dates[2] + dates[3] + "." + crop.getType() + ".JPG"));
        BufferedImage subImage = originalImage.getSubimage(crop.getStartX(), crop.getStartY(), crop.getEndX() - crop.getStartX(), crop.getEndY() - crop.getStartY());
        String[] now = new SimpleDateFormat("yyyy-MM-dd-ssSSS").format(new Date()).split("-");
        String path = now[0] + "/" + now[1] + "/" + now[2];
        String name = now[3] + "_" + (Math.random() * 100) + ".JPG";
        Links imgLink = new Links("download Image", SERVER_NAME + "/api/image/path/" + path.replaceAll("/", "-") + "/name/" + name, "GET");
        Links downLink = new Links("make Satellite Data", SERVER_NAME + "/api/satelliteData", "POST");

        File mkdir = new File("C:/OUT_IMAGE/" + path);

        if (!mkdir.exists()) {
            mkdir.mkdirs();
        }

        File outputFile = new File("C:/OUT_IMAGE/" + path + "/" + name);
        ImageIO.write(subImage, "jpg", outputFile);
        return new Crop.Response(path, name, imgLink, new He5.Response(crop, downLink));
    }


    public DownLog insertDownLog(String ip, String fileName) {
        DownLog downLog = new DownLog();
        downLog.setDate(new Date());
        downLog.setFilename(fileName);
        downLog.setIp(ip);

        return downLogRepository.save(downLog);
    }
}