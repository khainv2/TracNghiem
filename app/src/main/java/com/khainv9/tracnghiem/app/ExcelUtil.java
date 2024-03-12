package com.khainv9.tracnghiem.app;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.khainv9.tracnghiem.models.ExamResult;
import com.khainv9.tracnghiem.models.Student;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
//import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ExcelUtil {
    public static class Result {
        public boolean success = false;
        public String message = "";
        public List<Student> students = new ArrayList<>();
    }

    public static boolean writeExcel(Context context, String path, List<ExamResult> examResults){
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet
                    = workbook.createSheet("Diem thi");
            Map<String, Object[]> data
                    = new TreeMap<String, Object[]>(); // tạo một map để lưu dữ liệu

            data.put("0", new Object[] { "Mã số", "Họ tên", "Lớp", "Mã đề", "Điểm" });

            for (int i = 0; i < examResults.size(); i++) {
                ExamResult examResult = examResults.get(i);
                Student student = DatabaseManager.getStudentById(examResult.studentId);
                data.put(i + "", new Object[] { student.id, student.name, student.iclass, examResult.questionPaperCode, examResult.score });
            }
            // Iterating over data and writing it to sheet
            Set<String> keyset = data.keySet();
            int rownum = 0;
            for (String key : keyset) {
                // Creating a new row in the sheet
                Row row = sheet.createRow(rownum++);
                Object[] objArr = data.get(key);
                int cellnum = 0;
                for (Object obj : objArr) {
                    Cell cell = row.createCell(cellnum++);
                    if (obj instanceof String)
                        cell.setCellValue((String)obj);
                    else if (obj instanceof Integer)
                        cell.setCellValue((Integer)obj);
                }
            }

            Log.d("MyLog", "writeExcel: " + Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + path);
            File file = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + path);
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Result readExcel(String path){
        Result result = new Result();
        try {
            Workbook workbook = WorkbookFactory.create(new File(path));
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                Iterator<Cell> cellIterator = row.cellIterator();
                if (row.getRowNum() == 0) continue;
                Student student = new Student();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    String val = "";
                    if (cell.getCellType() == CellType.NUMERIC){
                        val = String.valueOf((int) cell.getNumericCellValue());
                    } else if (cell.getCellType() == CellType.STRING){
                        val = cell.getStringCellValue();
                    }

                    switch (cell.getColumnIndex()) {
                        case 0: student.id = val; break;
                        case 1: student.name = val; break;
                        case 2: student.iclass = val; break;
                    }
                }
                while (student.id.length() < 6)
                    student.id = "0" + student.id;

                result.students.add(student);
            }
            result.success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
