package kr.map.goci;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by kiost on 2017-07-09.
 */
@RestController
@Slf4j
public class MapController {
    @GetMapping("/home")
    public ResponseEntity home() {
        return new ResponseEntity<>("Hello Spring", HttpStatus.OK);
    }
}
