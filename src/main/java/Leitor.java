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

    // Construtor que inicializa o JdbcTemplate e as operações do bucket
    public Leitor(OperacoesBucket operacoesBucket) {
        DBConnectionProvider dbConnectionProvider = new DBConnectionProvider();
        this.jdbcTemplate = dbConnectionProvider.getConnection();
        this.operacoesBucket = operacoesBucket;
    }

    // Método que lê o arquivo a partir do bucket
    public boolean lerArquivo(String bucketName, String arquivoKey) {
        try (InputStream inputStream = operacoesBucket.baixarObjeto(bucketName, arquivoKey)) {
            // Chama o método processFile com o InputStream
            processFile(inputStream);
            return true; // Retorna true se a leitura foi bem-sucedida
        } catch (Exception e) {
            System.err.println("Erro ao ler o arquivo: " + e.getMessage());
            return false; // Retorna false se houver erro
        }
    }

    // Método que processa o arquivo e insere os dados no banco de dados
    private void processFile(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            // Lista de especificações de interesse
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

            // Itera sobre todas as planilhas (folhas) do arquivo
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String nomeDaFolha = sheet.getSheetName();

                // Tenta converter o nome da folha para o ano. Se falhar, ignora a folha.
                int ano;
                try {
                    ano = Integer.parseInt(nomeDaFolha);
                } catch (NumberFormatException e) {
                    System.out.println("Nome da folha '" + nomeDaFolha + "' não representa um ano válido. Ignorando a folha.");
                    continue;
                }

                // Itera sobre as linhas da folha, começando da segunda (índice 1)
                for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                    Row row = sheet.getRow(rowIndex);
                    if (row != null) {
                        Cell cellEspecificacao = row.getCell(0);
                        if (cellEspecificacao != null) {
                            String especificacao = cellEspecificacao.getStringCellValue();

                            // Verifica se a especificação está na lista de interesse
                            if (especificacoesValidas.contains(especificacao)) {
                                int[] quantidades = extrairQuantidades(row);

                                // Inserindo os dados no banco de dados para cada mês
                                for (int i = 0; i < 12; i++) {
                                    inserirNoBanco(especificacao, quantidades[i], ano, i + 1);
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

    // Método que extrai as quantidades de cada mês de uma linha do Excel
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
                quantidades[i - 1] = 0; // Define 0 caso a célula esteja vazia
            }
        }
        return quantidades;
    }

    // Método que converte uma string para um inteiro
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

    // Método que realiza a inserção no banco de dados
    private void inserirNoBanco(String especificacao, int quantidade, int ano, int mes) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO crimes (especificacao, qtd_casos, ano, mes) VALUES (?, ?, ?, ?)",
                    especificacao, quantidade, ano, mes
            );
            System.out.println("Tentando inserir: " + especificacao + ", Ano: " + ano + ", Mês: " + mes + ", Casos: " + quantidade);
            System.out.println("[" + LocalDateTime.now().format(formatter) + "] - Inserido: " + especificacao + ", Ano: " + ano + ", Mês: " + mes + ", Casos: " + quantidade);
        } catch (DataAccessException e) {
            System.err.println("Erro ao inserir no banco de dados: " + e.getMessage());
        }
    }
}
