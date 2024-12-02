import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BancoDados {
    private final JdbcTemplate jdbcTemplate;
    private final Leitor leitor;

    public BancoDados() {
        DBConnectionProvider connectionProvider = new DBConnectionProvider();
        this.jdbcTemplate = connectionProvider.getConnection();
        S3Provider s3Provider = new S3Provider();
        OperacoesBucket operacoesBucket = new OperacoesBucket(s3Provider);
        this.leitor = new Leitor(operacoesBucket, this); // Passando 'this' para BancoDados, se necessário
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public void registrarCrime(String especificacao, Integer quantidade, int ano, int mes, String localidade) {
        if (ano == 2024 && (mes >= 8 && mes <= 12)) {
            System.out.println("Registro ignorado: Ano 2024 e mês " + mes + " não são permitidos para inserção.");
            return;
        }

        if ("LATROCÍNIO".equals(especificacao) && (quantidade == null || quantidade == 0)) {
            System.out.println("Quantidade para 'LATROCÍNIO' ajustada de " + quantidade + " para 1 no mês "+mes+", do ano: "+ano);
            quantidade = 1;
        }

        if (quantidade == null) {
            System.out.println("Valor '...' encontrado na especificação: " + especificacao + " em " + localidade + " - Não será inserido no banco.");
            return;
        }
        try {
            // Inserção no banco de dados
            jdbcTemplate.update(
                    "INSERT INTO crimes (especificacao, qtd_casos, ano, mes, localidade) VALUES (?, ?, ?, ?, ?)",
                    especificacao, quantidade, ano, mes, localidade
            );
            System.out.println("["+ LocalDateTime.now().format(formatter) + "] - Crime registrado: " + especificacao + ", Ano: " + ano + ", Mês: " + mes + ", Localidade: "+localidade);
        } catch (Exception e) {
            System.err.println("Erro ao registrar crime: " + e.getMessage());
        }
    }

    public void consultarCrimes() {
        String sql = "SELECT * FROM crimes";
        try {
            List<String> crimes = jdbcTemplate.query(sql, (rs, rowNum) ->
                    "ID: " + rs.getInt("id") + ", Especificação: " + rs.getString("especificacao")
            );
            crimes.forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("Erro ao consultar crimes: " + e.getMessage());
        }
    }

    public void selecionarCrimes(String localidade) {
        String sql = "SELECT * FROM crimes WHERE localidade = ?";
        try {
            List<String> crimes = jdbcTemplate.query(sql, new Object[]{localidade}, (rs, rowNum) ->
                    "ID: " + rs.getInt("id") + ", Especificação: " + rs.getString("especificacao")
            );
            crimes.forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("Erro ao selecionar crimes: " + e.getMessage());
        }
    }

    public void deletarCrimes(int id) {
        String sql = "DELETE FROM crimes WHERE id = ?";
        try {
            jdbcTemplate.update(sql, id);
            System.out.println("Crime com ID " + id + " foi deletado.");
        } catch (Exception e) {
            System.err.println("Erro ao deletar crime: " + e.getMessage());
        }
    }

    public void criarTabelas() {
        String sql = """
                CREATE TABLE IF NOT EXISTS crimes (
                    id SERIAL PRIMARY KEY,
                    especificacao VARCHAR(255),
                    qtd_casos INT,
                    ano INT,
                    mes INT,
                    localidade VARCHAR(255)
                )
                """;
        try {
            jdbcTemplate.execute(sql);
            System.out.println("Tabela 'crimes' criada com sucesso.");
        } catch (Exception e) {
            System.err.println("Erro ao criar tabela: " + e.getMessage());
        }
    }

    public void processarArquivos(String bucketName) {
        leitor.arquivoKeys.forEach(key -> {
            if (leitor.lerArquivo(bucketName, key)) {
                System.out.println("Arquivo processado: " + key);
            } else {
                System.err.println("Erro ao processar o arquivo: " + key);
            }
        });
    }
}
