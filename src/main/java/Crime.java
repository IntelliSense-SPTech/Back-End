import java.util.Date;

public class Crime {
    private int idCrime;
    private String local;
    private String descricao;
    private Date dataCrime;

    public Crime(int idCrime, String local, String descricao, Date dataCrime) {
        this.idCrime = idCrime;
        this.local = local;
        this.descricao = descricao;
        this.dataCrime = dataCrime;
    }

    public int getIdCrime() {
        return idCrime;
    }

    public void setIdCrime(int idCrime) {
        this.idCrime = idCrime;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Date getDataCrime() {
        return dataCrime;
    }

    public void setDataCrime(Date dataCrime) {
        this.dataCrime = dataCrime;
    }

    public void registrarCrime() {
        System.out.println("Crime registrado:");
        System.out.println("ID: " + idCrime);
        System.out.println("Local: " + local);
        System.out.println("Descrição: " + descricao);
        System.out.println("Data: " + dataCrime);
    }

    public void consultarCrimes() {
        System.out.println("Consultando crime:");
        System.out.println("ID: " + idCrime);
        System.out.println("Local: " + local);
        System.out.println("Descrição: " + descricao);
        System.out.println("Data: " + dataCrime);
    }
}
