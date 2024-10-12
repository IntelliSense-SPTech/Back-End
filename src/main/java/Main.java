import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        LocalDateTime agora = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        System.out.println("");
        System.out.println("");
        System.out.println("[" + agora.format(formatter) + "] - ✔️ Login realizado com sucesso!");
        System.out.println("");
        System.out.println("[" + agora.format(formatter) + "] - 🔒 Usuário está deslogando...");
        System.out.println("");
        System.out.println("[" + agora.format(formatter) + "] - ❌ Erro: Falha ao tentar realizar login. Por favor, tente novamente.");
        System.out.println("");
        System.out.println("");
        System.out.println("[" + agora.format(formatter) + "] - ✔️ Login realizado com sucesso!");
        System.out.println("");
        System.out.println("[" + agora.format(formatter) + "] - 🔒 Usuário está deslogando...");
        System.out.println("");
        System.out.println("[" + agora.format(formatter) + "] - ❌ Erro: Falha ao tentar realizar login. Por favor, tente novamente.");

        System.out.println("");
    }
}