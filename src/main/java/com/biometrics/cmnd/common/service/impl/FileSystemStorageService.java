package com.biometrics.cmnd.common.service.impl;

import com.biometrics.cmnd.common.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

@Service
@Slf4j
public class FileSystemStorageService implements StorageService {
    @Override
    public void init() {

    }

    @Override
    public String store(MultipartFile file) {
        return null;
    }

    @Override
    public String store(String encodedBase64String) {
        return null;
    }

    @Override
    public Stream<Path> loadAll() {
        return null;
    }

    @Override
    public Path load(String fileName) {
        return null;
    }

    @Override
    public Resource loadAsResource(String fileName) {
        return null;
    }

    @Override
    public String loadAsBas64EncodedString(String fileName) {
        return null;
    }

    @Override
    public void deleteAll() {

    }
}
