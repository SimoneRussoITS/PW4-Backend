package itsincom.webdev2425.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import itsincom.webdev2425.persistence.model.Prodotto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@ApplicationScoped
@Transactional
public class ProdottoRepository implements PanacheRepository<Prodotto> {

    public List<Prodotto> getAll(String search) {
        if (search == null) {
            return listAll();
        } else {
            return list("nome like ?1", "%" + search + "%");
        }
    }

    public Prodotto findByName(String nome) {
        Prodotto prodotto = find("nome", nome).firstResult();
        return prodotto;
    }

    public Prodotto getById(String id) {
        Long idLong = Long.parseLong(id);
        return findById(idLong);
    }

    public Prodotto add(Prodotto prodotto) {
        Prodotto newProdotto = Prodotto.create(prodotto.getNome(), prodotto.getDescrizione(), prodotto.getIngredienti(), prodotto.getQuantita(), prodotto.getPrezzo(), prodotto.getFoto());
        persist(newProdotto);
        return newProdotto;
    }

    public Prodotto update(Prodotto prodotto, String id) {
        update("nome = ?1, " +
               "descrizione = ?2, " +
               "ingredienti = ?3, " +
               "quantita = ?4, " +
               "prezzo = ?5, " +
               "foto = ?6 " +
               "where id = ?7",
                prodotto.getNome(),
                prodotto.getDescrizione(),
                prodotto.getIngredienti(),
                prodotto.getQuantita(),
                prodotto.getPrezzo(),
                prodotto.getFoto(),
                id);
        return prodotto;
    }

    public void delete(String id) {
        Long idLong = Long.parseLong(id);
        deleteById(idLong);
    }

    public void getExcelInventario() {
        List<Prodotto> prodotti = listAll();
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Inventario");

        // Creazione dell'intestazione
        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Nome", "Descrizione", "Ingredienti", "Quantità", "Prezzo", "Foto"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
        }

        // Popolamento dei dati
        int rowNum = 1;
        for (Prodotto prodotto : prodotti) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(prodotto.getId());
            row.createCell(1).setCellValue(prodotto.getNome());
            row.createCell(2).setCellValue(prodotto.getDescrizione());
            row.createCell(3).setCellValue(prodotto.getIngredienti());
            row.createCell(4).setCellValue(prodotto.getQuantita());
            row.createCell(5).setCellValue(prodotto.getPrezzo().doubleValue());
            row.createCell(6).setCellValue(prodotto.getFoto());
        }

        // Scrittura del file
        try (FileOutputStream fileOut = new FileOutputStream(System.getProperty("user.home") + "/Downloads/inventario.xls")) {
            workbook.write(fileOut);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
