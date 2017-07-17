package kr.map.goci;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xpath.internal.operations.Mod;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by kiost on 2017-07-09.
 */
@RestController
@Slf4j
public class MapController {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/api/{date}/{pos}/{zoom}/{type}")
    public ResponseEntity getValue(@PathVariable String date, @PathVariable String pos, @PathVariable String zoom, @PathVariable String type) throws Exception {
        String[] dateInfo = date.split("-");
        Scanner scan = new Scanner(new File("C:/mat/output/" + dateInfo[0] + "/" + dateInfo[1] + "/" + dateInfo[2] + "/" + dateInfo[3] + "/" + type + "/" + zoom + "/" + pos + ".db"));
        String txt = "";

        int x = 0;
        int y = 0;
        String[][] arr = new String[100][100];

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
        return new ResponseEntity<>(objectMapper.writeValueAsString(arr), HttpStatus.OK);
    }

    @GetMapping("/api/lonlat/{pos}/{zoom}")
    public ResponseEntity getLatLon(@PathVariable String pos, @PathVariable String zoom) throws Exception {
        String[] path = {"C:/mat/output/lon" + zoom + "/" + pos + ".db", "C:/mat/output/lat" + zoom + "/" + pos + ".db"};
        String[][][] arr = new String[2][100][100];

        for (int i = 0; i < 2; i++) {
            Scanner scan = new Scanner(new File(path[i]));
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
        return new ResponseEntity<>(objectMapper.writeValueAsString(arr), HttpStatus.OK);
    }

    @GetMapping(value = "/api/image/path/{path}/name/{name}")
    public ResponseEntity getImage(@PathVariable String path, @PathVariable String name) throws Exception {
        log.info("C:/OUT_IMAGE/" + path.replaceAll("-", "/") + "/" + name);
        File imgPath = new File("C:/OUT_IMAGE/" + path.replaceAll("-", "/") + "/" + name + ".JPG");
        byte[] image = Files.readAllBytes(imgPath.toPath());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(image.length);

        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

    @PostMapping(value = "/api/crop", produces = "application/json;charset=UTF-8")
    public ResponseEntity image(@RequestBody Crop.Request crop) throws Exception {
        String[] dates = crop.getDate().split("-");
        for (int i = 1; i < dates.length; i++) {
            if (dates[i].length() == 1)
                dates[i] = "0" + dates[i];
        }

        BufferedImage originalImage = ImageIO.read(new File("C:\\ORI_IMAGE\\" + dates[0] + "\\" + dates[1] + "\\" + dates[2] + "\\" + "COMS_GOCI_L2A_GA_" + dates[0] + dates[1] + dates[2] + dates[3] + "." + crop.getType() + ".JPG"));
        BufferedImage subImage = originalImage.getSubimage(crop.getStartX(), crop.getStartY(), crop.getEndX() - crop.getStartX(), crop.getEndY() - crop.getStartY());
        String[] now = new SimpleDateFormat("yyyy-MM-dd-ssSSS").format(new Date()).split("-");
        String path = now[0] + "/" + now[1] + "/" + now[2];
        String name = now[3] + (Math.random() * 100) + ".JPG";
        Links links = new Links("download Image", "http://localhost:8080/api/image/path/" + path.replaceAll("/", "-") + "/name/" + name);

        File mkdir = new File("C:/OUT_IMAGE/" + path);
        if (!mkdir.exists()) {
            mkdir.mkdirs();
        }

        File outputFile = new File("C:/OUT_IMAGE/" + path + "/" + name);
        ImageIO.write(subImage, "jpg", outputFile);
        Crop.Response res = new Crop.Response(path, name, links);

        return new ResponseEntity<>(objectMapper.writeValueAsString(res), HttpStatus.OK);
    }


    @PostMapping(value = "/api/image", produces = "application/json;charset=UTF-8")
    public ResponseEntity makeCrop(@RequestBody He5 he5) throws Exception {
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
        log.info("param : " + params);
        return new ResponseEntity<>(objectMapper.writeValueAsString(he5), HttpStatus.OK);
    }
}