import java.time.LocalDate;

public interface Caducable {
    LocalDate getFechaCaducidad();
    boolean estaCaducado(LocalDate fechaOperacion);
    
}
