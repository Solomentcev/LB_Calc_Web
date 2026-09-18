package com.lb_calc_web.helper;

import com.lb_calc_web.dto.ALSDTO;
import com.lb_calc_web.dto.LBCDTO;
import com.lb_calc_web.dto.LBDTO;
import com.lb_calc_web.dto.ProjectDTO;
import com.lb_calc_web.service.util.ALSImageService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ExcellHelper {

    private static final Logger logger =
            LoggerFactory.getLogger(ExcellHelper.class);

    private static final DateTimeFormatter DF =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static ByteArrayInputStream projectToExcel(
            ProjectDTO project
    ) {

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet(
                    project.getName()
            );

            initColumns(sheet);
            autosize(sheet, 8);

            Styles s = new Styles(wb);

            XSSFDrawing drawing =
                    (XSSFDrawing)
                            sheet.createDrawingPatriarch();

            int r = 0;

            // ===== TITLE =====

            Row titleRow =
                    sheet.createRow(r++);

            createCell(
                    titleRow,
                    0,
                    project.getName(),
                    s.title
            );

            sheet.addMergedRegion(
                    new CellRangeAddress(
                            0,
                            0,
                            0,
                            7
                    )
            );

            r++;

            // ===== PROJECT INFO =====

            r = section(
                    sheet,
                    "ОБЩАЯ ИНФОРМАЦИЯ",
                    s,
                    r
            );

            r = info(
                    sheet,
                    "Заказчик",
                    project.getCompany(),
                    s,
                    r
            );

            r = info(
                    sheet,
                    "Описание",
                    project.getDescription(),
                    s,
                    r
            );

            String createdBy =
                    project.getCreatedBy() == null
                            ? ""
                            : project.getCreatedBy().getLastName()
                              + " "
                              + project.getCreatedBy().getFirstName();

            r = info(
                    sheet,
                    "Создал",
                    createdBy,
                    s,
                    r
            );

            r = info(
                    sheet,
                    "Дата",
                    project.getCreatedAt() == null
                            ? ""
                            : project.getCreatedAt().format(DF),
                    s,
                    r
            );

            r += 2;

            // ===== ALS =====

            int index = 1;

            for (
                    Map.Entry<ALSDTO, Integer> entry
                    : project.getQuantityALS().entrySet()
            ) {

                r = alsBlock(
                        sheet,
                        wb,
                        drawing,
                        entry.getKey(),
                        entry.getValue(),
                        index++,
                        s,
                        r
                );
            }

            autosize(sheet, 8);

            wb.write(out);

            return new ByteArrayInputStream(
                    out.toByteArray()
            );

        } catch (Exception e) {

            logger.error(
                    "Не удалось создать Excel файл",
                    e
            );

            throw new RuntimeException(
                    "Ошибка создания Excel файла",
                    e
            );
        }
    }

    // ================= ALS BLOCK =================

    private static int alsBlock(
            Sheet sheet,
            Workbook wb,
            XSSFDrawing drawing,
            ALSDTO als,
            Integer qty,
            int index,
            Styles s,
            int r
    ) {

        r = alsHeader(
                sheet,
                als,
                index,
                s,
                r
        );

        r = alsInfo(
                sheet,
                als,
                qty,
                index,
                s,
                r
        );

        r = alsLbTable(
                sheet,
                als,
                s,
                r
        );

        r = addImage(
                sheet,
                wb,
                drawing,
                als,
                r++,
                1,
                8
        );

        return r + 2;
    }

    private static int alsHeader(
            Sheet sheet,
            ALSDTO als,
            int index,
            Styles s,
            int r
    ) {

        Row row = sheet.createRow(r++);

        createCell(
                row,
                0,
                "ALS #" + index + " — " + als.getName(),
                s.section
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        row.getRowNum(),
                        row.getRowNum(),
                        0,
                        7
                )
        );

        return r;
    }

    private static int alsInfo(
            Sheet sheet,
            ALSDTO als,
            Integer qty,
            int index,
            Styles s,
            int r
    ) {

        Row row = sheet.createRow(r++);

        row.setHeightInPoints(90);

        createCell(
                row,
                0,
                String.valueOf(index),
                s.cell
        );

        createCell(
                row,
                1,
                als.getDescription(),
                s.wrap
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        row.getRowNum(),
                        row.getRowNum(),
                        1,
                        3
                )
        );

        createCell(
                row,
                4,
                String.valueOf(qty),
                s.cell
        );

        return r;
    }

    private static int alsLbTable(
            Sheet sheet,
            ALSDTO als,
            Styles s,
            int r
    ) {

        r = lbHeader(
                sheet,
                s,
                r
        );

        int i = 1;

        if (als.getLBC() != null) {
            r = lbcRow(
                    sheet,
                    als.getLBC(),
                    1,
                    i++,
                    s,
                    r
            );
        }

        for (
                Map.Entry<LBDTO, Integer> entry
                : als.getQuantityLB().entrySet()
        ) {

            r = lbRow(
                    sheet,
                    entry.getKey(),
                    entry.getValue(),
                    i++,
                    s,
                    r
            );
        }

        return r + 1;
    }

    private static int lbcRow(
            Sheet sheet,
            LBCDTO lbc,
            Integer qty,
            int index,
            Styles s,
            int r
    ) {
        Row row =
                sheet.createRow(r++);

        createCell(
                row,
                1,
                "LBC " + index,
                s.cell
        );

        createCell(
                row,
                2,
                lbc.getDescription(),
                s.wrap
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        row.getRowNum(),
                        row.getRowNum(),
                        2,
                        3
                )
        );

        createCell(
                row,
                4,
                String.valueOf(qty),
                s.cell
        );

        return r;
    }

    private static int lbRow(
            Sheet sheet,
            LBDTO lb,
            Integer qty,
            int index,
            Styles s,
            int r
    ) {

        Row row =
                sheet.createRow(r++);

        createCell(
                row,
                1,
                String.valueOf(index),
                s.cell
        );

        createCell(
                row,
                2,
                lb.getDescription(),
                s.wrap
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        row.getRowNum(),
                        row.getRowNum(),
                        2,
                        3
                )
        );

        createCell(
                row,
                4,
                String.valueOf(qty),
                s.cell
        );

        return r;
    }

    // ================= LB TABLE =================

    private static int lbHeader(
            Sheet sheet,
            Styles s,
            int r
    ) {

        Row row =
                sheet.createRow(r++);

        createCell(
                row,
                1,
                "Модуль",
                s.tableHeader
        );

        createCell(
                row,
                2,
                "Описание",
                s.tableHeader
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        row.getRowNum(),
                        row.getRowNum(),
                        2,
                        3
                )
        );

        createCell(
                row,
                4,
                "Кол-во",
                s.tableHeader
        );

        return r;
    }

    // ================= IMAGE =================

    private static int addImage(
            Sheet sheet,
            Workbook wb,
            XSSFDrawing drawing,
            ALSDTO als,
            int row,
            int col1,
            int col2
    ) {

        byte[] img =
                ALSImageService.getBytesArrayALSImage(
                        als
                );

        int id =
                wb.addPicture(
                        img,
                        Workbook.PICTURE_TYPE_PNG
                );

        int rows = 32;

        Row row1 =
                sheet.createRow(row);

        row1.createCell(col1)
                .getCellStyle()
                .setVerticalAlignment(
                        VerticalAlignment.BOTTOM
                );

        XSSFClientAnchor anchor =
                new XSSFClientAnchor();

        anchor.setCol1(col1);
        anchor.setRow1(row);

        anchor.setAnchorType(
                ClientAnchor.AnchorType.MOVE_AND_RESIZE
        );

        Picture picture =
                drawing.createPicture(
                        anchor,
                        id
                );

        picture.resize();

        return row + rows;
    }

    // ================= COMMON =================

    private static int section(
            Sheet sheet,
            String title,
            Styles s,
            int r
    ) {

        Row row =
                sheet.createRow(r++);

        createCell(
                row,
                0,
                title,
                s.section
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        row.getRowNum(),
                        row.getRowNum(),
                        0,
                        7
                )
        );

        return r;
    }

    private static int info(
            Sheet sheet,
            String key,
            String value,
            Styles s,
            int r
    ) {

        Row row =
                sheet.createRow(r++);

        createCell(
                row,
                1,
                key,
                s.label
        );

        createCell(
                row,
                2,
                value,
                s.wrap
        );

        return r;
    }

    private static void createCell(
            Row row,
            int col,
            String value,
            CellStyle style
    ) {

        Cell cell =
                row.createCell(col);

        cell.setCellValue(
                value != null
                        ? value
                        : ""
        );

        cell.setCellStyle(style);
    }

    private static void initColumns(
            Sheet sheet
    ) {

        sheet.setColumnWidth(
                0,
                4000
        );

        sheet.setColumnWidth(
                1,
                9000
        );

        for (
                int i = 2;
                i < 8;
                i++
        ) {

            sheet.setColumnWidth(
                    i,
                    4000
            );
        }
    }

    private static void autosize(
            Sheet sheet,
            int count
    ) {

        for (
                int i = 0;
                i < count;
                i++
        ) {

            sheet.autoSizeColumn(i);
        }
    }

    // ================= STYLES =================

    private static class Styles {

        final CellStyle bigTitle;
        final CellStyle title;
        final CellStyle section;
        final CellStyle label;
        final CellStyle wrap;
        final CellStyle tableHeader;
        final CellStyle cell;

        Styles(Workbook wb) {

            Font bold =
                    wb.createFont();

            bold.setBold(true);

            Font big =
                    wb.createFont();

            big.setBold(true);

            big.setFontHeightInPoints(
                    (short) 16
            );

            bigTitle =
                    base(wb);

            bigTitle.setFont(big);

            bigTitle.setAlignment(
                    HorizontalAlignment.CENTER
            );

            title =
                    base(wb);

            title.setFont(bold);

            title.setAlignment(
                    HorizontalAlignment.CENTER
            );

            section =
                    base(wb);

            section.setFont(bold);

            section.setFillForegroundColor(
                    IndexedColors.GREY_40_PERCENT
                            .getIndex()
            );

            section.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );

            label =
                    base(wb);

            label.setFont(bold);

            label.setFillForegroundColor(
                    IndexedColors.GREY_25_PERCENT
                            .getIndex()
            );

            label.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );

            wrap =
                    base(wb);

            wrap.setWrapText(true);

            wrap.setAlignment(
                    HorizontalAlignment.LEFT
            );

            wrap.setVerticalAlignment(
                    VerticalAlignment.TOP
            );

            tableHeader =
                    base(wb);

            tableHeader.setFont(bold);

            tableHeader.setAlignment(
                    HorizontalAlignment.CENTER
            );

            cell =
                    base(wb);

            cell.setAlignment(
                    HorizontalAlignment.LEFT
            );
        }

        private CellStyle base(
                Workbook wb
        ) {

            CellStyle style =
                    wb.createCellStyle();

            style.setBorderTop(
                    BorderStyle.THIN
            );

            style.setBorderBottom(
                    BorderStyle.THIN
            );

            style.setBorderLeft(
                    BorderStyle.THIN
            );

            style.setBorderRight(
                    BorderStyle.THIN
            );

            style.setVerticalAlignment(
                    VerticalAlignment.CENTER
            );

            return style;
        }
    }
}