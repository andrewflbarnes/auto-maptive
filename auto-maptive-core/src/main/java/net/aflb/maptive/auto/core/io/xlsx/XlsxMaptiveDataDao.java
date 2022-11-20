package net.aflb.maptive.auto.core.io.xlsx;

import net.aflb.maptive.auto.core.MaptiveData;
import net.aflb.maptive.auto.core.MaptiveId;
import net.aflb.maptive.auto.core.io.MaptiveDataDao;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

public class XlsxMaptiveDataDao implements MaptiveDataDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(XlsxMaptiveDataDao.class);

    private final String idCol;
    private final String dateTimeFormat;
    private final SimpleDateFormat dtf;
    private final File file;
    private Map<MaptiveId, MaptiveData> data;

    public static XlsxMaptiveDataDao forResource(String file, String idCol, String dateTimeFormat) throws IOException {
        final var f = Optional.of(XlsxMaptiveDataDao.class.getClassLoader())
            .map(cl -> cl.getResource(file))
            .map(URL::getFile)
            .map(File::new)
            .orElseThrow(() -> new IOException("File " + file + " not found"));
        return new XlsxMaptiveDataDao(f, idCol, dateTimeFormat);
    }

    public static XlsxMaptiveDataDao forFile(String file, String idCol, String dateTimeFormat) throws IOException, NotXlsxFileException {
        try {
            return new XlsxMaptiveDataDao(new File(file), idCol,  dateTimeFormat);
        } catch (NotOfficeXmlFileException e) {
            throw new NotXlsxFileException("Not an XLSX file: " + file, e);
        }
    }

    public XlsxMaptiveDataDao(File file, String idCol, String dateTimeFormat) throws IOException {
        this.file = file;
        this.idCol = idCol;
        this.dateTimeFormat = dateTimeFormat;
        this.dtf = new SimpleDateFormat(dateTimeFormat);
//        this.dtf = DateTimeFormatter.ofPattern(dateTimeFormat);
        refresh();
    }

    private void refresh() throws IOException {
        try (final var fis = new FileInputStream(file)) {
            final var workbook = new XSSFWorkbook(fis);
            final var sheet = workbook.getSheetAt(0);
            final var formatter = new DataFormatter();

            final var parseHeaders = new AtomicBoolean(true);
            final List<String> headers = new ArrayList<>();
            final Map<MaptiveId, MaptiveData> items = new HashMap<>();
            StreamSupport.stream(sheet.spliterator(), false)
                .forEach(row -> {
                    if (parseHeaders.get()) {
                        StreamSupport.stream(row.spliterator(), false)
                            .map(Cell::toString)
                            .forEachOrdered(headers::add);
                        parseHeaders.set(false);
                    } else {
                        final List<String> item = new ArrayList<>();
                        StreamSupport.stream(row.spliterator(), false)
                            .map(r -> {
                                if (r.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(r)) {
                                    final var date = r.getDateCellValue();
                                    try {
                                        return dtf.format(date);
                                    } catch (Exception e) {
                                        LOGGER.warn("Could not format date {} expecting format {}: {}", date, dateTimeFormat, e.getMessage());
                                    }
                                }
                                return formatter.formatCellValue(r);
                            })
                            .map(v -> v.replace("\"", ""))
                            .forEachOrdered(item::add);

                        // TODO make this configurable
                        MaptiveId id = null;
                        final var val = new MaptiveData(idCol);

                        for (int i = 0; i < headers.size(); i++) {

                            final var header = headers.get(i);
                            if (idCol.equalsIgnoreCase(header)) {
                                id = new MaptiveId(item.get(i));
                            }

                            final var itemVal = i >= item.size()
                                ? ""
                                : item.get(i);
                            val.put(header, itemVal);
                        }

                        if (id == null) {
                            throw new IllegalStateException("No ID column exists with name \"" + idCol + "\"");
                        }

                        items.put(id, val);
                    }
                });

            data = items;
        }
    }

    @Override
    public Map<MaptiveId, MaptiveData> getAll() {
        try {
            refresh();
        } catch (IOException e) {
            LOGGER.error("Unable to refresh data from file: {}", file);
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }
}
