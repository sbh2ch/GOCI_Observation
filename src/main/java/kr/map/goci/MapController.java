package kr.map.goci;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.map.commons.ErrorResponse;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by kiost on 2017-07-09.
 */
@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class MapController {
    private String SERVER_NAME = "http://localhost:9090";

    @Autowired
    private MapService mapService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/api/{date}/{pos}/{zoom}/{type}")
    public ResponseEntity getValue(@PathVariable String date, @PathVariable String pos, @PathVariable String zoom, @PathVariable String type) {
        String result;
        try {
            String[][] arr = mapService.getValue(date, pos, zoom, type);
            result = objectMapper.writeValueAsString(arr);
        } catch (FileNotFoundException e) {
            log.error("File Not Found Exception! {}", e.toString());
            return new ResponseEntity<>(new ErrorResponse("잘못된 요청입니다.", "bad.request"), HttpStatus.BAD_REQUEST);
        } catch (JsonProcessingException e) {
            log.error("Json Parsing Error! {}", e.toString());
            return new ResponseEntity<>(new ErrorResponse("파싱 실패", "failed.parsing"), HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/api/lonlat/{pos}/{zoom}")
    public ResponseEntity getLatLon(@PathVariable String pos, @PathVariable String zoom) {
        String[] path = {"C:/mat/output/lon" + zoom + "/" + pos + ".db", "C:/mat/output/lat" + zoom + "/" + pos + ".db"};
        String result;

        try {
            String[][][] arr = mapService.getLonLat(pos, zoom);
            result = objectMapper.writeValueAsString(arr);
        } catch (FileNotFoundException e) {
            log.error("File Not Found Exception! {}", e.toString());
            return new ResponseEntity<>(new ErrorResponse("잘못된 요청입니다.", "bad.request"), HttpStatus.BAD_REQUEST);
        } catch (JsonProcessingException e) {
            log.error("Json Parsing Error! {}", e.toString());
            return new ResponseEntity<>(new ErrorResponse("파싱 실패", "failed.parsing"), HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    //이미지 display
    @GetMapping(value = "/api/image/path/{path}/name/{name}")
    public ResponseEntity displayImage(@PathVariable String path, @PathVariable String name) {
        byte[] image;

        try {
            image = mapService.displayImage(path, name);
        } catch (IOException e) {
            log.error("image.file.IO.Exception,{}", e.toString());
            return new ResponseEntity<>(new ErrorResponse("잘못된 요청입니다.", "bad.request"), HttpStatus.BAD_REQUEST);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        headers.setContentLength(image.length);

        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

    //이미지 만들고 HATEOAS -> image display, satellite data make link
    @PostMapping(value = "/api/image", produces = "application/json;charset=UTF-8")
    public ResponseEntity makeImage(@RequestBody Crop.Request crop) throws Exception {
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
        Crop.Response res = new Crop.Response(path, name, imgLink, new He5.Response(crop, downLink));

        return new ResponseEntity<>(objectMapper.writeValueAsString(res), HttpStatus.OK);
    }


    //satellite data 만들고 다운로드 HATEOAS -> satellite data download link
    @PostMapping(value = "/api/satelliteData", produces = "application/json;charset=UTF-8")
    public ResponseEntity makeCrop(@RequestBody He5.Attributes he5) throws Exception {
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