package kr.map.goci;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.map.commons.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by kiost on 2017-07-09.
 */
@CrossOrigin(origins = "*")
@RestController
@Slf4j
@Transactional
public class MapController {
    @Autowired
    private LogService logService;

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
    public ResponseEntity makeCrop(@RequestBody He5.Attributes he5) throws InterruptedException {
        try {
            mapService.makeCroppedData(he5);
        } catch (IOException e) {
            log.error(e.toString());
            return new ResponseEntity<>(new ErrorResponse("IO 에러", "bad.request"), HttpStatus.BAD_REQUEST);
        }

        String result = null;
        try {
            result = objectMapper.writeValueAsString(he5);
        } catch (JsonProcessingException e) {
            log.error(e.toString());
            return new ResponseEntity<>(new ErrorResponse("파싱 실패", "failed.parsing"), HttpStatus.BAD_REQUEST);
        }


        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    //satellite data 다운로드 파일 이름, 생성 날짜 xxxx-x-x , 산출물 종류(CDOM, TSS), 파일 종류(nc, he5)
    @GetMapping(value = "/api/satelliteData/path/{path}/name/{name}/outputType/{outputType}/fileType/{fileType}")
    public ResponseEntity getCroppedData(@PathVariable String path, @PathVariable String name, @PathVariable String outputType, @PathVariable String fileType, HttpServletRequest request) {
        File file = mapService.downloadFile(path, name, outputType, fileType);
        DownLog downInfo = logService.insertDownLog(request.getRemoteAddr(), name, outputType, fileType);
        log.info("download log is {}", downInfo.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-msdownload"));
        headers.setContentLength(file.length());
        headers.setContentDispositionFormData("attachment", file.getName());

        try {
            return new ResponseEntity<>(new InputStreamResource(new FileInputStream(file)), headers, HttpStatus.OK);
        } catch (FileNotFoundException e) {
            log.error(e.toString());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(NoSuchFileException.class)
    public ResponseEntity handleNoSuchFileException(NoSuchFileException e) {

        return new ResponseEntity<>(new ErrorResponse("해당 파일이 없습니다.", "no.such.file.exception"), HttpStatus.BAD_REQUEST);
    }
}