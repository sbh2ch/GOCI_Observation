package kr.map.goci;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
public class DownLog {
    @Id
    @GeneratedValue
    private Long id;
    private String filename;
    private String ip;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
}