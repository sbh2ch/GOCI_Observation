package kr.map.goci;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

@Service
@Slf4j
public class MapService {
    private String SERVER_NAME = "http://localhost:9090";
    private String OUTPUT_FILE_PATH = "C:/output/goci/";

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

    public void makeCroppedData(He5.Attributes he5) throws IOException, InterruptedException {
        String[] dates = he5.getDate().split("-");
        StringBuilder dateParams = new StringBuilder();
        Arrays.stream(dates).forEach(date -> dateParams.append((date.length() == 1 ? "0" + date : date) + " "));

        String name = new SimpleDateFormat("yyMMddHHmmssSS").format(new Date());
        String params = dateParams + he5.getType() + " " + name + " " + he5.getStartX() + " " + he5.getEndX() + " " + he5.getStartY() + " " + he5.getEndY() + " C:\\";
        // he5일때
        if (he5.getOutputType().equals("he5")) {
            Runtime.getRuntime().exec("C:\\mat\\crop\\distrib\\testing.exe " + params).waitFor();
        } else {
            //todo NetCDF convert Code
            log.info("NetCDF convert!");
        }

        log.info("created he5 : " + name + "_" + dates[0] + dates[1] + dates[2] + dates[3] + he5.getType() + "." + (he5.getOutputType().equals("he5") ? "he5" : "nc"));
    }

    public File downloadFile(String path, String name, String outputType, String fileType) {
        String filePath = OUTPUT_FILE_PATH + path.replaceAll("-", "/") + "/" + name + "." + outputType + "." + fileType;
        log.info(filePath);
        File downFile = new File(filePath);

        if (!downFile.exists()) {
            throw new NoSuchFileException(filePath);
        }

        return downFile;
    }
}