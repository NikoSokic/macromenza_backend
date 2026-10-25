package com.macromenza.macromenza.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

/** Prevelik upload se odbija prije kontrolera, pa ga ovdje pretvaramo u 400 s porukom (slike ≤ 8 MB). */
@RestControllerAdvice
public class MultipartGreskeAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> prevelikaSlika(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("poruka", "Slika može imati najviše 8 MB."));
    }
}
