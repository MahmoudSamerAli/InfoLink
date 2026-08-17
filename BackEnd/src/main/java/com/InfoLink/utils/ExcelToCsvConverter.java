package com.InfoLink.utils;
import org.apache.poi.ss.usermodel.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class ExcelToCsvConverter {

    // =========================================================
    // 1. Excel File -> CSV
    // =========================================================
    public static void convertExcelToCsv(String inputPath, String outputPath)
            throws IOException {

        File inputFile = new File(inputPath);

        // Validate file
        if (!inputFile.exists()) {
            throw new FileNotFoundException(
                    "File does not exist: " + inputPath
            );
        }

        if (!inputFile.isFile()) {
            throw new IOException("The path is not a valid file.");
        }

        String fileName = inputFile.getName().toLowerCase();

        if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
            throw new IOException(
                    "Invalid Excel format. Only .xls and .xlsx are supported."
            );
        }

        try (Workbook workbook = WorkbookFactory.create(inputFile);
             BufferedWriter writer = Files.newBufferedWriter(
                     Paths.get(outputPath),
                     StandardCharsets.UTF_8)) {

            // Get first worksheet
            Sheet sheet = workbook.getSheetAt(0);

            DataFormatter formatter = new DataFormatter();

            // Get number of columns from first row
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new IOException("Excel file is empty.");
            }

            int numberOfColumns = headerRow.getLastCellNum();

            // =================================================
            // Write Headers
            // =================================================

            for (int i = 0; i < numberOfColumns; i++) {

                Cell cell = headerRow.getCell(i);

                String value = "";

                if (cell != null) {
                    value = formatter.formatCellValue(cell);
                }

                writer.write(escapeCsv(value));

                if (i < numberOfColumns - 1) {
                    writer.write(",");
                }
            }

            writer.newLine();

            // =================================================
            // Write Data Rows
            // =================================================

            for (int rowIndex = 1;
                 rowIndex <= sheet.getLastRowNum();
                 rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null) {
                    writer.newLine();
                    continue;
                }

                for (int columnIndex = 0;
                     columnIndex < numberOfColumns;
                     columnIndex++) {

                    Cell cell = row.getCell(columnIndex);

                    String value = "";

                    if (cell != null) {
                        value = formatter.formatCellValue(cell);
                    }

                    writer.write(escapeCsv(value));

                    if (columnIndex < numberOfColumns - 1) {
                        writer.write(",");
                    }
                }

                writer.newLine();
            }
        }
    }


    // =========================================================
    // 2. CSV Value Protection
    // =========================================================
    private static String escapeCsv(String value) {

        if (value == null) {
            return "";
        }

        // If value contains comma, quote or new line
        if (value.contains(",")
                || value.contains("\"")
                || value.contains("\n")
                || value.contains("\r")) {

            // Replace " with ""
            value = value.replace("\"", "\"\"");

            return "\"" + value + "\"";
        }

        return value;
    }


    // =========================================================
    // 3. ZIP -> Excel Files -> CSV Files -> New ZIP
    // =========================================================
    public static void processZipArchive(
            String inputZipPath,
            String outputZipPath) throws IOException {

        File zipFile = new File(inputZipPath);

        if (!zipFile.exists()) {
            throw new FileNotFoundException(
                    "ZIP file does not exist."
            );
        }

        Path tempDirectory = Files.createTempDirectory(
                "excel_conversion_"
        );

        try {

            // Extract ZIP
            extractZip(inputZipPath, tempDirectory);

            // Find Excel files
            List<Path> excelFiles = findExcelFiles(tempDirectory);

            if (excelFiles.isEmpty()) {
                throw new IOException(
                        "The archive contains no valid Excel files."
                );
            }

            // Create CSV directory
            Path csvDirectory = Files.createDirectory(
                    tempDirectory.resolve("csv_output")
            );

            // Convert every Excel file
            for (Path excelFile : excelFiles) {

                String originalName =
                        excelFile.getFileName().toString();

                String csvName =
                        originalName.substring(
                                0,
                                originalName.lastIndexOf(".")
                        ) + ".csv";

                Path csvPath =
                        csvDirectory.resolve(csvName);

                convertExcelToCsv(
                        excelFile.toString(),
                        csvPath.toString()
                );
            }

            // Create new ZIP
            createZip(
                    csvDirectory,
                    outputZipPath
            );

        } finally {

            // Clean temporary files
            deleteDirectory(tempDirectory);
        }
    }


    // =========================================================
    // 4. Extract ZIP
    // =========================================================
    private static void extractZip(
            String zipPath,
            Path destination) throws IOException {

        try (ZipInputStream zipInputStream =
                     new ZipInputStream(
                             new FileInputStream(zipPath))) {

            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry())
                    != null) {

                Path outputPath =
                        destination.resolve(entry.getName())
                                .normalize();

                // Security check
                if (!outputPath.startsWith(destination.normalize())) {
                    throw new IOException(
                            "Invalid archive entry."
                    );
                }

                if (entry.isDirectory()) {

                    Files.createDirectories(outputPath);

                } else {

                    Files.createDirectories(
                            outputPath.getParent()
                    );

                    try (OutputStream outputStream =
                                 Files.newOutputStream(outputPath)) {

                        byte[] buffer = new byte[8192];

                        int length;

                        while ((length =
                                zipInputStream.read(buffer)) > 0) {

                            outputStream.write(
                                    buffer,
                                    0,
                                    length
                            );
                        }
                    }
                }

                zipInputStream.closeEntry();
            }
        }
    }


    // =========================================================
    // 5. Find Excel Files
    // =========================================================
    private static List<Path> findExcelFiles(
            Path directory) throws IOException {

        List<Path> excelFiles = new ArrayList<>();

        Files.walk(directory)
                .filter(Files::isRegularFile)
                .forEach(path -> {

                    String fileName =
                            path.getFileName()
                                    .toString()
                                    .toLowerCase();

                    if (fileName.endsWith(".xls")
                            || fileName.endsWith(".xlsx")) {

                        excelFiles.add(path);
                    }
                });

        return excelFiles;
    }


    // =========================================================
    // 6. Create ZIP From CSV Files
    // =========================================================
    private static void createZip(
            Path csvDirectory,
            String outputZipPath) throws IOException {

        Path outputPath = Paths.get(outputZipPath);

        Path parent = outputPath.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (ZipOutputStream zipOutputStream =
                     new ZipOutputStream(
                             new FileOutputStream(outputZipPath))) {

            Files.walk(csvDirectory)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {

                        try {

                            ZipEntry entry =
                                    new ZipEntry(
                                            file.getFileName()
                                                    .toString()
                                    );

                            zipOutputStream.putNextEntry(entry);

                            Files.copy(
                                    file,
                                    zipOutputStream
                            );

                            zipOutputStream.closeEntry();

                        } catch (IOException e) {

                            throw new RuntimeException(e);
                        }
                    });
        }
    }


    // =========================================================
    // 7. Delete Temporary Directory
    // =========================================================
    private static void deleteDirectory(
            Path directory) throws IOException {

        if (!Files.exists(directory)) {
            return;
        }

        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {

                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore cleanup errors
                    }
                });
    }
}