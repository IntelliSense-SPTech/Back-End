import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Leitor {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final JdbcTemplate jdbcTemplate;
    private final OperacoesBucket operacoesBucket;

    public Leitor(OperacoesBucket operacoesBucket) {
        DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
        this.jdbcTemplate = dbConnectionProvider.getConnection();
        this.operacoesBucket = operacoesBucket;
    }

    public boolean lerArquivo(String bucketName, String arquivoKey) {
        try (InputStream inputStream = operacoesBucket.baixarObjeto(bucketName, arquivoKey)) {
            processFile(inputStream);
            return true; // Retorna true se a leitura foi bem-sucedida
        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
            return false; // Retorna false se houver erro
        }
    }

    private void processFile(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row != null) {
                    Cell cellEspecificacao = row.getCell(0);
                    if (cellEspecificacao != null) {
                        String especificacao = cellEspecificacao.getStringCellValue();
                        int[] quantidades = new int[12];
                        for (int i = 1; i <= 12; i++) {
                            Cell cellQuantidade = row.getCell(i);
                            if (cellQuantidade != null) {
                                switch (cellQuantidade.getCellType()) {
                                    case NUMERIC:
                                        quantidades[i - 1] = (int) cellQuantidade.getNumericCellValue();
                                        break;
                                    case STRING:
                                        String cellValue = cellQuantidade.getStringCellValue();
                                        if ("...".equals(cellValue)) {
                                            quantidades[i - 1] = 0;
                                        } else {
                                            try {
                                                NumberFormat format = NumberFormat.getInstance();
                                                Number number = format.parse(cellValue.replace(".", "").replace(",", "."));
                                                quantidades[i - 1] = number.intValue();
                                            } catch (ParseException e) {
                                                System.err.println("Erro ao converter a célula para inteiro: " + e.getMessage());
                                                quantidades[i - 1] = 0;
                                            }
                                        }
                                        break;
                                    default:
                                        quantidades[i - 1] = 0;
                                }
                            } else {
                                quantidades[i - 1] = 0;
                            }
                        }

                        for (int i = 0; i < 12; i++) {
                            jdbcTemplate.update("INSERT INTO crimes (especificacao, qtd_casos, ano, mes) VALUES (?, ?, ?, ?)",
                                    especificacao, quantidades[i], 2024, i + 1);
                            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Foi lido e inserido no banco de dados: " + especificacao + ", Mês: " + (i + 1) + ", Casos: " + quantidades[i]);
                        }
                    } else {
                        System.out.println("Linha " + row.getRowNum() + " está vazia ou a célula de especificação é nula.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar o arquivo: " + e.getMessage());
        }
    }
}
