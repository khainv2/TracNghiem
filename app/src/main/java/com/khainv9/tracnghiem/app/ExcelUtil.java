package com.khainv9.tracnghiem.app;

import android.util.Log;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class ExcelUtil {
    public class Result {
        public boolean success;
        public String message;
        List<>
    }

    public static boolean readExcel(String path){
        try {
            Workbook workbook = WorkbookFactory.create(new File(path));
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {

                Row row = rowIterator.next();

                // For each row, iterate through all the
                // columns
                Iterator<Cell> cellIterator
                        = row.cellIterator();

                while (cellIterator.hasNext()) {

                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()){
                        case STRING:
                            Log.d("ExcelUtil", "readExcel: " + cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            Log.d("ExcelUtil", "readExcel: " + cell.getNumericCellValue());
                            break;
                    }
                }
                System.out.println("");
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
