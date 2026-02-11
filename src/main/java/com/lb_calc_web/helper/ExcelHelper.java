package com.lb_calc_web.helper;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.service.ALSImageService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;


public class ExcelHelper {
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static ByteArrayInputStream projectToExcel(ProjectDTO project) {

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(project.getName());
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 4000);


            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);

            headerStyle.setTopBorderColor   (IndexedColors.BLACK.getIndex());
            headerStyle.setRightBorderColor (IndexedColors.BLACK.getIndex());
            headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            headerStyle.setLeftBorderColor  (IndexedColors.BLACK.getIndex());
            headerStyle.setAlignment        (HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment  .CENTER);


            XSSFFont font = workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            font.setBold(true);
            headerStyle.setFont(font);
            Row header = sheet.createRow(1);
            Cell headerCell = header.createCell(0);

            headerCell.setCellValue("Заказчик");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(1);
            headerCell.setCellValue("Дата создания");
            headerCell.setCellStyle(headerStyle);

            headerCell = header.createCell(2);
            headerCell.setCellValue("Дата обновления");
            headerCell.setCellStyle(headerStyle);

            Row row = sheet.createRow(2);
            headerCell = row.createCell(0);

            headerCell.setCellValue(project.getCompany());
            headerCell.setCellStyle(headerStyle);

            headerCell = row.createCell(1);
            headerCell.setCellValue(project.getCreatedDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            headerCell.setCellStyle(headerStyle);

            headerCell = row.createCell(2);
            headerCell.setCellValue(project.getUpdatedDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            headerCell.setCellStyle(headerStyle);

            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);

            style.setTopBorderColor   (IndexedColors.BLACK.getIndex());
            style.setRightBorderColor (IndexedColors.BLACK.getIndex());
            style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
            style.setLeftBorderColor  (IndexedColors.BLACK.getIndex());
            style.setAlignment        (HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment  .CENTER);

            int rowNum = 1;
            for (Map.Entry<ALSDTO, Integer> entry : project.getQuantityALS().entrySet()) {
                    Row rowAls = sheet.createRow(rowNum+2);
                    Cell cell = rowAls.createCell(0);
                    cell.setCellValue(rowNum);
                    cell.setCellStyle(style);

                    cell = rowAls.createCell(1);
                    cell.setCellValue(entry.getKey().getName());
                    cell.setCellStyle(style);

                    cell = rowAls.createCell(2);
                    cell.setCellValue(entry.getValue().toString());
                    cell.setCellStyle(style);

                    rowAls =sheet.createRow(rowNum+3);
                    cell = rowAls.createCell(0);
                    byte[] alsImageBytes= ALSImageService.getBytesArrayALSImage(entry.getKey());
                    int alsImageId=workbook.addPicture(alsImageBytes, Workbook.PICTURE_TYPE_PNG);
                    XSSFDrawing drawing=(XSSFDrawing) sheet.createDrawingPatriarch();
                    XSSFClientAnchor alsAnchor=new XSSFClientAnchor();
                    alsAnchor.setCol1(0);
                    alsAnchor.setCol2(1);
                    alsAnchor.setRow1(rowNum+3);
                    alsAnchor.setRow2(rowNum+4);
                    drawing.createPicture(alsAnchor, alsImageId);
                    cell.setCellStyle(style);

                    style.setAlignment        (HorizontalAlignment.LEFT);
                    cell=rowAls.createCell(1);
                    cell.setCellValue(entry.getKey().getDescription());
                    cell.setCellStyle(style);
                    style.setAlignment      (HorizontalAlignment.CENTER);
                    rowNum=rowNum+2;
                }
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
        }
    }
}
