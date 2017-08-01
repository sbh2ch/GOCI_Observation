package kr.map.goci;

import lombok.Getter;

@Getter
public class NoSuchFileException extends RuntimeException {
    private String filename;

    public NoSuchFileException(String filename) {
        this.filename = filename;
    }
}
