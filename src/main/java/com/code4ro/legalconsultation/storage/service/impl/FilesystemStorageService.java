package com.code4ro.legalconsultation.storage.service.impl;

import com.code4ro.legalconsultation.storage.service.StorageApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@Profile("dev")
public class FilesystemStorageService implements StorageApi {

    @Value("${storage.filesystem.directory}")
    private String customStoreDirPath;

    private File storeDir;

    @PostConstruct
    private void prepareStoreDir() {
        final String home = System.getProperty("user.home");
        storeDir = new File(home, customStoreDirPath);
        if (!storeDir.exists()) {
            if(!storeDir.mkdir()){
                log.error("Unable to create directory, could be a permission issue: {}", storeDir.getName());

                try {
                    ProcessBuilder pb =
                            new ProcessBuilder("sudo", "mkdir", storeDir.getAbsolutePath());
                    Process process = pb.start();
                    process.waitFor();
                } catch(IOException | InterruptedException exception){
                    log.error("Unable to create directory: {}", storeDir.getAbsolutePath());
                }

            }
        }
    }

    @Override
    public String storeFile(final MultipartFile document) throws IOException, IllegalStateException {
        // add a random string to each file in order to avoid duplicates
        final String fileName = StorageApi.resolveUniqueName(document);

        try {
            if (!storeDir.exists()) {
                log.error("The directory does not exist: {}", storeDir.getName());
                ProcessBuilder pb =
                        new ProcessBuilder("sudo", "mkdir", storeDir.getAbsolutePath());
                Process process = pb.start();
                int exitCode = process.waitFor();
                log.debug("Process ended with exit status: {}", exitCode);
            }
        } catch(InterruptedException | IOException exception){
            log.error("Error creating directory: {}", storeDir.getAbsolutePath());
            throw new IOException("The directory can not be created");
        }

        final Path filepath = Paths.get(storeDir.getAbsolutePath(), fileName);
        try {
            document.transferTo(filepath);
        } catch (IOException | IllegalStateException exception) {
            log.error("Error transferring file to filesystem {}", document.getName(), exception);
            throw exception;
        }
        return filepath.toString();
    }

    @Override
    public byte[] loadFile(final String documentURI)  {
        try {
            return Files.readAllBytes(Paths.get(documentURI));
        } catch (IOException e) {
            throw new RuntimeException("Load File fail");
        }
    }

    @Override
    public void deleteFile(String documentURI)  {
        try {
            Files.delete(Paths.get(documentURI));
        } catch (IOException e) {
            throw  new RuntimeException();
        }
    }
}
