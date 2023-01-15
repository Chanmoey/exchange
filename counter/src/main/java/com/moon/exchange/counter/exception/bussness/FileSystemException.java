package com.moon.exchange.counter.exception.bussness;

import lombok.Getter;

/**
 * @author Chanmoey
 * @date 2023年01月04日
 */
@Getter
public class FileSystemException extends BusinessException{
    public FileSystemException(int code) {
        this.code = code;
    }
}
