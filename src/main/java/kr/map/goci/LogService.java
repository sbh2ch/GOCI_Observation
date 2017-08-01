package kr.map.goci;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class LogService {
    @Autowired
    private DownLogRepository logRepository;

    public DownLog insertDownLog(String ip, String fileName, String outputType, String fileType) {
        DownLog downLog = new DownLog();
        downLog.setDate(new Date());
        downLog.setFilename(fileName);
        downLog.setIp(ip);
        downLog.setFileType(fileType);
        downLog.setOutputType(outputType);

        return logRepository.save(downLog);
    }
}
