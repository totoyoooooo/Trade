package com.second.hand.trading.server.service.impl;

import com.second.hand.trading.server.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class FileServiceImpl implements FileService {

    private String userFilePath = System.getProperty("user.dir") + File.separator + "pic";

    public boolean uploadFile(MultipartFile multipartFile,String fileName)throws IOException {
        File fileDir = new File(userFilePath);
        System.out.println("Attempting to upload file: " + fileName);
        System.out.println("Target directory: " + fileDir.getAbsolutePath());
        if (!fileDir.exists()) {
            System.out.println("Target directory does not exist, attempting to create: " + fileDir.getAbsolutePath());
            if (!fileDir.mkdirs()) {
                System.out.println("Failed to create directory: " + fileDir.getAbsolutePath());
                return false;
            }
            System.out.println("Directory created successfully: " + fileDir.getAbsolutePath());
        }
        File file = new File(fileDir.getAbsolutePath() +"/"+fileName);
        System.out.println("Target file path: " + file.getAbsolutePath());
        if (file.exists()) {
            System.out.println("File already exists, attempting to delete: " + file.getAbsolutePath());
            if (!file.delete()) {
                System.out.println("Failed to delete existing file: " + file.getAbsolutePath());
                return false;
            }
            System.out.println("Existing file deleted successfully: " + file.getAbsolutePath());
        }
        if (file.createNewFile()) {
            System.out.println("Empty file created successfully: " + file.getAbsolutePath());
            multipartFile.transferTo(file);
            System.out.println("File content transferred successfully: " + file.getAbsolutePath());
            return true;
        }
        System.out.println("Failed to create new file: " + file.getAbsolutePath());
        return false;
    }
}
