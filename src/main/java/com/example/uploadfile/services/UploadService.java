package com.example.uploadfile.services;

import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.example.uploadfile.util.UploadUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadService {

    private final UploadUtil uploadUtil;

    public UploadService(UploadUtil uploadUtil) {
        this.uploadUtil = uploadUtil;
    }

    public List<Map<String, String>> upload(MultipartFile file) throws Exception {

        Path tempDir = Files.createTempDirectory("");

        File tempFile = tempDir.resolve(file.getOriginalFilename()).toFile();

        file.transferTo(tempFile);

        Workbook workbook = WorkbookFactory.create(tempFile);

        Sheet sheet = workbook.getSheetAt(0);

        Supplier<Stream<Row>> rowStreamSupplier = uploadUtil.getRowStreamSupplier(sheet);



        Row headerRow = rowStreamSupplier.get().findFirst().get();

        List<String> headerCells = uploadUtil.getStream(headerRow)
                //.map(Cell::getNumericCellValue)
                .map(String::valueOf)
                .collect(Collectors.toList());

        //List<String> stringList = new ArrayList<String>();


//        if(headerRow == Cell.CELL_TYPE_NUMERIC)
//            //your code
//        else if(MytempCell.getCellType() == Cell.CELL_TYPE_STRING)
//            your code

        int colCount = headerCells.size();

        //System.out.println("headerCells -> "+ headerCells);



        return rowStreamSupplier.get()
                .skip(1)
                .map(row -> {

                    List<String> cellList = uploadUtil.getStream(row)
                            //.map(Cell::getStringCellValue)
                            .map(String::valueOf)
                            .collect(Collectors.toList());

//                    for (String val: cellList){
//                        val = val.toString();
//                        stringList.add(val);
//                    }

                    return uploadUtil.cellIteratorSupplier(colCount-2)
                            .get()
                            .collect(toMap(headerCells::get, cellList::get));
                })
                .collect(Collectors.toList());
    }

}
