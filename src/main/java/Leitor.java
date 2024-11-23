import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;

import java.io.InputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class Leitor {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final JdbcTemplate jdbcTemplate;
    private final OperacoesBucket operacoesBucket;

    public Leitor(OperacoesBucket operacoesBucket) {
        DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
        this.jdbcTemplate = dbConnectionProvider.getConnection();
        this.operacoesBucket = operacoesBucket;
    }

    public List<String> arquivoKeys = Arrays.asList(
            "OcorrenciaMensal(Criminal)-Grande São Paulo (exclui a Capital)_20240917_174302.xlsx",
            "OcorrenciaMensal(Criminal)-Capital_20240917_174245.xlsx",
            "OcorrenciaMensal(Criminal)-Santos_20240917_174314.xlsx"
    );

    public boolean lerArquivo(String bucketName, String arquivoKey) {
        try (InputStream inputStream = operacoesBucket.baixarObjeto(bucketName, arquivoKey)) {
            processFile(inputStream, arquivoKey);
            return true;
        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
            return false;
        }
    }

    private void processFile(InputStream inputStream, String arquivoKey) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            List<String> especificacoesValidas = Arrays.asList(
                    "LATROCÍNIO",
                    "TOTAL DE ROUBO - OUTROS (1)",
                    "ROUBO - OUTROS",
                    "ROUBO DE VEÍCULO",
                    "ROUBO A BANCO",
                    "ROUBO DE CARGA",
                    "FURTO - OUTROS",
                    "FURTO DE VEÍCULO"
            );

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String nomeDaFolha = sheet.getSheetName();

                int ano;
                try {
                    ano = Integer.parseInt(nomeDaFolha);
                } catch (NumberFormatException e) {
                    System.out.println("Nome da folha '" + nomeDaFolha + "' não representa um ano válido. Ignorando a folha.");
                    continue;
                }

                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        Cell cellEspecificacao = row.getCell(0);
                        if (cellEspecificacao != null) {
                            String especificacao = cellEspecificacao.getStringCellValue();

                            if (especificacoesValidas.contains(especificacao)) {
                                int[] quantidades = extrairQuantidades(row);
                                String localidade = definirLocalidade(arquivoKey);

                                for (int i = 0; i < 12; i++) {
                                    inserirNoBanco(especificacao, quantidades[i], ano, i + 1, localidade);
                                }
                            }
                        } else {
                            System.out.println("Linha " + row.getRowNum() + " está vazia ou a célula de especificação é nula.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar o arquivo: " + e.getMessage());
        }
    }

    private int[] extrairQuantidades(Row row) {
        int[] quantidades = new int[12];
        for (int i = 1; i <= 12; i++) {
            Cell cellQuantidade = row.getCell(i);
            if (cellQuantidade != null) {
                try {
                    switch (cellQuantidade.getCellType()) {
                        case NUMERIC:
                            quantidades[i - 1] = (int) cellQuantidade.getNumericCellValue();
                            break;
                        case STRING:
                            quantidades[i - 1] = parseStringAsInt(cellQuantidade.getStringCellValue());
                            break;
                        default:
                            quantidades[i - 1] = 0;
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao processar a célula na linha " + row.getRowNum() + ": " + e.getMessage());
                    quantidades[i - 1] = 0;
                }
            } else {
                quantidades[i - 1] = 0;
            }
        }
        return quantidades;
    }

    private int parseStringAsInt(String value) {
        if ("...".equals(value)) {
            return 0;
        }
        try {
            NumberFormat format = NumberFormat.getInstance();
            Number number = format.parse(value.replace(".", "").replace(",", "."));
            return number.intValue();
        } catch (ParseException e) {
            System.err.println("Erro ao converter a célula para inteiro: " + e.getMessage());
            return 0;
        }
    }

    private String definirLocalidade(String arquivoKey) {
        if (arquivoKey.contains("Grande São Paulo")) {
            return "Grande São Paulo";
        } else if (arquivoKey.contains("Capital")) {
            return "Capital";
        } else if (arquivoKey.contains("Santos")){
            return "Litoral";
        }else{
            return "Desconhecido";
        }
    }

    private void inserirNoBanco(String especificacao, int quantidade, int ano, int mes, String localidade) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO crimes (especificacao, qtd_casos, ano, mes, localidade) VALUES (?, ?, ?, ?, ?)",
                    especificacao, quantidade, ano, mes, localidade
            );
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Inserido: " + especificacao + ", Ano: " + ano + ", Mês: " + mes + ", Casos: " + quantidade + ", Localidade: " + localidade);
        } catch (DataAccessException e) {
            System.err.println("Erro ao inserir no banco de dados: " + e.getMessage());
        }
    }
}
