package com.bit.yourmine.service;

import com.bit.yourmine.domain.files.Files;
import com.bit.yourmine.domain.files.FilesRepository;
import com.bit.yourmine.service.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Service s3Service;
    private final FilesRepository filesRepository;

    private String getSaveFileName(String extName, String rootName) {
        String fileName = rootName+"_";

        Calendar calendar = Calendar.getInstance();
        fileName += calendar.get(Calendar.YEAR);
        fileName += calendar.get(Calendar.MONTH);
        fileName += calendar.get(Calendar.DATE);
        fileName += calendar.get(Calendar.HOUR);
        fileName += calendar.get(Calendar.MINUTE);
        fileName += calendar.get(Calendar.SECOND);
        fileName += calendar.get(Calendar.MILLISECOND);
        fileName += extName.toLowerCase();

        return fileName;
    }

    public String fileSave(MultipartFile profile, String filePath) {
        try {
            String origin = profile.getOriginalFilename();
            String nameCut = origin.substring(origin.lastIndexOf("."));
            String saveFileName = getSaveFileName(nameCut, filePath);

            if(!profile.isEmpty())
            {
                return s3Service.upload(profile, saveFileName);
            }
        }catch(Exception e)
        {
            e.getCause();
        }
        return null;
    }

    public List<String> getFiles(Long id) {
        List<Files> filesList = filesRepository.findAllByPostsId(id);
        List<String> fileName = new ArrayList<>();
        for (Files files: filesList) {
            fileName.add(files.getFileName());
        }
        return fileName;
    }

    public void delFiles(String name) {
        String[] nameList = name.split("&");
        for (String fileName: nameList) {
                filesRepository.delete(filesRepository.findByFileName(fileName));
        }
    }
}
