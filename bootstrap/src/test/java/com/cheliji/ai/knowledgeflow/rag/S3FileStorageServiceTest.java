package com.cheliji.ai.knowledgeflow.rag;


import com.cheliji.ai.knowledgeflow.rag.dto.StoredFileDTO;
import com.cheliji.ai.knowledgeflow.rag.service.impl.S3FileStorageService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;

@Slf4j
@SpringBootTest
public class S3FileStorageServiceTest {

    @Autowired
    private S3FileStorageService s3FileStorageService;
    private static final Tika tika = new Tika();

    @Test
    @SneakyThrows
    public void fileUpload() throws FileNotFoundException {

        String fileAddress = "D:\\Users\\Administrator\\Desktop\\新建文本文档.pdf" ;

        File file = new File(fileAddress);
        long size = file.length();
        String fileName = file.getName();


        String detect ;
        try (InputStream is = new FileInputStream(file)) {
            detect = tika.detect(is, fileName);
        }

        StoredFileDTO knowledge ;

        try (InputStream is = new FileInputStream(file)) {
            knowledge = s3FileStorageService.upload("knowledge", is, size, fileName,detect );
        }

        System.out.println(knowledge);


    }

    @Test
    public void delete() {
        s3FileStorageService.deleteByUrl("s3://knowledge/a3523d5cff494d53b41c26a08becba1e.pdf");
    }

}
