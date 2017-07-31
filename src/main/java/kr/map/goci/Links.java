package kr.map.goci;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by kiost on 2017-07-17.
 */
@Data
@AllArgsConstructor
public class Links {
    String rel;
    String href;
    String httpMethodType;
}
