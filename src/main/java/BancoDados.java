import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BancoDados {
    private final JdbcTemplate jdbcTemplate;
    private final Leitor leitor;
    private String fraseAumento = "";
    private String fraseReducao = "";
    private String assunto = "Veja o aumento e a redução dos crimes";

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

    public void truncateCrimes(){
        String sql = "TRUNCATE crimes;";

        try {
            jdbcTemplate.update(sql);
            System.out.println("Tabelas crimes deletados.");
        } catch (Exception e) {
            System.err.println("Erro ao deletar tabela crimes: " + e.getMessage());
        }

    }

    public void notificacaoAumentoDeCrimes() {
        String sql = """ 
    WITH dados_recentes AS (
        SELECT
            c.localidade,
            c.especificacao,
            c.qtd_casos,
            c.ano,
            c.mes,
            ROW_NUMBER() OVER (PARTITION BY c.localidade, c.especificacao ORDER BY c.ano DESC, c.mes DESC) AS ordem_mes
        FROM crimes c
    ),
    comparacao AS (
        SELECT
            atual.localidade,
            atual.especificacao,
            atual.qtd_casos AS casos_mes_atual,
            anterior.qtd_casos AS casos_mes_anterior,
            ROUND(((atual.qtd_casos - anterior.qtd_casos) / NULLIF(anterior.qtd_casos, 0)) * 100, 2) AS aumento_percentual
        FROM
            dados_recentes atual
        LEFT JOIN
            dados_recentes anterior
        ON
            atual.localidade = anterior.localidade
            AND atual.especificacao = anterior.especificacao
            AND atual.ordem_mes = 1
            AND anterior.ordem_mes = 2
        WHERE
            anterior.qtd_casos IS NOT NULL
            AND atual.qtd_casos > anterior.qtd_casos
    )
    SELECT
        localidade,
        especificacao AS crime,
        aumento_percentual
    FROM
        comparacao
    ORDER BY
        aumento_percentual DESC
    LIMIT 1;
    """;

        // Executa a consulta e captura os resultados
        List<String> crimes = jdbcTemplate.query(sql, (rs, rowNum) -> {
            String localidade = rs.getString("localidade");
            String crime = rs.getString("crime");
            Double aumentoPercentual = rs.getDouble("aumento_percentual");

            // Formata a mensagem para enviar ao Slack
            fraseAumento =  "Localidade: " + localidade + ", Crime: " + crime + ", Aumento Percentual: " + String.format("%.2f", aumentoPercentual) + "%";
            return "Localidade: " + localidade + ", Crime: " + crime + ", Aumento Percentual: " + String.format("%.2f", aumentoPercentual) + "%";
        });


        System.out.println(crimes);
    }

    public void notificacaoReducaoDeCrimes() {
        String sql = """
                WITH dados_recentes AS (
                                SELECT
                                    c.localidade,
                                    c.especificacao,
                                    c.qtd_casos,
                                    c.ano,
                                    c.mes,
                                    ROW_NUMBER() OVER (PARTITION BY c.localidade, c.especificacao ORDER BY c.ano DESC, c.mes DESC) AS ordem_mes
                                FROM crimes c
                            ),
                            comparacao AS (
                                SELECT
                                    atual.localidade,
                                    atual.especificacao,
                                    atual.qtd_casos AS casos_mes_atual,
                                    anterior.qtd_casos AS casos_mes_anterior,
                                    ROUND(((anterior.qtd_casos - atual.qtd_casos) / NULLIF(anterior.qtd_casos, 0)) * 100, 2) AS reducao_percentual
                                FROM
                                    dados_recentes atual
                                LEFT JOIN
                                    dados_recentes anterior
                                ON
                                    atual.localidade = anterior.localidade
                                    AND atual.especificacao = anterior.especificacao
                                    AND atual.ordem_mes = 1
                                    AND anterior.ordem_mes = 2
                                WHERE
                                    anterior.qtd_casos IS NOT NULL
                                    AND atual.qtd_casos < anterior.qtd_casos
                            )
                            SELECT
                                localidade,
                                especificacao AS crime,
                                reducao_percentual
                            FROM
                                comparacao
                            ORDER BY
                                reducao_percentual DESC
                            LIMIT 1;
        """;

        // Executa a consulta e captura os resultados
        List<String> crimes = jdbcTemplate.query(sql, (rs, rowNum) -> {
            String localidade = rs.getString("localidade");
            String crime = rs.getString("crime");
            Double reducaoPercentual = rs.getDouble("reducao_percentual");

            fraseReducao = "Localidade: " + localidade + ", Crime: " + crime + ", Redução Percentual: " + String.format("%.2f", reducaoPercentual) + "%";

            // Formata a mensagem para enviar ao Slack
            return "Localidade: " + localidade + ", Crime: " + crime + ", Redução Percentual: " + String.format("%.2f", reducaoPercentual) + "%";
        });

        System.out.println(crimes);

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

    public String getFraseAumento() {
        return fraseAumento;
    }

    public String getFraseReducao() {
        return fraseReducao;
    }

    public String getAssunto() {
        return assunto;
    }
}
