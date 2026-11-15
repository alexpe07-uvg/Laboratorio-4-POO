import java.util.List;
import java.util.ArrayList;

public class Bodega implements Iterable<Lote> {
    private List<Lote> lotes;

    public Bodega() {
        this.lotes = new ArrayList<>();
    }

    public void agregarLote(Lote lote) {
        if (lote != null) {
            lotes.add(lote);
        }
        
    }

    @Override
    
}
