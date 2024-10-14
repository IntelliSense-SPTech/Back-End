import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;

public class LeituraArquivo {
    public static void main(String[] args) {
        String caminhoArquivo = "caminho/para/seu-arquivo.xlsm";

        try (FileInputStream arquivo = new FileInputStream(caminhoArquivo);
             Workbook workbook = new XSSFWorkbook(arquivo)) {

            // Acessando a primeira planilha
            Sheet sheet = workbook.getSheetAt(0);

            // Iterando pelas linhas da planilha
            for (Row row : sheet) {
                for (Cell cell : row) {
                    // Obtendo e imprimindo o valor da célula
                    printCellValue(cell);
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    private static void printCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING -> System.out.println(cell.getStringCellValue());
            case NUMERIC -> System.out.println(cell.getNumericCellValue());
            case BOOLEAN -> System.out.println(cell.getBooleanCellValue());
            default -> System.out.println("Tipo de dado não suportado.");
        }
    }
}
