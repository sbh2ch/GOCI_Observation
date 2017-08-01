package kr.map.goci;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DownLog {
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private String filename;
    private String fileType;
    private String outputType;
    private String ip;
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
}