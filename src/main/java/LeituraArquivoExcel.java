import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class LeituraArquivoExcel {
    public void lerArquivo() throws Exception {
        Path caminho = Path.of("OcorrenciaMensal(Criminal)-EstadoSP_20241007_134342.xlsx");
        InputStream arquivo = Files.newInputStream(caminho);

        Workbook workbook = new XSSFWorkbook(arquivo);

        Sheet sheet = workbook.getSheetAt(0);

        Row row = sheet.getRow(0);

        Cell cell = row.getCell(0);

        String valor = cell.getStringCellValue();
        System.out.println("Valor da primeira célula: " + valor);

        workbook.close();
        arquivo.close();
    }
}
