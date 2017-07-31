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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
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
@Transactional
public class MapController {


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
    public ResponseEntity makeImage(@RequestBody Crop.Request crop) {

        Crop.Response res = null;
        try {
            res = mapService.makeImage(crop);
        } catch (IOException e) {
            log.error(e.toString());
            return new ResponseEntity<>(new ErrorResponse("잘못된 요청입니다.", "bad.request"), HttpStatus.BAD_REQUEST);
        }

        String result = null;
        try {
            result = objectMapper.writeValueAsString(res);
        } catch (JsonProcessingException e) {
            log.error(e.toString());
            return new ResponseEntity<>(new ErrorResponse("파싱 실패", "failed.parsing"), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
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
        return new ResponseEntity<>(objectMapper.writeValueAsString(he5), HttpStatus.OK);
    }

    @GetMapping(value = "/api/satelliteData/path/{path}/name/{name}")
    public ResponseEntity getCrop(@PathVariable String path, @PathVariable String name) throws Exception {


        return null;
    }

    @PostMapping(value = "/api/insertTest")
    public ResponseEntity insertLog(HttpServletRequest request) throws Exception {
        DownLog downLog = mapService.insertDownLog(request.getRemoteAddr(), "test");
        return new ResponseEntity<>(objectMapper.writeValueAsString(downLog), HttpStatus.OK);
    }
}