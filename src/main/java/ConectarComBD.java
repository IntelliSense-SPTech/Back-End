import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class ConectarComBD {
    private final JdbcTemplate jdbcTemplate;

    public ConectarComBD() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/nome_do_banco");
        dataSource.setUsername("usuario");
        dataSource.setPassword("senha");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void inserirDados(String dado) {
        String sql = "INSERT INTO tabela (coluna) VALUES (?)";
        jdbcTemplate.update(sql, dado);
        System.out.println("Dado inserido no banco: " + dado);
    }
}
