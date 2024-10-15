import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStream;

public class Leitor {
    private final JdbcTemplate jdbcTemplate;
    private final OperacoesBucket operacoesBucket;

    public Leitor(OperacoesBucket operacoesBucket) {
        DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
        this.jdbcTemplate = dbConnectionProvider.getConnection();
        this.operacoesBucket = operacoesBucket;
    }

    public void lerArquivo(String bucketName, String arquivoKey) {
        try (InputStream inputStream = operacoesBucket.baixarObjeto(bucketName, arquivoKey)) {
            processFile(inputStream);
        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    private void processFile(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            // Acessando a primeira planilha
            Sheet sheet = workbook.getSheetAt(0);

            // Iterando sobre as linhas da planilha
            for (Row row : sheet) {
                // Assumindo que a primeira célula tem o valor que queremos inserir no BD
                Cell cell = row.getCell(0);
                if (cell != null) { // Verifica se a célula não é nula
                    String valor = cell.getStringCellValue();
                    System.out.println("Valor lido: " + valor);

                    // Inserindo o valor no banco de dados
                    jdbcTemplate.update("INSERT INTO crimes (especificacao) VALUES (?)", valor);
                } else {
                    System.out.println("Linha " + row.getRowNum() + " está vazia ou a célula é nula.");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar o arquivo: " + e.getMessage());
        }
    }
}
