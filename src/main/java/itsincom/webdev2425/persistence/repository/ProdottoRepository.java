package itsincom.webdev2425.persistence.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import itsincom.webdev2425.persistence.model.Prodotto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

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
        // creo il nuovo prodotto
        Prodotto newProdotto = Prodotto.create(prodotto.getNome(), prodotto.getDescrizione(), prodotto.getIngredienti(), prodotto.getQuantita(), prodotto.getPrezzo(), prodotto.getFoto());
        // aggiungo il prodotto al database
        persist(newProdotto);
        return newProdotto;
    }

    public Prodotto update(Prodotto prodotto, String id) {
        // aggiorno il prodotto
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

        // Creazione degli stili
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.MEDIUM);
        headerStyle.setBorderTop(BorderStyle.MEDIUM);
        headerStyle.setBorderRight(BorderStyle.MEDIUM);
        headerStyle.setBorderLeft(BorderStyle.MEDIUM);

        CellStyle borderStyle = workbook.createCellStyle();
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);

        // Creazione dell'intestazione
        Row headerRow = sheet.createRow(0);
        String[] columns = {"ID", "Nome", "Descrizione", "Ingredienti", "Quantità", "Prezzo", "Foto"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }

        // Popolamento dei dati
        int rowNum = 1;
        for (Prodotto prodotto : prodotti) {
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue(prodotto.getId());
            cell.setCellStyle(borderStyle);

            cell = row.createCell(1);
            cell.setCellValue(prodotto.getNome());
            cell.setCellStyle(borderStyle);

            cell = row.createCell(2);
            cell.setCellValue(prodotto.getDescrizione());
            cell.setCellStyle(borderStyle);

            cell = row.createCell(3);
            cell.setCellValue(prodotto.getIngredienti());
            cell.setCellStyle(borderStyle);

            cell = row.createCell(4);
            cell.setCellValue(prodotto.getQuantita());
            cell.setCellStyle(borderStyle);

            cell = row.createCell(5);
            cell.setCellValue(prodotto.getPrezzo().doubleValue());
            cell.setCellStyle(borderStyle);

            cell = row.createCell(6);
            cell.setCellValue(prodotto.getFoto());
            cell.setCellStyle(borderStyle);
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
